package grapes.microservices.paymentbackend.dto;

import java.math.BigDecimal;

public class PaymentInitiateRequest {
    private BigDecimal amount;
    private String clientId;

    // Constructeurs
    public PaymentInitiateRequest() {
    }

    public PaymentInitiateRequest(BigDecimal amount, String clientId) {
        this.amount = amount;
        this.clientId = clientId;
    }

    // Getters et Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}