import re
import difflib
from datetime import datetime

# ---------------- BASIC CATEGORY MAP ----------------
CATEGORY_KEYWORDS = {
    "Food": ["doodh", "milk", "khana", "food", "dahi", "burger", "pizza", "chai", "tea", "restaurant", "lunch", "dinner"],
    "Transport": ["bus", "taxi", "rickshaw", "fuel", "petrol", "uber", "ride", "bike", "car"],
    "Health": ["medicine", "doctor", "clinic", "hospital", "tablet", "medical", "checkup"],
    "Shopping": ["clothes", "shirt", "shoes", "mobile", "shopping", "dress", "watch"],
    "Bills": ["bill", "electric", "internet", "gas", "utility", "recharge"]
}

def extract_amount(text):
    # Remove commas and handle common currency prefixes
    clean_text = text.replace(',', '')
    # Find all numbers (integers or decimals)
    numbers = re.findall(r"\d+(?:\.\d+)?", clean_text)

    if not numbers:
        return 0

    # Logic: In a short expense sentence, the larger number is usually the price
    # e.g., "2 burgers for 500" -> 500 is the amount, not 2.
    float_numbers = [float(n) for n in numbers]
    return max(float_numbers)

def detect_category(text):
    text = text.lower()
    words = text.split()

    # 1. Direct Keyword Match
    for category, keywords in CATEGORY_KEYWORDS.items():
        for word in keywords:
            if word in text:
                return category

    # 2. Fuzzy Match (Similarity check)
    for category, keywords in CATEGORY_KEYWORDS.items():
        for keyword in keywords:
            for word in words:
                similarity = difflib.SequenceMatcher(None, word, keyword).ratio()
                if similarity > 0.8: # 80% similarity
                    return category

    return "Other"

def extract_title(text):
    title_text = re.sub(r'\d+', '', text).strip()
    words = title_text.split()
    if len(words) > 0:
        return " ".join(words[:3]).capitalize()
    return "Expense"

def parse_expense_text(text, email="unknown"):
    try:
        amount = extract_amount(text)
        category = detect_category(text)
        title = extract_title(text)

        if amount <= 0:
            return {"status": "error", "message": "Amount not detected"}

        return {
            "status": "success",
            "email": email,
            "title": title,
            "category": category,
            "amount": amount,
            "date": datetime.now().strftime("%Y-%m-%d"),
            "raw_text": text
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}
