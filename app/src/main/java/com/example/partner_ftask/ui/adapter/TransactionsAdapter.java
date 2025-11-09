package com.example.partner_ftask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        private final ImageView ivIcon;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvStatus;
        private final TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views with fallback to alternative IDs
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }

        public void bind(Transaction transaction) {
            // Description
            if (tvDescription != null) {
                tvDescription.setText(transaction.getDescription());
            }

            // Date
            if (tvDate != null) {
                tvDate.setText(DateTimeUtils.formatDateTime(transaction.getCreatedAt()));
            }

            // Type & Amount
            String type = transaction.getType();
            double amount = transaction.getAmount();

            int iconColor;
            int iconRes;
            String amountText;
            int amountColor;

            if ("EARNING".equalsIgnoreCase(type) || "TOP_UP".equalsIgnoreCase(type)) {
                iconColor = ContextCompat.getColor(itemView.getContext(), R.color.earning_green);
                iconRes = R.drawable.ic_earning;
                amountText = "+" + DateTimeUtils.formatCurrency(amount);
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.earning_green);
            } else if ("WITHDRAWAL".equalsIgnoreCase(type)) {
                iconColor = ContextCompat.getColor(itemView.getContext(), R.color.withdrawal_red);
                iconRes = R.drawable.ic_withdrawal;
                amountText = "-" + DateTimeUtils.formatCurrency(amount);
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.withdrawal_red);
            } else {
                iconColor = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
                iconRes = R.drawable.ic_wallet;
                amountText = DateTimeUtils.formatCurrency(amount);
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
            }

            // Set icon
            if (ivIcon != null) {
                ivIcon.setImageResource(iconRes);
            }

            // Set icon background color
            if (iconContainer != null) {
                iconContainer.setBackgroundColor(iconColor);
            }

            // Set amount
            if (tvAmount != null) {
                tvAmount.setText(amountText);
                tvAmount.setTextColor(amountColor);
            }

            // Status
            String status = transaction.getStatus();
            int statusColor;
            String statusText;

            if (status == null) {
                status = "COMPLETED";
            }

            switch (status.toUpperCase()) {
                case "COMPLETED":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.success);
                    statusText = "Hoàn thành";
                    break;
                case "PENDING":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.pending_orange);
                    statusText = "Đang xử lý";
                    break;
                case "FAILED":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.error);
                    statusText = "Thất bại";
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
                    statusText = status;
                    break;
            }

            if (tvStatus != null) {
                tvStatus.setText(statusText);
                tvStatus.setBackgroundColor(statusColor);
                // Only show if not COMPLETED
                tvStatus.setVisibility("COMPLETED".equalsIgnoreCase(status) ? View.GONE : View.VISIBLE);
            }
        }
    }
}

