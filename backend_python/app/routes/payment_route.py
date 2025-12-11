from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from payos import PayOS
import os
import time
import hmac
import hashlib

PAYMENT_BLUEPRINT = Blueprint('payment', __name__)

# Khởi tạo PayOS với thông tin từ biến môi trường
def get_payos():
    return PayOS(
        client_id=os.getenv("PAYOS_CLIENT_ID"),
        api_key=os.getenv("PAYOS_API_KEY"),
        checksum_key=os.getenv("PAYOS_CHECKSUM_KEY")
    )


def create_signature(data: dict) -> str:
    """
    Tạo chữ ký HMAC_SHA256 theo định dạng PayOS yêu cầu
    Data phải được sort theo alphabet: amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
    """
    checksum_key = os.getenv("PAYOS_CHECKSUM_KEY")
    
    # Sort keys theo alphabet và tạo string
    sorted_data = "&".join([f"{k}={v}" for k, v in sorted(data.items())])
    
    # Tạo HMAC_SHA256 signature
    signature = hmac.new(
        checksum_key.encode('utf-8'),
        sorted_data.encode('utf-8'),
        hashlib.sha256
    ).hexdigest()
    
    return signature


def verify_webhook_signature(webhook_data: dict) -> bool:
    """
    Xác thực chữ ký webhook từ PayOS
    """
    try:
        checksum_key = os.getenv("PAYOS_CHECKSUM_KEY")
        received_signature = webhook_data.get("signature", "")
        data = webhook_data.get("data", {})
        
        # Tạo signature từ data nhận được
        sorted_data = "&".join([f"{k}={v}" for k, v in sorted(data.items()) if v is not None and v != ""])
        
        calculated_signature = hmac.new(
            checksum_key.encode('utf-8'),
            sorted_data.encode('utf-8'),
            hashlib.sha256
        ).hexdigest()
        
        return hmac.compare_digest(calculated_signature, received_signature)
    except Exception as e:
        print(f"Signature verification error: {e}")
        return False


# ==================== API ENDPOINTS ====================

