package com.example.fintechaiassistantapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.ml.AiAnalyzer;
import com.example.fintechaiassistantapp.models.IncomeEntity;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {
    private TextInputEditText etIncomeAmount;
    private MaterialButton btnSaveIncome;
    private AppDatabase db;
    private SessionManager sessionManager;
    private AiAnalyzer aiAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);
        aiAnalyzer = new AiAnalyzer(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etIncomeAmount = findViewById(R.id.et_income_amount);
        btnSaveIncome = findViewById(R.id.btn_save_income);

        btnSaveIncome.setOnClickListener(v -> {
            String amountStr = etIncomeAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter income amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String month = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
            long timestamp = System.currentTimeMillis();
            String userEmail = sessionManager.getUserEmail();

            IncomeEntity income = new IncomeEntity(amount, timestamp, month, userEmail);

            ThreadManager.runInBackground(() -> {
                db.incomeDao().insertIncome(income);
                sessionManager.setMonthlyIncome((float) amount);
                aiAnalyzer.analyzeAndGenerateInsights();
                runOnUiThread(() -> {
                    Toast.makeText(AddIncomeActivity.this, "Income updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}