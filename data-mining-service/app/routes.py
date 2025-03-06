from flask import request
from flask_restful import Api, Resource

api = Api()

# Définition des routes
class ItemResource(Resource):
    def get(self):
        return {"items": ["item1", "item2", "item3"]}

    def post(self):
        data = request.get_json()
        return {"message": "Item créé", "item": data}, 201

# Ajout des routes à l'API
api.add_resource(ItemResource, "/items")
