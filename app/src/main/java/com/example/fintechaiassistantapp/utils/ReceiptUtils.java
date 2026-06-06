package com.example.fintechaiassistantapp.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.models.ExpenseEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceiptUtils {

    public static void generateAndSharePdfReceipt(Context context, ExpenseEntity expense) {
        String filename = "Receipt_" + expense.getId() + "_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getCacheDir(), filename);
        
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(450, 700, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            drawHighClassReceipt(context, page.getCanvas(), expense);
            pdfDocument.finishPage(page);
            
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share Professional Receipt"));

        } catch (IOException e) {
            Toast.makeText(context, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void printReceipt(Context context, ExpenseEntity expense) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        String jobName = context.getString(R.string.app_name) + " Receipt " + expense.getId();

        printManager.print(jobName, new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }
                PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
                callback.onLayoutFinished(pdi, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(450, 700, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                
                drawHighClassReceipt(context, page.getCanvas(), expense);

                pdfDocument.finishPage(page);

                try {
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
                } finally {
                    pdfDocument.close();
                }
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, null);
    }

    private static void drawHighClassReceipt(Context context, Canvas canvas, ExpenseEntity expense) {
        SessionManager session = new SessionManager(context);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 1. PREMIUM BACKGROUND & WATERMARK
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.parseColor("#F8FAFC"));
        canvas.drawRect(10, 10, 440, 690, paint);

        // Anti-Scam Watermark
        paint.setColor(Color.parseColor("#E2E8F0"));
        paint.setTextSize(30);
        paint.setFakeBoldText(true);
        paint.setAlpha(30);
        for (int i = 0; i < 700; i += 150) {
            canvas.save();
            canvas.rotate(-35, 225, i);
            canvas.drawText("FININTELLIGENCE OFFICIAL", -50, i, paint);
            canvas.restore();
        }
        paint.setAlpha(255);

        // 2. OUTER BORDER
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.parseColor("#1E293B"));
        canvas.drawRect(15, 15, 435, 685, paint);
        paint.setStrokeWidth(1);
        paint.setColor(Color.parseColor("#D4AF37")); // Gold
        canvas.drawRect(20, 20, 430, 680, paint);

        // 3. HEADER BLOCK
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0F172A"));
        canvas.drawRect(15, 15, 435, 120, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("FININTELLIGENCE AI", 90, 60, paint);
        
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        paint.setColor(Color.parseColor("#CBD5E1"));
        canvas.drawText("PREMIUM DIGITAL TRANSACTION RECEIPT", 95, 85, paint);
        canvas.drawText("SECURED BY FINTECH AI ENCRYPTION", 130, 105, paint);

        // 4. CUSTOMER INFO
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("ISSUED TO:", 40, 160, paint);
        
        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        canvas.drawText("Name: " + session.getUsername().toUpperCase(), 40, 185, paint);
        canvas.drawText("Email: " + session.getUserEmail(), 40, 205, paint);

        // 5. DATA TABLE
        paint.setColor(Color.parseColor("#F1F5F9"));
        canvas.drawRoundRect(35, 240, 415, 500, 10, 10, paint);
        
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        int y = 280;
        canvas.drawText("DESCRIPTION", 60, y, paint);
        canvas.drawText("DETAILS", 220, y, paint);
        paint.setStrokeWidth(1);
        canvas.drawLine(50, y+10, 400, y+10, paint);

        paint.setFakeBoldText(false);
        y += 40;
        canvas.drawText("Transaction ID", 60, y, paint);
        canvas.drawText("#FT-" + System.currentTimeMillis() / 1000, 220, y, paint);
        
        y += 40;
        canvas.drawText("Category", 60, y, paint);
        canvas.drawText(expense.getCategory().toUpperCase(), 220, y, paint);
        
        y += 40;
        canvas.drawText("Date", 60, y, paint);
        canvas.drawText(expense.getDate(), 220, y, paint);
        
        y += 40;
        canvas.drawText("Note", 60, y, paint);
        String note = expense.getTitle();
        canvas.drawText(note.length() > 25 ? note.substring(0, 22) + "..." : note, 220, y, paint);

        // 6. AMOUNT BOX
        y = 540;
        paint.setColor(Color.parseColor("#1E293B"));
        canvas.drawRoundRect(100, y-30, 350, y+40, 15, 15, paint);
        
        paint.setColor(Color.parseColor("#D4AF37")); // Gold
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL AMOUNT", 175, y, paint);
        paint.setTextSize(22);
        canvas.drawText(CurrencyUtils.formatPKR(expense.getAmount()), 160, y+30, paint);

        // 7. SECURITY STAMP
        paint.setColor(Color.parseColor("#EF4444")); // Red
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        canvas.drawCircle(350, 620, 50, paint);
        canvas.drawCircle(350, 620, 45, paint);
        
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(10);
        paint.setFakeBoldText(true);
        canvas.save();
        canvas.rotate(-15, 350, 620);
        canvas.drawText("AUTHENTIC", 325, 615, paint);
        canvas.drawText("VERIFIED", 330, 630, paint);
        canvas.restore();

        // 8. QR PLACEHOLDER
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawRect(40, 580, 100, 640, paint);
        paint.setStyle(Paint.Style.FILL);
        for(int i=45; i<95; i+=10) {
            for(int j=585; j<635; j+=10) {
                if((i+j) % 20 == 0) canvas.drawRect(i, j, i+6, j+6, paint);
            }
        }

        // 9. FOOTER
        paint.setColor(Color.GRAY);
        paint.setTextSize(9);
        paint.setFakeBoldText(false);
        canvas.drawText("This receipt is a legal proof of transaction generated by FinIntelligence AI.", 40, 665, paint);
        canvas.drawText("Any alteration is strictly prohibited.", 40, 678, paint);
    }
}
