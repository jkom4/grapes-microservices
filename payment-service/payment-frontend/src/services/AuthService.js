// src/services/AuthService.js

import axios from 'axios';

// Configuration de base
const API_BASE_URL = 'http://localhost:8043'; // Ajustez selon l'URL de votre backend Spring Boot

export class AuthService {
    /**
     * Méthode pour se connecter à l'application
     * @param {User} user - L'objet utilisateur contenant login et password
     * @returns {Promise<Object>} Résultat de la connexion
     */
    static async login(user) {
        try {
            console.log(`Envoi de la requête d'authentification à ${API_BASE_URL}/api/login`);

            const response = await axios.post(`${API_BASE_URL}/api/login`, {
                login: user.login,
                password: user.password
            });

            // Traiter la réponse
            if (response.status === 200) {
                console.log('Authentification réussie:', response.data);

                // Stocker le token de session et l'ID de l'utilisateur
                localStorage.setItem('sessionToken', response.data.token);
                localStorage.setItem('userId', response.data.userId);

                return {
                    success: true,
                    redirectUrl: '/dashboard', // URL de redirection après connexion réussie
                    userId: response.data.userId
                };
            } else {
                return {
                    success: false,
                    error: response.data.message || 'Authentication failed'
                };
            }
        } catch (error) {
            console.error('Login error:', error);

            // Gérer les différents types d'erreurs
            if (error.response) {
                // La requête a été faite et le serveur a répondu avec un code d'erreur
                return {
                    success: false,
                    error: error.response.data.message || 'Authentication failed'
                };
            } else if (error.request) {
                // La requête a été faite mais pas de réponse reçue
                return {
                    success: false,
                    error: 'No response from server. Please try again later.'
                };
            } else {
                // Une erreur s'est produite lors de la configuration de la requête
                return {
                    success: false,
                    error: 'Connection error. Please check your network.'
                };
            }
        }
    }

    /**
     * Vérifie si l'utilisateur est connecté
     * @returns {boolean} État de connexion
     */
    static isLoggedIn() {
        return localStorage.getItem('sessionToken') !== null;
    }

    /**
     * Déconnecte l'utilisateur
     */
    static logout() {
        localStorage.removeItem('sessionToken');
        localStorage.removeItem('userId');
        window.location.href = '/login';
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     * @returns {string|null} ID de l'utilisateur
     */
    static getUserId() {
        return localStorage.getItem('userId');
    }

    /**
     * Récupère le token de session
     * @returns {string|null} Token de session
     */
    static getToken() {
        return localStorage.getItem('sessionToken');
    }
}