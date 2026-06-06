package com.example.fintechaiassistantapp.analytics;

import android.graphics.Color;
import com.example.fintechaiassistantapp.models.CategorySummaryModel;
import com.example.fintechaiassistantapp.models.MonthlyTrendModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ChartDataManager {

    public static PieData getPieData(List<CategorySummaryModel> summaries) {
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySummaryModel summary : summaries) {
            entries.add(new PieEntry((float) summary.getTotalAmount(), summary.getCategory()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getCustomColors());
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        return data;
    }

    public static BarData getBarData(List<MonthlyTrendModel> trends) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < trends.size(); i++) {
            entries.add(new BarEntry(i, (float) trends.get(i).getAmount()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Spending");
        dataSet.setColor(Color.parseColor("#3B82F6")); // secondary color
        dataSet.setValueTextColor(Color.parseColor("#64748B")); // text_secondary
        dataSet.setValueTextSize(10f);

        return new BarData(dataSet);
    }

    private static List<Integer> getCustomColors() {
        List<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        return colors;
    }
}
