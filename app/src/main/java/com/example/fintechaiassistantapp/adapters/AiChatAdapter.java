package com.example.fintechaiassistantapp.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.models.ChatMessage;
import com.google.android.material.color.MaterialColors;
import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<AiChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages;

    public AiChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.tvMessage.setText(message.getText());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.tvMessage.getLayoutParams();
        if (message.isUser()) {
            holder.tvMessage.setBackgroundResource(R.drawable.bg_chat_user);
            holder.tvMessage.setTextColor(MaterialColors.getColor(holder.tvMessage, com.google.android.material.R.attr.colorOnPrimary));
            params.setMarginStart(100);
            params.setMarginEnd(0);
        } else {
            holder.tvMessage.setBackgroundResource(R.drawable.bg_chat_ai);
            holder.tvMessage.setTextColor(MaterialColors.getColor(holder.tvMessage, com.google.android.material.R.attr.colorOnSurfaceVariant));
            params.setMarginStart(0);
            params.setMarginEnd(100);
        }
        holder.tvMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ChatViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }
    }
}
