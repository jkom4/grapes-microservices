from flask import Flask
from app.routes import api

app = Flask(__name__)

# Enregistrement des routes
api.init_app(app)

@app.route("/")
def home():
    return {"message": "Bienvenue sur mon microservice Flask !"}

if __name__ == "__main__":
    app.run(debug=True)
