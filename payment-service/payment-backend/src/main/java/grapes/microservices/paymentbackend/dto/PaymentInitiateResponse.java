package grapes.microservices.paymentbackend.dto;

import java.math.BigDecimal;
import java.util.Map;
public class PaymentInitiateResponse {



    private String paymentId;
    private String status;
    private String message;
    private Map<String, Object> additionalData;

    // Constructors
    public PaymentInitiateResponse() {
    }

    public PaymentInitiateResponse(String paymentId, String status, String message, Map<String, Object> additionalData) {
        this.paymentId = paymentId;
        this.status = status;
        this.message = message;
        this.additionalData = additionalData;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
}