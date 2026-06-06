import re
from datetime import datetime
from nlp_parser import extract_amount, detect_category

def ai_finance_assistant(text, user_data=None):
    text_lower = text.lower()
    response = {
        "status": "success",
        "intent": "chat",
        "message": "Main aapki spending analyze kar sakta hoon. Expense bolo ya 'tips' likho.",
        "advice": "",
        "extracted": {}
    }

    # 1. Greetings (Urdu/English Support)
    if any(greet in text_lower for greet in ["hi", "hello", "salaam", "hey", "aoa", "assalam"]):
        response["message"] = "Assalam-o-Alaikum! Main aapka FinIntelligence AI Assistant hoon. Aap apna kharcha bata sakte hain (e.g., '1000 for dinner') ya financial advice le sakte hain."
        return response

    # 2. Expense Detection using the improved NLP logic
    amount = extract_amount(text)
    if amount > 0:
        response["intent"] = "expense_detected"
        category = detect_category(text)

        response["extracted"] = {
            "amount": amount,
            "category": category,
            "title": text[:30].capitalize(),
            "date": datetime.now().strftime("%Y-%m-%d")
        }
        response["message"] = f"Theek hai, main ne {amount} PKR ka kharcha {category} mein note kar liya hai."

        # SMART ADVICE (Context aware)
        if user_data:
            budget = user_data.get("monthly_budget", 0)
            spent = user_data.get("total_spent", 0)
            avg = user_data.get("avg_spending", 0)

            if budget > 0 and (spent + amount) > budget:
                response["advice"] = "⚠️ Alert: Aapka budget khatam ho raha hai. Thora ehtiyat karein!"
            elif amount > avg * 1.5 and avg > 0:
                response["advice"] = "💡 Ye kharcha aapke routine se zyada hai. Kya ye zaroori tha?"
            else:
                response["advice"] = "✅ Acha control hai! Aap apne budget ke mutabiq chal rahe hain."
        return response

    # 3. Help/Tips
    if any(word in text_lower for word in ["help", "advice", "tips", "saving", "bachat"]):
        response["message"] = "Hamesha yaad rakhein: 'Pehle save karein, phir kharch karein'. 50/30/20 rule best hai."
        response["advice"] = "💡 Smart Tip: Rozana ke chote kharche (jaise chai, snacks) mahine ke akhir mein bara amount ban jate hain."

    return response
