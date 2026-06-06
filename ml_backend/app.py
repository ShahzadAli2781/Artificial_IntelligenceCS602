from flask import Flask, jsonify, request
from flask_cors import CORS
from config import expenses_collection
from xgboost_model import train_xgboost
from nlp_parser import parse_expense_text
from ai_assistant import ai_finance_assistant   # ✅ IMPORTANT FIX

app = Flask(__name__)
CORS(app)

# =========================================================
# 🏠 HOME
# =========================================================
@app.route('/')
def home():
    return jsonify({
        "status": "success",
        "message": "FinIntelligence Backend Running"
    })


# =========================================================
# 🧾 1. MANUAL EXPENSE ENTRY
# =========================================================
@app.route('/add_expense', methods=['POST'])
def add_expense():
    try:
        data = request.get_json(force=True)

        email = data.get("email") or data.get("userEmail")
        title = data.get("title", "No Title")
        category = data.get("category", "Other")
        date = data.get("date", "")

        try:
            amount = float(data.get("amount", 0))
        except:
            amount = 0.0

        if not email or amount <= 0:
            return jsonify({
                "status": "error",
                "message": "Invalid input"
            }), 400

        expense = {
            "email": email,
            "title": title,
            "category": category,
            "amount": amount,
            "date": date
        }

        expenses_collection.insert_one(expense)

        return jsonify({
            "status": "success",
            "message": "Expense saved (manual)"
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500


# =========================================================
# 🚀 2. FULL AI AUTO EXPENSE
# =========================================================
@app.route('/add_expense_auto', methods=['POST'])
def add_expense_auto():
    try:
        data = request.get_json(force=True)

        text = data.get("text", "")
        email = data.get("email", "")

        if not text or not email:
            return jsonify({
                "status": "error",
                "message": "Text and email required"
            }), 400

        parsed = parse_expense_text(text, email)

        if parsed.get("status") != "success":
            return jsonify(parsed), 400

        expense = {
            "email": email,
            "title": parsed.get("title"),
            "category": parsed.get("category"),
            "amount": parsed.get("amount"),
            "date": parsed.get("date")
        }

        expenses_collection.insert_one(expense)

        return jsonify({
            "status": "success",
            "message": "Expense added via AI",
            "data": expense
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500


# =========================================================
# 🧠 3. NLP PARSER ONLY
# =========================================================
@app.route('/parse_expense', methods=['POST'])
def parse_expense():
    try:
        data = request.get_json(force=True)

        text = data.get("text", "")
        email = data.get("email", "")

        result = parse_expense_text(text, email)

        return jsonify(result)

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500


# =========================================================
# 💬 4. AI CHAT ENDPOINT (FIXED)
# =========================================================
@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json(force=True)

        text = data.get("text", "")
        user_data = data.get("user_data", None)

        result = ai_finance_assistant(text, user_data)

        return jsonify(result)

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500


# =========================================================
# 📊 5. PREDICTION (XGBOOST ONLY)
# =========================================================
@app.route('/predict/<email>', methods=['GET'])
def predict(email):

    try:
        data = list(expenses_collection.find({"email": email}))
        transaction_count = len(data)

        if not data:
            return jsonify({
                "status": "success",
                "final_prediction": 0,
                "xgboost_prediction": 0,
                "transaction_count": 0,
                "data_points": 0,
                "model_status": "XGBoost Only",
                "message": "No data found"
            })

        xgb_result = train_xgboost(data)
        prediction = float(xgb_result.get("prediction", 0))

        return jsonify({
            "status": "success",
            "final_prediction": round(prediction, 2),
            "xgboost_prediction": round(prediction, 2),
            "transaction_count": transaction_count,
            "data_points": xgb_result.get("data_points", transaction_count),
            "model_status": "XGBoost Only"
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500


# =========================================================
# 🚀 RUN SERVER
# =========================================================
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)