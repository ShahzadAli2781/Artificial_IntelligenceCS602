package com.example.fintechaiassistantapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.adapters.AiChatAdapter;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.models.ChatMessage;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.network.ApiClient;
import com.example.fintechaiassistantapp.network.ChatRequest;
import com.example.fintechaiassistantapp.network.ChatResponse;
import com.example.fintechaiassistantapp.network.SyncManager;
import com.example.fintechaiassistantapp.utils.NetworkUtils;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import com.example.fintechaiassistantapp.voice.VoiceInputManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiAssistantActivity extends AppCompatActivity implements VoiceInputManager.VoiceInputListener {

    private RecyclerView rvChat;
    private AiChatAdapter adapter;
    private List<ChatMessage> messages;
    private EditText etMessage;
    private ImageButton btnSend, btnMic, btnScan;
    private com.google.android.material.chip.Chip chipBudget, chipPredict, chipTips;
    private SessionManager sessionManager;
    private VoiceInputManager voiceInputManager;
    private com.example.fintechaiassistantapp.ocr.OCRManager ocrManager;
    private AppDatabase db;

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 101;
    private static final int PERMISSION_REQUEST_CAMERA = 102;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> cameraLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    android.graphics.Bitmap bitmap = (android.graphics.Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) processOCR(bitmap);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) processOCR(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getInstance(this);
        voiceInputManager = new VoiceInputManager(this, this);
        ocrManager = new com.example.fintechaiassistantapp.ocr.OCRManager();

        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnMic = findViewById(R.id.btn_mic);
        btnScan = findViewById(R.id.btn_scan);
        chipBudget = findViewById(R.id.chip_budget);
        chipPredict = findViewById(R.id.chip_predict);
        chipTips = findViewById(R.id.chip_tips);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        messages = new ArrayList<>();
        adapter = new AiChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        addAiMessage("Hello! I am your AI Finance Assistant. How can I help you today?");

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });

        btnMic.setOnClickListener(v -> checkPermissionAndStartVoice());
        btnScan.setOnClickListener(v -> showImageSourceDialog());

        // Quick Actions Listeners
        chipBudget.setOnClickListener(v -> sendMessage("Check my budget"));
        chipPredict.setOnClickListener(v -> sendMessage("Predict my expenses"));
        chipTips.setOnClickListener(v -> sendMessage("Financial tips"));
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Select Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndLaunch();
                    else galleryLauncher.launch("image/*");
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        android.content.Intent intent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void processOCR(android.graphics.Bitmap bitmap) {
        addAiMessage("Processing image...");
        ocrManager.processImage(bitmap, new com.example.fintechaiassistantapp.ocr.OCRManager.OCRListener() {
            @Override
            public void onSuccess(String text) {
                sendMessage(text);
            }

            @Override
            public void onFailure(Exception e) {
                addAiMessage("OCR failed: " + e.getMessage());
            }
        });
    }

    private void processOCR(android.net.Uri uri) {
        addAiMessage("Processing image...");
        ocrManager.processImage(this, uri, new com.example.fintechaiassistantapp.ocr.OCRManager.OCRListener() {
            @Override
            public void onSuccess(String text) {
                sendMessage(text);
            }

            @Override
            public void onFailure(Exception e) {
                addAiMessage("OCR failed: " + e.getMessage());
            }
        });
    }

    private void sendMessage(String text) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        addUserMessage(text);
        etMessage.setText("");
        
        // Prepare user data for smart advice
        ThreadManager.runInBackground(() -> {
            String email = sessionManager.getUserEmail();
            double totalSpent = db.expenseDao().getTotalExpensesSync(email);
            double avgSpending = db.expenseDao().getAverageExpenseSync(email);
            double monthlyBudget = db.incomeDao().getLatestIncomeAmountSync(email);

            java.util.Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("total_spent", totalSpent);
            userData.put("avg_spending", avgSpending);
            userData.put("monthly_budget", monthlyBudget);

            ChatRequest request = new ChatRequest(text, userData);

            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                sendToChatApi(request);
            });
        });
    }

    private void sendToChatApi(ChatRequest request) {
        // Add Typing Indicator
        addAiMessage("AI is thinking...");
        int typingIndex = messages.size() - 1;

        ApiClient.getApiService().chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                // Remove Typing Indicator
                if (typingIndex < messages.size()) {
                    messages.remove(typingIndex);
                    adapter.notifyItemRemoved(typingIndex);
                }

                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    
                    // Show AI Message
                    addAiMessage(chatResponse.getMessage());

                    // Show AI Advice if present
                    if (chatResponse.getAdvice() != null && !chatResponse.getAdvice().isEmpty()) {
                        addAiMessage(chatResponse.getAdvice());
                    }
                    
                    // Save expense if detected
                    if ("expense_detected".equals(chatResponse.getIntent()) && chatResponse.getExtracted() != null) {
                        saveExpenseFromChat(chatResponse.getExtracted());
                    }
                } else {
                    addAiMessage("Sorry, I couldn't process that request.");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                addAiMessage("Error connecting to server. Please try again.");
            }
        });
    }

    private void saveExpenseFromChat(java.util.Map<String, Object> extracted) {
        ThreadManager.runInBackground(() -> {
            try {
                Object amountObj = extracted.get("amount");
                if (amountObj == null) return;

                double amount;
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).doubleValue();
                } else {
                    amount = Double.parseDouble(String.valueOf(amountObj));
                }

                String title = (String) extracted.get("title");
                String category = (String) extracted.get("category");
                String date = (String) extracted.get("date");

                ExpenseEntity entity = new ExpenseEntity(
                        (title != null && !title.isEmpty()) ? title : "AI Expense",
                        amount,
                        (category != null && !category.isEmpty()) ? category : "Other",
                        (date != null && !date.isEmpty()) ? date : new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()),
                        System.currentTimeMillis(),
                        sessionManager.getUserEmail()
                );
                db.expenseDao().insertExpense(entity);
                SyncManager.scheduleSync(AiAssistantActivity.this);
            } catch (Exception e) {
                android.util.Log.e("AiAssistant", "Error saving expense: " + e.getMessage());
            }
        });
    }

    private void checkPermissionAndStartVoice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        } else {
            voiceInputManager.startListening();
        }
    }

    @Override
    public void onReadyForSpeech() {
        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(String result) {
        if (result != null && !result.isEmpty()) {
            sendMessage(result);
        }
    }

    private void addAiMessage(String text) {
        messages.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.smoothScrollToPosition(messages.size() - 1);
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.smoothScrollToPosition(messages.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceInputManager != null) {
            voiceInputManager.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            voiceInputManager.startListening();
        }
    }
}
