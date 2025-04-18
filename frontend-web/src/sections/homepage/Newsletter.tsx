import React, { useState } from "react";
import NewsletterImage from "../../assets/images/newsletter.png";
import { useLanguage } from "../../features/LanguageContext";
import "react-toastify/dist/ReactToastify.css";
import { toast } from "react-toastify";
import axios from "axios";

// Load the API key from the environment variables
const BREVO_API_KEY = process.env.REACT_APP_BREVO_API_KEY;
const BREVO_LIST_ID = process.env.REACT_APP_BREVO_LIST_ID;

function Newsletter() {
    const { language } = useLanguage();
    const [email, setEmail] = useState<string>("");

    const text = {
        en: {
            header: "Subscribe to Our Newsletter",
            placeholder: "Enter your email",
            button: "Submit",
        },
        fr: {
            header: "Abonnez-vous à notre Newsletter",
            placeholder: "Entrez votre email",
            button: "Envoyer",
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Check if the email is valid
        if (!email) {
            toast.error("Please enter a valid email address!");
            return;
        }

        // Retrieve the subscribers from localStorage
        let subscribers = JSON.parse(localStorage.getItem("subscribers") || "[]") as string[];

        // If the email is already in localStorage, show a message; otherwise, add it to localStorage
        if (subscribers.includes(email)) {
            toast.info("You are already subscribed to the newsletter.");
        } else {
            // Add email to localStorage
            subscribers.push(email);
            localStorage.setItem("subscribers", JSON.stringify(subscribers));
        }

        try {
            // Send the email to Brevo
            const response = await axios.post(
                `https://api.brevo.com/v3/contacts`,
                {
                    email: email,
                    listIds: [parseInt(String(BREVO_LIST_ID))], // List ID for your Brevo list
                },
                {
                    headers: {
                        "api-key": BREVO_API_KEY, // API key from the .env file
                        "Content-Type": "application/json",
                    },
                }
            );

            // Check if the response is successful (status 201)
            if (response.status === 201) {
                toast.success("You are subscribed to the newsletter!");
            } else {
                toast.error(`Failed to add email to Brevo: ${response.statusText}`);
            }
        } catch (error) {
            toast.error("Error adding email to Brevo.");
        }

        // Reset the input field
        setEmail("");
    };

    return (
        <section className="bg-white flex justify-center items-center min-h-[300px] py-10 mt-14">
            <div className="relative w-4/5 text-center">
                <div className="w-full h-[300px] overflow-hidden rounded-[100px] mb-10">
                    <img
                        src={NewsletterImage}
                        alt="Newsletter"
                        className="w-full h-full object-cover rounded-lg"
                    />
                </div>
                <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full">
                    <h2 className="text-white font-semibold text-2xl">{text[language].header}</h2>
                    <input
                        type="email"
                        placeholder={text[language].placeholder}
                        className="mt-4 p-3 text-base rounded-[20px_0_0_20px] w-[300px] focus:outline-none"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <button
                        onClick={handleSubmit}
                        className="mt-4 py-3 px-5 font-semibold text-base text-white bg-secondary rounded-[0_20px_20px_0] hover:bg-accent"
                    >
                        {text[language].button}
                    </button>
                </div>
            </div>
        </section>
    );
}

export default Newsletter;
