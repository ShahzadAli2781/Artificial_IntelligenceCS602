import pandas as pd
import numpy as np
from xgboost import XGBRegressor

def train_xgboost(data):

    df = pd.DataFrame(data)

    if df.empty:
        return {"prediction": 0, "data_points": 0}

    # =========================
    # CLEANING
    # =========================
    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df["amount"] = pd.to_numeric(df["amount"], errors="coerce")

    df = df.dropna(subset=["date", "amount"])

    if len(df) < 3:
        return {
            "prediction": float(df["amount"].mean()) if len(df) else 0,
            "data_points": len(df)
        }

    # =========================
    # DAILY AGGREGATION
    # =========================
    daily = df.groupby(df["date"].dt.floor("D")).agg({
        "amount": ["sum", "count", "mean"]
    }).reset_index()

    daily.columns = ["date", "total", "count", "avg"]
    daily = daily.sort_values("date")

    # =========================
    # FEATURE ENGINEERING
    # =========================
    daily["day_index"] = np.arange(len(daily))

    daily["lag_1"] = daily["total"].shift(1)
    daily["lag_2"] = daily["total"].shift(2)

    daily["rolling_3"] = daily["total"].rolling(3).mean()

    # =========================
    # SAFE CLEANING (FIXED)
    # =========================
    daily = daily.replace([np.inf, -np.inf], np.nan)

    numeric_cols = daily.select_dtypes(include=[np.number]).columns
    daily[numeric_cols] = daily[numeric_cols].fillna(0)

    daily = daily.bfill().ffill()

    if len(daily) < 3:
        return {
            "prediction": float(daily["total"].mean()) if len(daily) else 0,
            "data_points": len(daily)
        }

    # =========================
    # MODEL
    # =========================
    model = XGBRegressor(
        n_estimators=200,
        learning_rate=0.05,
        max_depth=4,
        subsample=0.9,
        colsample_bytree=0.9,
        random_state=42
    )

    X = daily[["day_index", "count", "avg", "lag_1", "lag_2", "rolling_3"]]
    y = daily["total"]

    model.fit(X, y)

    # =========================
    # FUTURE PREDICTION
    # =========================
    next_input = np.array([[

        len(daily),
        daily["count"].mean(),
        daily["avg"].mean(),
        daily["total"].iloc[-1],
        daily["total"].iloc[-2] if len(daily) > 1 else daily["total"].iloc[-1],
        daily["total"].tail(3).mean()

    ]])

    pred = model.predict(next_input)[0]

    return {
        "prediction": float(max(pred, 0)),
        "data_points": len(daily)
    }