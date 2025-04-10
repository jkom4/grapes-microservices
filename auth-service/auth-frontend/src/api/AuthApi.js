const API_BASE_URL = 'http://localhost:5000/api'; // Changez ceci par l'URL de votre API

export const loginWithEmail = async (email, password) => {
    try {
        const response = await fetch(`${API_BASE_URL}/login/email`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            throw new Error('Failed to log in');
        }

        const data = await response.json();
        return data; // Retourne la réponse API
    } catch (error) {
        console.error('Error logging in:', error);
        throw error;
    }
};

export const loginWithPhone = async (phoneNumber, otp) => {
    try {
        const response = await fetch(`${API_BASE_URL}/login/phone`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ phoneNumber, otp }),
        });

        if (!response.ok) {
            throw new Error('Failed to log in');
        }

        const data = await response.json();
        return data; // Retourne la réponse API
    } catch (error) {
        console.error('Error logging in:', error);
        throw error;
    }
};
