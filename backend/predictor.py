import numpy as np
from datetime import datetime
import calendar

class FinancePredictor:
    def __init__(self, model):
        self.model = model

    def predict_total(self, amounts):
        """
        Calculates a highly accurate monthly prediction.
        Combines Statistical Velocity with ML-based Historical Trends.
        """
        if not amounts:
            return 0.0

        now = datetime.now()
        day_of_month = now.day
        days_in_month = calendar.monthrange(now.year, now.month)[1]

        current_total = sum(amounts)
        avg_per_day = current_total / day_of_month

        # 1. Statistical Projection (Linear Velocity)
        linear_projection = avg_per_day * days_in_month

        # 2. ML-Based Adjustment (XGBoost/Linear Simulation)
        try:
            # We look at the average of the last 10 transactions to see current 'heat'
            recent_avg = np.mean(amounts[-10:]) if len(amounts) >= 10 else np.mean(amounts)

            # Use ML model to predict what the monthly average SHOULD be based on history
            ml_expected_avg = self.model.predict([[recent_avg]])[0]

            # Forecast remaining days using ML suggested rate
            remaining_days = days_in_month - day_of_month
            ml_contribution = current_total + (ml_expected_avg * remaining_days / 7) # assuming weekly cycle

            # Hybrid: 70% ML Insight + 30% Current Velocity
            final_prediction = (0.7 * ml_contribution) + (0.3 * linear_projection)
        except:
            final_prediction = linear_projection

        return round(float(final_prediction), 2)

    def calculate_trend(self, predicted_total, amounts):
        """
        Compares predicted total against historical average.
        """
        if not amounts: return "+0%"
        historical_avg = np.mean(amounts) * 30 / 7 # Approximate monthly

        diff_pct = ((predicted_total - historical_avg) / historical_avg) * 100
        sign = "+" if diff_pct >= 0 else ""
        return f"{sign}{round(diff_pct, 1)}%"

    def generate_insights(self, amounts, predicted_total):
        """
        Generates AI insights that are mathematically consistent with the prediction.
        """
        insights = []
        if not amounts: return insights

        historical_avg = np.mean(amounts) * 30 / 7

        # Prediction-based Warnings
        if predicted_total > historical_avg * 1.25:
            insights.append({
                "type": "warning",
                "message": f"ML Alert: Your predicted spending of {predicted_total} is 25% higher than your normal trend."
            })
        elif predicted_total < historical_avg * 0.9:
            insights.append({
                "type": "suggestion",
                "message": "Efficiency Tip: You are performing better than average. Try to save this surplus!"
            })
        else:
            insights.append({
                "type": "info",
                "message": "Stable Forecast: Your end-of-month spending looks consistent with previous months."
            })

        return insights
