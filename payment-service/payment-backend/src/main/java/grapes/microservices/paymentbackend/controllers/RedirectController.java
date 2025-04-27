package grapes.microservices.paymentbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling redirects for payment flow.
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    /**
     * Handles redirect requests to the login page, typically after payment initiation.
     * Logs the payment ID if provided but relies primarily on session data.
     *
     * @param paymentId Optional payment identifier
     * @param request The HTTP request to get the referring URL
     * @return ResponseEntity with redirect headers to the login page
     */
    @GetMapping("/redirect")
    public ResponseEntity<?> redirectToLogin(@RequestParam(required = false) String paymentId,
                                             HttpServletRequest request) {
        if (paymentId != null) {
            log.warn("[RedirectController] Received redirect request for paymentId {}, but session should handle context", paymentId);
        } else {
            log.info("[RedirectController] Received redirect request without paymentId");
        }

        // Get redirect URL from the request
        String redirectUrl = request.getHeader("Referer");
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            redirectUrl = "/login";
        } else if (!redirectUrl.endsWith("/login")) {
            redirectUrl = redirectUrl + "/login";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}