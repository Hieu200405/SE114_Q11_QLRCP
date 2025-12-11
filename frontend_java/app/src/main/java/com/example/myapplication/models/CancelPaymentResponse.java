package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

/**
 * Response hủy thanh toán PayOS
 */
public class CancelPaymentResponse {
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("desc")
    private String desc;
    
    @SerializedName("data")
    private CancelPaymentData data;
    
    public boolean isSuccess() {
        return "00".equals(code);
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public CancelPaymentData getData() {
        return data;
    }
    
    public void setData(CancelPaymentData data) {
        this.data = data;
    }
    
    // Inner class for cancel payment data
    public static class CancelPaymentData {
        
        @SerializedName("id")
        private String id;
        
        @SerializedName("orderCode")
        private long orderCode;
        
        @SerializedName("amount")
        private int amount;
        
        @SerializedName("amountPaid")
        private int amountPaid;
        
        @SerializedName("amountRemaining")
        private int amountRemaining;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("canceledAt")
        private String canceledAt;
        
        @SerializedName("cancellationReason")
        private String cancellationReason;
        
        // Getters and Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public long getOrderCode() {
            return orderCode;
        }
        
        public void setOrderCode(long orderCode) {
            this.orderCode = orderCode;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public void setAmount(int amount) {
            this.amount = amount;
        }
        
        public int getAmountPaid() {
            return amountPaid;
        }
        
        public void setAmountPaid(int amountPaid) {
            this.amountPaid = amountPaid;
        }
        
        public int getAmountRemaining() {
            return amountRemaining;
        }
        
        public void setAmountRemaining(int amountRemaining) {
            this.amountRemaining = amountRemaining;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getCanceledAt() {
            return canceledAt;
        }
        
        public void setCanceledAt(String canceledAt) {
            this.canceledAt = canceledAt;
        }
        
        public String getCancellationReason() {
            return cancellationReason;
        }
        
        public void setCancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
        }
    }
}
