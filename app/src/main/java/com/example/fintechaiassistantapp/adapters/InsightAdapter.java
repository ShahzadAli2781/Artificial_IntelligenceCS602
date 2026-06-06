package com.example.fintechaiassistantapp.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.models.AiInsightEntity;
import java.util.ArrayList;
import java.util.List;

public class InsightAdapter extends RecyclerView.Adapter<InsightAdapter.ViewHolder> {
    private List<AiInsightEntity> insights = new ArrayList<>();

    public void setInsights(List<AiInsightEntity> insights) {
        this.insights = insights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_insight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AiInsightEntity insight = insights.get(position);
        Context context = holder.itemView.getContext();
        
        String fullMessage = insight.getMessage();
        if (fullMessage != null && fullMessage.contains(": ")) {
            String[] parts = fullMessage.split(": ", 2);
            holder.title.setText(parts[0]);
            holder.content.setText(parts[1]);
        } else {
            holder.title.setText("AI Insight");
            holder.content.setText(fullMessage);
        }
        
        int color;
        int iconRes;
        
        if ("warning".equalsIgnoreCase(insight.getType())) {
            iconRes = android.R.drawable.ic_dialog_alert;
            color = ContextCompat.getColor(context, R.color.error);
        } else if ("suggestion".equalsIgnoreCase(insight.getType())) {
            iconRes = android.R.drawable.ic_menu_edit;
            color = ContextCompat.getColor(context, R.color.success);
        } else {
            iconRes = android.R.drawable.ic_dialog_info;
            color = ContextCompat.getColor(context, R.color.secondary);
        }
        
        holder.icon.setImageResource(iconRes);
        holder.icon.setColorFilter(color);
        holder.title.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return insights.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_insight_title);
            content = itemView.findViewById(R.id.tv_insight_content);
            icon = itemView.findViewById(R.id.iv_insight_icon);
        }
    }
}