@PAYMENT_BLUEPRINT.route('/create-payment', methods=['POST'])
def create_payment():
    """
    API: Tạo link thanh toán PayOS
    
    Request body (JSON):
    {
        "orderCode": 123456789,       # Mã đơn hàng (tùy chọn, auto-generate nếu không có)
        "amount": 50000,              # Số tiền (VND) - BẮT BUỘC
        "description": "Thanh toan",  # Mô tả (tối đa 25 ký tự cho bank không liên kết payOS, 9 ký tự cho bank thường)
        "buyerName": "Nguyen Van A",  # Tên người mua (tùy chọn)
        "buyerEmail": "a@email.com",  # Email người mua (tùy chọn)
        "buyerPhone": "0901234567",   # SĐT người mua (tùy chọn)
        "buyerAddress": "123 ABC",    # Địa chỉ (tùy chọn)
        "items": [                    # Danh sách sản phẩm (tùy chọn)
            {"name": "Ve phim", "quantity": 2, "price": 25000}
        ],
        "expiredAt": 1699999999,      # Thời gian hết hạn (Unix timestamp, tùy chọn)
        "cancelUrl": "https://...",   # URL khi hủy (tùy chọn)
        "returnUrl": "https://..."    # URL khi thành công (tùy chọn)
    }
    
    Response (200):
    {
        "code": "00",
        "desc": "success",
        "data": {
            "bin": "970422",
            "accountNumber": "113366668888",
            "accountName": "...",
            "amount": 50000,
            "description": "THANH TOAN DON HANG 123",
            "orderCode": 123456789,
            "currency": "VND",
            "paymentLinkId": "...",
            "status": "PENDING",
            "checkoutUrl": "https://pay.payos.vn/web/...",
            "qrCode": "..."
        }
    }
    """
    try:
        data = request.json or {}
        
        # === Validate required fields ===
        amount = data.get('amount')
        if not amount:
            return jsonify({
                "code": "01",
                "desc": "Amount is required",
                "data": None
            }), 400
            
        amount = int(amount)
        if amount < 1000:
            return jsonify({
                "code": "01",
                "desc": "Số tiền tối thiểu là 1000 VND",
                "data": None
            }), 400
        
        # === Generate order code if not provided ===
        order_code = data.get('orderCode')
        if not order_code:
            # Dùng timestamp milliseconds để đảm bảo unique
            order_code = int(time.time() * 1000) % 9007199254740991  # Max safe integer
        else:
            order_code = int(order_code)

        # === Process description (giới hạn 9 ký tự cho bank thường) ===
        description = data.get('description', f'DH{order_code}')
        # Giới hạn 9 ký tự, không dấu, không ký tự đặc biệt
        description = description[:9].upper()

        # === URLs ===
        cancel_url = data.get('cancelUrl', 'https://your-app.com/cancel')
        return_url = data.get('returnUrl', 'https://your-app.com/success')

        # === Items ===
        items_data = data.get('items', [])
        if not items_data:
            items_data = [{"name": "Thanh toan", "quantity": 1, "price": amount}]
        
        items = []
        for item in items_data:
            items.append({
                "name": item.get('name', 'San pham')[:50],
                "quantity": int(item.get('quantity', 1)),
                "price": int(item.get('price', amount))
            })

        # === Create payment request data ===
        payment_data = {
            "orderCode": order_code,
            "amount": amount,
            "description": description,
            "items": items,
            "cancelUrl": cancel_url,
            "returnUrl": return_url
        }
        
        # Add optional fields
        if data.get('buyerName'):
            payment_data["buyerName"] = data.get('buyerName')
        if data.get('buyerEmail'):
            payment_data["buyerEmail"] = data.get('buyerEmail')
        if data.get('buyerPhone'):
            payment_data["buyerPhone"] = data.get('buyerPhone')
        if data.get('buyerAddress'):
            payment_data["buyerAddress"] = data.get('buyerAddress')
        if data.get('expiredAt'):
            payment_data["expiredAt"] = data.get('expiredAt')

        # === Call PayOS API ===
        payos = get_payos()
        response = payos.payment_requests.create(payment_data)
        
        # === Return success response ===
        return jsonify({
            "code": "00",
            "desc": "success",
            "data": {
                "bin": getattr(response, 'bin', None),
                "accountNumber": getattr(response, 'account_number', None),
                "accountName": getattr(response, 'account_name', None),
                "amount": amount,
                "description": description,
                "orderCode": order_code,
                "currency": getattr(response, 'currency', 'VND'),
                "paymentLinkId": getattr(response, 'payment_link_id', None),
                "status": getattr(response, 'status', 'PENDING'),
                "checkoutUrl": getattr(response, 'checkout_url', None),
                "qrCode": getattr(response, 'qr_code', None)
            }
        }), 200

    except Exception as e:
        print(f"PayOS Create Payment Error: {str(e)}")
        return jsonify({
            "code": "99",
            "desc": f"Error: {str(e)}",
            "data": None
        }), 500


@PAYMENT_BLUEPRINT.route('/payment-requests/<order_id>', methods=['GET'])
def get_payment_info(order_id):
    """
    API: Lấy thông tin link thanh toán
    
    Path params:
        order_id: Mã đơn hàng hoặc mã link thanh toán payOS
    
    Response (200):
    {
        "code": "00",
        "desc": "success",
        "data": {
            "id": "124c33293c934a85be5b7f8761a27a07",
            "orderCode": 123,
            "amount": 10000,
            "amountPaid": 0,
            "amountRemaining": 10000,
            "status": "PENDING" | "PAID" | "CANCELLED",
            "createdAt": "2024-01-15T10:30:00.000Z",
            "transactions": []
        }
    }
    """
    try:
        payos = get_payos()
        
        # Xử lý order_id (có thể là số hoặc string)
        try:
            order_code = int(order_id)
        except ValueError:
            order_code = order_id  # Giữ nguyên nếu là paymentLinkId
            
        payment_info = payos.payment_requests.get(order_code)
        
        return jsonify({
            "code": "00",
            "desc": "success",
            "data": {
                "id": getattr(payment_info, 'id', None),
                "orderCode": getattr(payment_info, 'order_code', order_code),
                "amount": getattr(payment_info, 'amount', 0),
                "amountPaid": getattr(payment_info, 'amount_paid', 0),
                "amountRemaining": getattr(payment_info, 'amount_remaining', 0),
                "status": getattr(payment_info, 'status', 'UNKNOWN'),
                "createdAt": getattr(payment_info, 'created_at', None),
                "transactions": getattr(payment_info, 'transactions', []),
                "canceledAt": getattr(payment_info, 'canceled_at', None),
                "cancellationReason": getattr(payment_info, 'cancellation_reason', None)
            }
        }), 200
        
    except Exception as e:
        print(f"PayOS Get Payment Info Error: {str(e)}")
        return jsonify({
            "code": "99",
            "desc": f"Error: {str(e)}",
            "data": None
        }), 500


