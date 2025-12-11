package com.example.myapplication.network;

import com.example.myapplication.models.CreatePaymentRequest;
import com.example.myapplication.models.CreatePaymentResponse;
import com.example.myapplication.models.PaymentStatusResponse;
import com.example.myapplication.models.CancelPaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * API Service cho thanh toán qua PayOS
 */
public interface ApiPaymentService {
    
    /**
     * Tạo link thanh toán PayOS
     * @param token Bearer token
     * @param request Thông tin thanh toán
     * @return Response chứa checkoutUrl
     */
    @POST("payment/create-payment")
    Call<CreatePaymentResponse> createPayment(
            @Header("Authorization") String token,
            @Body CreatePaymentRequest request
    );
    
    /**
     * Lấy thông tin trạng thái thanh toán
     * @param token Bearer token
     * @param orderId Mã đơn hàng
     * @return Response trạng thái thanh toán
     */
    @GET("payment/payment-requests/{orderId}")
    Call<PaymentStatusResponse> getPaymentStatus(
            @Header("Authorization") String token,
            @Path("orderId") long orderId
    );
    
    /**
     * Hủy link thanh toán
     * @param token Bearer token
     * @param orderId Mã đơn hàng
     * @return Response hủy thanh toán
     */
    @POST("payment/payment-requests/{orderId}/cancel")
    Call<CancelPaymentResponse> cancelPayment(
            @Header("Authorization") String token,
            @Path("orderId") long orderId
    );
}
