package grapes.microservices.paymentbackend.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) containing the information
 * needed to initiate a payment request.
 */
public class PaymentInitiateRequest {

    private BigDecimal amount;
    private String merchantId;

    public PaymentInitiateRequest() {
    }

    public PaymentInitiateRequest(BigDecimal amount, String merchantId) {
        this.amount = amount;
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}