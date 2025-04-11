import { API_CONFIG } from './config';
//import { PaymentVerification } from '../models/PaymentVerification';

export class VerificationService {
    // Static method to verify payment with OTP
    static async verifyPayment(verification) {
        try {
            // Send verification data to dedicated verification endpoint
            const response = await fetch(`${API_CONFIG.VERIFICATION_BASE_URL}/payment`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(verification.toJSON())  // Convert verification object to JSON
            });

            // Return boolean indicating success/failure
            return response.ok;
        } catch (error) {
            console.error('Error submitting payment:', error);
            // Throw standardized error for consistent handling
            throw new Error('Payment verification failed');
        }
    }
}