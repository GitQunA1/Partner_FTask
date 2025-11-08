package com.example.partner_ftask.ui.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.Transaction;
import com.example.partner_ftask.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addTransactions(List<Transaction> newTransactions) {
        if (newTransactions != null) {
            int startPosition = this.transactions.size();
            this.transactions.addAll(newTransactions);
            notifyItemRangeInserted(startPosition, newTransactions.size());
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout iconContainer;
        private final TextView tvIcon;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvStatus;
        private final TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.icon_container);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }

        public void bind(Transaction transaction) {
            // Description
            tvDescription.setText(transaction.getDescription());

            // Date
            tvDate.setText(DateTimeUtils.formatDateTime(transaction.getCreatedAt()));

            // Type & Amount
            String type = transaction.getType();
            double amount = transaction.getAmount();

            int iconColor;
            String icon;
            String amountText;
            int amountColor;

            if ("EARNING".equals(type)) {
                iconColor = itemView.getContext().getColor(R.color.earning_green);
                icon = "↓";
                amountText = "+" + DateTimeUtils.formatCurrency(amount);
                amountColor = itemView.getContext().getColor(R.color.earning_green);
            } else if ("WITHDRAWAL".equals(type)) {
                iconColor = itemView.getContext().getColor(R.color.withdrawal_red);
                icon = "↑";
                amountText = "-" + DateTimeUtils.formatCurrency(amount);
                amountColor = itemView.getContext().getColor(R.color.withdrawal_red);
            } else {
                iconColor = itemView.getContext().getColor(R.color.text_secondary);
                icon = "•";
                amountText = DateTimeUtils.formatCurrency(amount);
                amountColor = itemView.getContext().getColor(R.color.text_secondary);
            }

            // Set icon
            tvIcon.setText(icon);

            // Set icon background color
            GradientDrawable drawable = (GradientDrawable) iconContainer.getBackground();
            if (drawable != null) {
                drawable.setColor(iconColor);
            }

            // Set amount
            tvAmount.setText(amountText);
            tvAmount.setTextColor(amountColor);

            // Status
            String status = transaction.getStatus();
            int statusColor;
            String statusText;

            switch (status) {
                case "COMPLETED":
                    statusColor = itemView.getContext().getColor(R.color.success);
                    statusText = "Hoàn thành";
                    break;
                case "PENDING":
                    statusColor = itemView.getContext().getColor(R.color.pending_orange);
                    statusText = "Đang xử lý";
                    break;
                case "FAILED":
                    statusColor = itemView.getContext().getColor(R.color.error);
                    statusText = "Thất bại";
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
                    statusText = status;
                    break;
            }

            tvStatus.setText(statusText);
            GradientDrawable statusDrawable = (GradientDrawable) tvStatus.getBackground();
            if (statusDrawable != null) {
                statusDrawable.setColor(statusColor);
            }
        }
    }
}

