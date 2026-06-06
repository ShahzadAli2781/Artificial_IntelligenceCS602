import re
import difflib

# ---------------- BASIC CATEGORY MAP----------------
CATEGORY_KEYWORDS = {
    "Food": ["doodh", "milk", "khana", "food", "dahi", "burger", "pizza", "chai", "tea", "restaurant", "lunch", "dinner"],
    "Transport": ["bus", "taxi", "rickshaw", "fuel", "petrol", "uber", "ride", "bike", "car"],
    "Health": ["medicine", "doctor", "clinic", "hospital", "tablet", "medical", "checkup"],
    "Shopping": ["clothes", "shirt", "shoes", "mobile", "shopping", "dress", "watch"],
    "Bills": ["bill", "electric", "internet", "gas", "utility", "recharge"]
}

def extract_amount(text):
    # Support for numbers like "1,000" or "500.50"
    clean_text = text.replace(',', '')
    numbers = re.findall(r"\d+(?:\.\d+)?", clean_text)
    if numbers:
        return float(numbers[0])
    return 0

def detect_category(text):
    text = text.lower()
    words = text.split()
    
    # 1. Direct Keyword Match
    for category, keywords in CATEGORY_KEYWORDS.items():
        for word in keywords:
            if word in text:
                return category
                
    # 2. Fuzzy Match (If spelling is slightly wrong)
    for category, keywords in CATEGORY_KEYWORDS.items():
        for keyword in keywords:
            for word in words:
                # 0.8 means 80% similarity (e.g., "piza" matches "pizza")
                similarity = difflib.SequenceMatcher(None, word, keyword).ratio()
                if similarity > 0.8:
                    return category

    return "Other"

def extract_title(text):
    # Remove amount from title for cleaner look
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
            "raw_text": text
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}