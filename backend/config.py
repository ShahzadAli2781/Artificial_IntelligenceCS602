import os
from dotenv import load_dotenv
from pymongo import MongoClient

load_dotenv()

# MongoDB Atlas Connection
MONGO_URI = os.getenv("MONGO_URI", "mongodb+srv://admin:admin@cluster0.mongodb.net/finintelligence?retryWrites=true&w=majority")

client = MongoClient(MONGO_URI)
db = client["finintelligence"]
expenses_collection = db["expenses"]