@PAYMENT_BLUEPRINT.route('/payment-requests/<order_id>/cancel', methods=['POST'])
def cancel_payment(order_id):
    """
    API: Hủy link thanh toán
    
    Path params:
        order_id: Mã đơn hàng hoặc mã link thanh toán payOS
    
    Request body (JSON - optional):
    {
        "cancellationReason": "Changed my mind"
    }
    
    Response (200):
    {
        "code": "00",
        "desc": "success",
        "data": {
            "id": "...",
            "orderCode": 123,
            "amount": 10000,
            "status": "CANCELLED",
            "canceledAt": "...",
            "cancellationReason": "..."
        }
    }
    """
    try:
        data = request.json or {}
        cancellation_reason = data.get('cancellationReason', 'User cancelled')
        
        payos = get_payos()
        
        # Xử lý order_id
        try:
            order_code = int(order_id)
        except ValueError:
            order_code = order_id
            
        # Cancel payment link
        result = payos.payment_requests.cancel(order_code, cancellation_reason)
        
        return jsonify({
            "code": "00",
            "desc": "success",
            "data": {
                "id": getattr(result, 'id', None),
                "orderCode": getattr(result, 'order_code', order_code),
                "amount": getattr(result, 'amount', 0),
                "amountPaid": getattr(result, 'amount_paid', 0),
                "amountRemaining": getattr(result, 'amount_remaining', 0),
                "status": "CANCELLED",
                "canceledAt": getattr(result, 'canceled_at', None),
                "cancellationReason": cancellation_reason
            }
        }), 200
        
    except Exception as e:
        print(f"PayOS Cancel Payment Error: {str(e)}")
        return jsonify({
            "code": "99",
            "desc": f"Error: {str(e)}",
            "data": None
        }), 500


@PAYMENT_BLUEPRINT.route('/webhook', methods=['POST'])
def payment_webhook():
    """
    Webhook: Nhận thông tin thanh toán từ PayOS
    Cần cấu hình URL này trên Dashboard PayOS (ví dụ: https://your-ngrok.dev/api/payment/webhook)
    
    Request body từ PayOS:
    {
        "code": "00",
        "desc": "success",
        "success": true,
        "data": {
            "orderCode": 123,
            "amount": 3000,
            "description": "VQRIO123",
            "accountNumber": "12345678",
            "reference": "TF230204212323",
            "transactionDateTime": "2023-02-04 18:25:00",
            "currency": "VND",
            "paymentLinkId": "...",
            "code": "00",
            "desc": "Thành công",
            "counterAccountBankId": "",
            "counterAccountBankName": "",
            "counterAccountName": "",
            "counterAccountNumber": "",
            "virtualAccountName": "",
            "virtualAccountNumber": ""
        },
        "signature": "..."
    }
    
    Response: Trả về 2XX để xác nhận đã nhận webhook
    """
    try:
        webhook_data = request.json
        print(f"[WEBHOOK] Received: {webhook_data}")
        
        # Verify signature
        payos = get_payos()
        
        try:
            # Sử dụng SDK để verify
            verified_data = payos.webhooks.verify(webhook_data)
            
            if verified_data:
                order_code = getattr(verified_data, 'order_code', None) or getattr(verified_data, 'orderCode', None)
                amount = getattr(verified_data, 'amount', 0)
                description = getattr(verified_data, 'description', '')
                transaction_datetime = getattr(verified_data, 'transaction_date_time', '') or getattr(verified_data, 'transactionDateTime', '')
                reference = getattr(verified_data, 'reference', '')
                
                print(f"[WEBHOOK] Payment VERIFIED - OrderCode: {order_code}, Amount: {amount}, Ref: {reference}")
                
                # =====================================================
                # TODO: CẬP NHẬT DATABASE Ở ĐÂY
                # Ví dụ: Cập nhật trạng thái ticket sang PAID
                # 
                # from app.models.Ticket import Ticket
                # from app.extension import db
                # 
                # ticket = Ticket.query.filter_by(order_code=order_code).first()
                # if ticket:
                #     ticket.status = 'PAID'
                #     ticket.paid_at = datetime.now()
                #     db.session.commit()
                # =====================================================
                
                return jsonify({
                    "code": "00",
                    "desc": "Webhook received successfully",
                    "success": True
                }), 200
            else:
                print("[WEBHOOK] Invalid signature!")
                return jsonify({
                    "code": "97",
                    "desc": "Invalid signature",
                    "success": False
                }), 400
                
        except Exception as verify_error:
            print(f"[WEBHOOK] Verification error: {verify_error}")
            # Vẫn trả về 200 để PayOS không retry liên tục
            return jsonify({
                "code": "99",
                "desc": f"Verification error: {str(verify_error)}",
                "success": False
            }), 200
            
    except Exception as e:
        print(f"[WEBHOOK] Error: {str(e)}")
        return jsonify({
            "code": "99",
            "desc": str(e),
            "success": False
        }), 500


