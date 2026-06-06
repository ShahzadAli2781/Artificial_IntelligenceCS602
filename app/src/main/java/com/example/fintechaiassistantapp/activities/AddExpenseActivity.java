package com.example.fintechaiassistantapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.ml.AiAnalyzer;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.models.IncomeEntity;
import com.example.fintechaiassistantapp.models.OCRExpenseModel;
import com.example.fintechaiassistantapp.models.VoiceExpenseModel;
import com.example.fintechaiassistantapp.ocr.BillParser;
import com.example.fintechaiassistantapp.ocr.OCRManager;
import com.example.fintechaiassistantapp.network.SyncManager;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import com.example.fintechaiassistantapp.voice.SpeechParser;
import com.example.fintechaiassistantapp.voice.VoiceInputManager;
import com.example.fintechaiassistantapp.network.AiExpenseRequest;
import com.example.fintechaiassistantapp.network.AiExpenseResponse;
import com.example.fintechaiassistantapp.network.ApiClient;
import com.example.fintechaiassistantapp.network.ExpenseRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddExpenseActivity extends AppCompatActivity
        implements VoiceInputManager.VoiceInputListener {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 101;
    private static final int PERMISSION_REQUEST_CAMERA = 102;

    private EditText etAmount, etTitle;
    private ChipGroup cgCategory;

    private AppDatabase db;
    private AiAnalyzer aiAnalyzer;
    private SessionManager sessionManager;
    private VoiceInputManager voiceInputManager;
    private OCRManager ocrManager;

    private MaterialButton btnVoice, btnScan;
    private boolean isAiInput = false;
    private String rawAiText = "";
    private boolean isEditMode = false;
    private int expenseId = -1;

    // ================= CAMERA LAUNCHER =================

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getExtras() != null) {

                            Bitmap bitmap =
                                    (Bitmap) result.getData()
                                            .getExtras()
                                            .get("data");

                            if (bitmap != null) {
                                processOCR(bitmap);
                            }
                        }
                    }
            );

    // ================= GALLERY LAUNCHER =================

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            processOCR(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = AppDatabase.getInstance(this);
        aiAnalyzer = new AiAnalyzer(this);
        sessionManager = new SessionManager(this);
        voiceInputManager = new VoiceInputManager(this, this);
        ocrManager = new OCRManager();

        etAmount = findViewById(R.id.et_amount);
        etTitle = findViewById(R.id.et_title);
        cgCategory = findViewById(R.id.cg_category);

        etAmount.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) isAiInput = false; });
        etTitle.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) isAiInput = false; });

        btnVoice = findViewById(R.id.btn_voice);
        btnScan = findViewById(R.id.btn_scan_bill);

        MaterialButton btnSave =
                findViewById(R.id.btn_save_expense);

        MaterialToolbar toolbar =
                findViewById(R.id.toolbar);

        // ================= TOOLBAR =================

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // ================= OCR / INTENT DATA =================

        if (getIntent().hasExtra("is_edit")) {
            isEditMode = true;
            expenseId = getIntent().getIntExtra("expense_id", -1);
            etAmount.setText(String.valueOf(getIntent().getDoubleExtra("amount", 0.0)));
            etTitle.setText(getIntent().getStringExtra("note"));
            String category = getIntent().getStringExtra("category");
            
            if (category != null) {
                for (int i = 0; i < cgCategory.getChildCount(); i++) {
                    Chip chip = (Chip) cgCategory.getChildAt(i);
                    if (chip.getText().toString().equalsIgnoreCase(category)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
            btnSave.setText(R.string.update_expense);
            if (toolbar != null) toolbar.setTitle(R.string.edit_expense);
        } else {
            if (getIntent().hasExtra("amount")) {
                double amount = getIntent().getDoubleExtra("amount", 0.0);
                etAmount.setText(String.valueOf(amount));
            }

            if (getIntent().hasExtra("note")) {
                etTitle.setText(getIntent().getStringExtra("note"));
            }
        }

        // ================= BUTTONS =================

        btnSave.setOnClickListener(v -> saveExpense());

        if (btnVoice != null) {
            btnVoice.setOnClickListener(
                    v -> checkPermissionAndStartVoice()
            );
        }

        if (btnScan != null) {
            btnScan.setOnClickListener(
                    v -> showImageSourceDialog()
            );
        }
    }

    // ================= IMAGE SOURCE =================

    private void showImageSourceDialog() {

        String[] options = {
                getString(R.string.camera),
                getString(R.string.gallery)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_source)
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        checkCameraPermissionAndLaunch();
                    } else {
                        galleryLauncher.launch("image/*");
                    }

                })
                .show();
    }

    // ================= CAMERA =================

    private void checkCameraPermissionAndLaunch() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA
            );

        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraLauncher.launch(intent);
    }

    // ================= OCR PROCESS =================

    private void processOCR(Bitmap bitmap) {

        Toast.makeText(
                this,
                R.string.processing_bill,
                Toast.LENGTH_SHORT
        ).show();

        ocrManager.processImage(bitmap,
                new OCRManager.OCRListener() {

                    @Override
                    public void onSuccess(String text) {
                        handleOCRResult(text);
                    }

                    @Override
                    public void onFailure(Exception e) {

                        Toast.makeText(
                                AddExpenseActivity.this,
                                "OCR Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void processOCR(Uri uri) {

        Toast.makeText(
                this,
                R.string.processing_bill,
                Toast.LENGTH_SHORT
        ).show();

        ocrManager.processImage(this, uri,
                new OCRManager.OCRListener() {

                    @Override
                    public void onSuccess(String text) {
                        handleOCRResult(text);
                    }

                    @Override
                    public void onFailure(Exception e) {

                        Toast.makeText(
                                AddExpenseActivity.this,
                                "OCR Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // ================= OCR RESULT =================

    private void handleOCRResult(String text) {
        isAiInput = true;
        rawAiText = text;

        if (text == null || text.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    R.string.no_text_found,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        OCRExpenseModel model = BillParser.parse(text);

        if (model != null) {

            runOnUiThread(() -> {

                etAmount.setText(
                        String.format(
                                Locale.US,
                                "%.2f",
                                model.getAmount()
                        )
                );

                etTitle.setText(model.getTitle());

                // Auto-select category
                for (int i = 0; i < cgCategory.getChildCount(); i++) {

                    Chip chip =
                            (Chip) cgCategory.getChildAt(i);

                    if (chip.getText()
                            .toString()
                            .equalsIgnoreCase(model.getCategory())) {

                        chip.setChecked(true);
                        break;
                    }
                }
            });
        }
    }

    // ================= VOICE =================

    private void checkPermissionAndStartVoice() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO
            );

        } else {
            voiceInputManager.startListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                voiceInputManager.startListening();

            } else {

                Toast.makeText(
                        this,
                        R.string.permission_denied_audio,
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else if (requestCode == PERMISSION_REQUEST_CAMERA) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                launchCamera();

            } else {

                Toast.makeText(
                        this,
                        R.string.permission_denied_camera,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    // ================= VOICE CALLBACKS =================

    @Override
    public void onReadyForSpeech() {

        btnVoice.setText(R.string.listening);

        btnVoice.setIconResource(
                android.R.drawable.presence_audio_online
        );
    }

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech() {

        btnVoice.setText(R.string.voice);

        btnVoice.setIconResource(
                android.R.drawable.ic_btn_speak_now
        );
    }

    @Override
    public void onError(String error) {

        btnVoice.setText(R.string.voice);

        btnVoice.setIconResource(
                android.R.drawable.ic_btn_speak_now
        );

        Toast.makeText(
                this,
                error,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onResults(String result) {
        isAiInput = true;
        rawAiText = result;

        VoiceExpenseModel model =
                SpeechParser.parse(result);

        if (model != null) {

            etAmount.setText(
                    String.valueOf(model.getAmount())
            );

            etTitle.setText(model.getRawText());

            // Auto-select category
            for (int i = 0; i < cgCategory.getChildCount(); i++) {

                Chip chip =
                        (Chip) cgCategory.getChildAt(i);

                if (chip.getText()
                        .toString()
                        .equalsIgnoreCase(model.getCategory())) {

                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (voiceInputManager != null) {
            voiceInputManager.shutdown();
        }
    }

    // ================= SAVE EXPENSE =================

    private void saveExpense() {

        String amountStr =
                etAmount.getText().toString().trim();

        String title =
                etTitle.getText().toString().trim();

        int checkedChipId =
                cgCategory.getCheckedChipId();

        if (amountStr.isEmpty()
                || title.isEmpty()
                || checkedChipId == -1) {

            Toast.makeText(
                    this,
                    R.string.fill_all_fields,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        double amount =
                Double.parseDouble(amountStr);

        ThreadManager.runInBackground(() -> {

            String userEmail =
                    sessionManager.getUserEmail();

            // Check income first
            IncomeEntity latestIncome =
                    db.incomeDao().getLatestIncomeSync(userEmail);

            if (latestIncome == null
                    && sessionManager.getMonthlyIncome() <= 0) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            R.string.income_required,
                            Toast.LENGTH_LONG
                    ).show();

                    startActivity(
                            new Intent(
                                    this,
                                    AddIncomeActivity.class
                            )
                    );

                    finish();
                });

                return;
            }

            Chip selectedChip = findViewById(checkedChipId);
            if (selectedChip == null) return;

            String category = "Other";
            if (selectedChip.getText() != null) {
                category = selectedChip.getText().toString();
            }

            ExpenseEntity expense;
            if (isEditMode) {
                expense = db.expenseDao().getExpenseByIdSync(expenseId);
                if (expense != null) {
                    expense.setTitle(title);
                    expense.setAmount(amount);
                    expense.setCategory(category);
                    expense.setSynced(false); // Reset sync status on edit
                    db.expenseDao().updateExpense(expense);
                }
            } else {
                // ML-Friendly Date Format
                String date = new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

                long timestamp =
                        System.currentTimeMillis();

                expense =
                        new ExpenseEntity(
                                title,
                                amount,
                                category,
                                date,
                                timestamp,
                                userEmail
                        );

                // Save Expense
                db.expenseDao().insertExpense(expense);
            }
            
            // Schedule background sync
            SyncManager.scheduleSync(this);

            // Run AI Analysis
            aiAnalyzer.analyzeAndGenerateInsights();

            runOnUiThread(() -> {

                Toast.makeText(
                        AddExpenseActivity.this,
                        R.string.expense_saved,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });

        });
    }
}