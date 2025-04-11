package grapes.microservices.paymentbackend.dto;

public class LoginResponse {
    private String token;
    private String status;
    private String message;
    private Long userId;

    // Constructeurs
    public LoginResponse() {
    }

    public LoginResponse(String token, String status, String message, Long userId) {
        this.token = token;
        this.status = status;
        this.message = message;
        this.userId = userId;
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}