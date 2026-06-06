package com.example.fintechaiassistantapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import java.util.ArrayList;
import com.example.fintechaiassistantapp.utils.CurrencyUtils;
import java.util.List;

import android.view.MenuItem;
import android.widget.PopupMenu;
import com.example.fintechaiassistantapp.utils.CurrencyUtils;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onEdit(ExpenseEntity expense);
        void onShare(ExpenseEntity expense);
        void onPrint(ExpenseEntity expense);
        void onDelete(ExpenseEntity expense);
    }

    private List<ExpenseEntity> expenseList = new ArrayList<>();
    private OnExpenseActionListener actionListener;

    public void setExpenses(List<ExpenseEntity> expenses) {
        this.expenseList = expenses;
        notifyDataSetChanged();
    }

    public void setOnExpenseActionListener(OnExpenseActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);
        holder.tvTitle.setText(expense.getTitle());
        holder.tvAmount.setText("-" + CurrencyUtils.formatPKR(expense.getAmount()));
        holder.tvCategory.setText(expense.getCategory());
        holder.tvDate.setText(expense.getDate());

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 1, 0, R.string.edit);
            popup.getMenu().add(0, 2, 1, R.string.share);
            popup.getMenu().add(0, 3, 2, R.string.print);
            popup.getMenu().add(0, 4, 3, R.string.delete);

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener == null) return false;
                int id = item.getItemId();
                if (id == 1) actionListener.onEdit(expense);
                else if (id == 2) actionListener.onShare(expense);
                else if (id == 3) actionListener.onPrint(expense);
                else if (id == 4) actionListener.onDelete(expense);
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvCategory, tvDate;
        ImageButton btnMore;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvAmount = itemView.findViewById(R.id.tv_item_amount);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}