package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response trạng thái thanh toán PayOS
 */
public class PaymentStatusResponse {
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("desc")
    private String desc;
    
    @SerializedName("data")
    private PaymentStatusData data;
    
    public boolean isSuccess() {
        return "00".equals(code);
    }
    
    public boolean isPaid() {
        return data != null && "PAID".equals(data.getStatus());
    }
    
    public boolean isPending() {
        return data != null && "PENDING".equals(data.getStatus());
    }
    
    public boolean isCancelled() {
        return data != null && "CANCELLED".equals(data.getStatus());
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
    
    public PaymentStatusData getData() {
        return data;
    }
    
    public void setData(PaymentStatusData data) {
        this.data = data;
    }
    
    // Inner class for payment status data
    public static class PaymentStatusData {
        
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
        
        @SerializedName("createdAt")
        private String createdAt;
        
        @SerializedName("transactions")
        private List<Transaction> transactions;
        
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
        
        public String getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
        
        public List<Transaction> getTransactions() {
            return transactions;
        }
        
        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
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
    
    // Inner class for transaction
    public static class Transaction {
        
        @SerializedName("reference")
        private String reference;
        
        @SerializedName("amount")
        private int amount;
        
        @SerializedName("accountNumber")
        private String accountNumber;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("transactionDateTime")
        private String transactionDateTime;
        
        @SerializedName("virtualAccountName")
        private String virtualAccountName;
        
        @SerializedName("virtualAccountNumber")
        private String virtualAccountNumber;
        
        @SerializedName("counterAccountBankId")
        private String counterAccountBankId;
        
        @SerializedName("counterAccountBankName")
        private String counterAccountBankName;
        
        @SerializedName("counterAccountName")
        private String counterAccountName;
        
        @SerializedName("counterAccountNumber")
        private String counterAccountNumber;
        
        // Getters and Setters
        public String getReference() {
            return reference;
        }
        
        public void setReference(String reference) {
            this.reference = reference;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public void setAmount(int amount) {
            this.amount = amount;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
        
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getTransactionDateTime() {
            return transactionDateTime;
        }
        
        public void setTransactionDateTime(String transactionDateTime) {
            this.transactionDateTime = transactionDateTime;
        }
        
        public String getVirtualAccountName() {
            return virtualAccountName;
        }
        
        public void setVirtualAccountName(String virtualAccountName) {
            this.virtualAccountName = virtualAccountName;
        }
        
        public String getVirtualAccountNumber() {
            return virtualAccountNumber;
        }
        
        public void setVirtualAccountNumber(String virtualAccountNumber) {
            this.virtualAccountNumber = virtualAccountNumber;
        }
        
        public String getCounterAccountBankId() {
            return counterAccountBankId;
        }
        
        public void setCounterAccountBankId(String counterAccountBankId) {
            this.counterAccountBankId = counterAccountBankId;
        }
        
        public String getCounterAccountBankName() {
            return counterAccountBankName;
        }
        
        public void setCounterAccountBankName(String counterAccountBankName) {
            this.counterAccountBankName = counterAccountBankName;
        }
        
        public String getCounterAccountName() {
            return counterAccountName;
        }
        
        public void setCounterAccountName(String counterAccountName) {
            this.counterAccountName = counterAccountName;
        }
        
        public String getCounterAccountNumber() {
            return counterAccountNumber;
        }
        
        public void setCounterAccountNumber(String counterAccountNumber) {
            this.counterAccountNumber = counterAccountNumber;
        }
    }
}
