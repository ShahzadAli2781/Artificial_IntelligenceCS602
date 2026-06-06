import re
from datetime import datetime

def ai_finance_assistant(text, user_data=None):
    text = text.lower()
    response = {
        "status": "success",
        "intent": "chat",
        "message": "",
        "advice": "",
        "extracted": {}
    }

    # 1. Check for Greetings
    if any(greet in text for greet in ["hi", "hello", "salaam", "hey"]):
        response["message"] = "Assalam-o-Alaikum! Main aapka FinIntelligence Assistant hoon. Aap apna kharcha bata sakte hain ya mujhse financial advice le sakte hain."
        return response

    # 2. Expense Detection logic (using simple logic or calling nlp_parser)
    amount_match = re.search(r'(\d+)', text)
    amount = float(amount_match.group(1)) if amount_match else 0

    if amount > 0:
        response["intent"] = "expense_detected"
        # Category detection (simplified for assistant)
        category = "Other"
        if "food" in text or "khana" in text: category = "Food"
        elif "fuel" in text or "petrol" in text: category = "Transport"
        
        response["extracted"] = {
            "amount": amount,
            "category": category,
            "date": datetime.now().strftime("%Y-%m-%d")
        }
        
        # --- SMART ADVICE LOGIC ---
        if user_data:
            budget = user_data.get("monthly_budget", 0)
            spent_so_far = user_data.get("total_spent", 0)
            
            if budget > 0 and (spent_so_far + amount) > budget:
                response["advice"] = f"⚠️ Alert: Is kharche ke baad aapka monthly budget (PKR {budget}) exceed ho jayega. Thora ehtiyat karein!"
            elif amount > user_data.get("avg_spending", 0) * 1.5:
                response["advice"] = "💡 Ye expense aapke aam kharchon se kafi zyada hai. Kya ye zaroori tha?"
            else:
                response["advice"] = "✅ Ye aapke budget ke mutabiq hai. Good job!"
        else:
            response["advice"] = "Expense note kar liya gaya hai. Behtar advice ke liye apna profile data complete karein."
            
        response["message"] = f"Theek hai, main ne PKR {amount} ka kharcha {category} mein detect kiya hai."

    # 3. Financial Tips (If user asks for help/advice)
    elif any(word in text for word in ["help", "advice", "tips", "saving"]):
        response["message"] = "Mera mashwara hai ke aap 50/30/20 rule follow karein: 50% Needs, 30% Wants, aur 20% Savings."
        response["advice"] = "Aap is mahine Food par 15% bacha sakte hain agar aap bahar ka khana kam karein."

    else:
        response["message"] = "Main filhal sirf expenses samajh sakta hoon. Maslan: 'Spent 500 on dinner'."

    return response