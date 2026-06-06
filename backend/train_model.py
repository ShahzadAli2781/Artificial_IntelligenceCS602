import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
import joblib
import os

def train_and_save():
    # Load dataset
    if not os.path.exists('dataset.csv'):
        print("Dataset not found. Creating dummy data...")
        # Create a dummy dataset if not exists for initial training
        data = {
            'amount': [100, 200, 300, 400, 500, 600, 700, 800],
            'target': [110, 215, 305, 420, 510, 630, 715, 840]
        }
        df = pd.DataFrame(data)
    else:
        # For actual implementation, we might want to group by time/user
        # but for Phase 10 we'll use a simple regression on historical trends
        df = pd.read_csv('dataset.csv')
        # Simple simulation: predict next spending based on previous total trend
        df['target'] = df['amount'] * 1.05 # Dummy logic for training sample

    X = df[['amount']].values
    y = df['target'].values

    model = LinearRegression()
    model.fit(X, y)

    # Ensure models directory exists
    if not os.path.exists('models'):
        os.makedirs('models')

    joblib.dump(model, 'models/finance_model.pkl')
    print("Model trained and saved to models/finance_model.pkl")

if __name__ == "__main__":
    train_and_save()
