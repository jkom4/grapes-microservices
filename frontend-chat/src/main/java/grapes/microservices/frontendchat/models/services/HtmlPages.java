package grapes.microservices.frontendchat.models.services;

public class HtmlPages {
    public static String AUTH_SUCCESS_PAGE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title>Authentication Success</title>
                <style>
                    body {
                        background-color: #f7f9fc;
                        color: #333;
                        font-family: Arial, sans-serif;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                    }
                    .container {
                        text-align: center;
                        background: #fff;
                        padding: 2rem 3rem;
                        border-radius: 8px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                    }
                    h1 {
                        font-size: 1.75rem;
                        margin-bottom: 1rem;
                    }
                    p {
                        margin-bottom: 1.5rem;
                        font-size: 1rem;
                    }
                </style>
            </head>
            <body>
            <div class="container">
                <h1>Authenticated Successfully!</h1>
                <p>You've been authenticated. Return to the Grapes Support app.</p>
                <img src="https://upload.wikimedia.org/wikipedia/commons/c/c6/Sign-check-icon.png" alt="Check Icon" style="width:150px;height:auto;">
            </div>
            </body>
            </html>
            """;
}
