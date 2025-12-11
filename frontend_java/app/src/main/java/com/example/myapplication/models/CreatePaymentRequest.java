package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request tạo thanh toán PayOS
 */
public class CreatePaymentRequest {
    
    @SerializedName("orderCode")
    private Long orderCode;
    
    @SerializedName("amount")
    private int amount;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("buyerName")
    private String buyerName;
    
    @SerializedName("buyerEmail")
    private String buyerEmail;
    
    @SerializedName("buyerPhone")
    private String buyerPhone;
    
    @SerializedName("buyerAddress")
    private String buyerAddress;
    
    @SerializedName("items")
    private List<PaymentItem> items;
    
    @SerializedName("cancelUrl")
    private String cancelUrl;
    
    @SerializedName("returnUrl")
    private String returnUrl;
    
    @SerializedName("expiredAt")
    private Long expiredAt;
    
    // Thông tin bổ sung để lưu tạm trước khi thanh toán
    @SerializedName("broadcastId")
    private int broadcastId;
    
    @SerializedName("seatId")
    private int seatId;
    
    public CreatePaymentRequest() {}
    
    public CreatePaymentRequest(int amount, String description) {
        this.amount = amount;
        this.description = description;
    }
    
    // Builder pattern for easier construction
    public static class Builder {
        private CreatePaymentRequest request = new CreatePaymentRequest();
        
        public Builder orderCode(Long orderCode) {
            request.orderCode = orderCode;
            return this;
        }
        
        public Builder amount(int amount) {
            request.amount = amount;
            return this;
        }
        
        public Builder description(String description) {
            request.description = description;
            return this;
        }
        
        public Builder buyerName(String buyerName) {
            request.buyerName = buyerName;
            return this;
        }
        
        public Builder buyerEmail(String buyerEmail) {
            request.buyerEmail = buyerEmail;
            return this;
        }
        
        public Builder buyerPhone(String buyerPhone) {
            request.buyerPhone = buyerPhone;
            return this;
        }
        
        public Builder buyerAddress(String buyerAddress) {
            request.buyerAddress = buyerAddress;
            return this;
        }
        
        public Builder items(List<PaymentItem> items) {
            request.items = items;
            return this;
        }
        
        public Builder cancelUrl(String cancelUrl) {
            request.cancelUrl = cancelUrl;
            return this;
        }
        
        public Builder returnUrl(String returnUrl) {
            request.returnUrl = returnUrl;
            return this;
        }
        
        public Builder expiredAt(Long expiredAt) {
            request.expiredAt = expiredAt;
            return this;
        }
        
        public Builder broadcastId(int broadcastId) {
            request.broadcastId = broadcastId;
            return this;
        }
        
        public Builder seatId(int seatId) {
            request.seatId = seatId;
            return this;
        }
        
        public CreatePaymentRequest build() {
            return request;
        }
    }
    
    // Getters and Setters
    public Long getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }
    
    public String getBuyerEmail() {
        return buyerEmail;
    }
    
    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }
    
    public String getBuyerPhone() {
        return buyerPhone;
    }
    
    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }
    
    public String getBuyerAddress() {
        return buyerAddress;
    }
    
    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }
    
    public List<PaymentItem> getItems() {
        return items;
    }
    
    public void setItems(List<PaymentItem> items) {
        this.items = items;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public Long getExpiredAt() {
        return expiredAt;
    }
    
    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }
    
    public int getBroadcastId() {
        return broadcastId;
    }
    
    public void setBroadcastId(int broadcastId) {
        this.broadcastId = broadcastId;
    }
    
    public int getSeatId() {
        return seatId;
    }
    
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
    
    // Inner class for payment items
    public static class PaymentItem {
        @SerializedName("name")
        private String name;
        
        @SerializedName("quantity")
        private int quantity;
        
        @SerializedName("price")
        private int price;
        
        public PaymentItem() {}
        
        public PaymentItem(String name, int quantity, int price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public int getPrice() {
            return price;
        }
        
        public void setPrice(int price) {
            this.price = price;
        }
    }
}
