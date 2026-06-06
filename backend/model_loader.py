import joblib
import os

class ModelLoader:
    _model = None

    @classmethod
    def load_model(cls):
        if cls._model is None:
            model_path = 'models/finance_model.pkl'
            if os.path.exists(model_path):
                cls._model = joblib.load(model_path)
                print(f"Model loaded successfully from {model_path}")
            else:
                print("Model file not found. Please run train_model.py first.")
                cls._model = None
        return cls._model
