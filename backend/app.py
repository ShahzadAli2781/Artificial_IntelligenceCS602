from flask import Flask, request, jsonify
from flask_cors import CORS
from model_loader import ModelLoader
from predictor import FinancePredictor
from ai_assistant import ai_finance_assistant
import os

app = Flask(__name__)
CORS(app)

# Load model and initialize predictor at startup
model = ModelLoader.load_model()
if model:
    predictor = FinancePredictor(model)
else:
    predictor = None

@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json()
    if not data or 'message' not in data:
        return jsonify({"error": "No message provided"}), 400

    user_message = data.get('message')
    user_data = data.get('userData', {}) # Optional user context (budget, etc.)

    response = ai_finance_assistant(user_message, user_data)
    return jsonify(response)

@app.route('/predict', methods=['POST'])
def predict():
    if predictor is None:
        return jsonify({"error": "Model not loaded on server"}), 500

    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    amounts = data.get('amounts', [])
    user_email = data.get('userEmail', 'unknown')

    if not amounts:
        return jsonify({
            "final_prediction": 0,
            "trend": "0%",
            "model_status": "No data",
            "insights": []
        })

    try:
        final_prediction = predictor.predict_total(amounts)
        previous_avg = sum(amounts) / len(amounts) * 30 / 7 # Simple heuristic
        trend = predictor.calculate_trend(final_prediction, previous_avg)
        insights = predictor.generate_insights(amounts, final_prediction)

        return jsonify({
            "status": "success",
            "final_prediction": final_prediction,
            "xgboost_prediction": final_prediction, # For now same as final
            "trend": trend,
            "transaction_count": len(amounts),
            "model_status": "XGBoost Active",
            "insights": insights
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "model_loaded": predictor is not None})

if __name__ == '__main__':
    # Using 0.0.0.0 to allow access from Android emulator/device in the same network
    app.run(host='0.0.0.0', port=5000, debug=False)