@PAYMENT_BLUEPRINT.route('/confirm-webhook', methods=['POST'])
def confirm_webhook():
    """
    API: Xác thực và cập nhật Webhook URL cho kênh thanh toán
    Gọi API này để đăng ký webhook URL mới với PayOS
    
    Request body:
    {
        "webhookUrl": "https://your-domain.com/api/payment/webhook"
    }
    
    Response (200):
    {
        "code": "00",
        "desc": "success"
    }
    """
    try:
        data = request.json or {}
        webhook_url = data.get('webhookUrl')
        
        if not webhook_url:
            return jsonify({
                "code": "01",
                "desc": "webhookUrl is required"
            }), 400
        
        payos = get_payos()
        
        # Confirm webhook với PayOS
        result = payos.webhooks.confirm(webhook_url)
        
        return jsonify({
            "code": "00",
            "desc": "Webhook URL confirmed successfully",
            "data": {
                "webhookUrl": webhook_url
            }
        }), 200
        
    except Exception as e:
        print(f"Confirm Webhook Error: {str(e)}")
        return jsonify({
            "code": "99",
            "desc": f"Error: {str(e)}"
        }), 500


# ==================== LEGACY ENDPOINTS (for backward compatibility) ====================

@PAYMENT_BLUEPRINT.route('/payment-status/<int:order_code>', methods=['GET'])
def get_payment_status_legacy(order_code):
    """
    Legacy API: Kiểm tra trạng thái thanh toán (backward compatible)
    Recommend sử dụng /payment-requests/<order_id> thay thế
    """
    try:
        payos = get_payos()
        payment_info = payos.payment_requests.get(order_code)
        
        return jsonify({
            "error": 0,
            "status": getattr(payment_info, 'status', 'UNKNOWN'),
            "orderCode": order_code,
            "amount": getattr(payment_info, 'amount', 0),
            "amountPaid": getattr(payment_info, 'amountPaid', 0),
            "amountRemaining": getattr(payment_info, 'amountRemaining', 0)
        }), 200
        
    except Exception as e:
        return jsonify({
            "error": 1,
            "message": str(e)
        }), 500


@PAYMENT_BLUEPRINT.route('/cancel-payment/<int:order_code>', methods=['POST'])
def cancel_payment_legacy(order_code):
    """
    Legacy API: Hủy link thanh toán (backward compatible)
    Recommend sử dụng /payment-requests/<order_id>/cancel thay thế
    """
    try:
        payos = get_payos()
        payos.payment_requests.cancel(order_code)
        
        return jsonify({
            "error": 0,
            "message": "Đã hủy link thanh toán",
            "orderCode": order_code
        }), 200
        
    except Exception as e:
        return jsonify({
            "error": 1,
            "message": str(e)
        }), 500
