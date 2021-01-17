package dev.muathamer.currencyconverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.List;

public class DialogHandler {

    private static final String TAG = "DialogHandler";

    public static void createSingleItemDialog(Activity activity, List<ExtendedCurrency> list, String title, OnClick listener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.currency_chooser_dialog, null);
        TextView titleTextView = view.findViewById(R.id.title_textview);
        titleTextView.setText(title);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        AlertDialog dialog = prepareDialog(activity, view);

        final SingleItemAdapter adapter = new SingleItemAdapter(activity, list, dialog, listener);
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private static AlertDialog prepareDialog(Activity activity, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        return builder.create();
    }

    public interface OnClick {
        void onClick(ExtendedCurrency item);
    }

    private static class SingleItemAdapter extends RecyclerView.Adapter<SingleItemAdapter.ViewHolder> {
        List<ExtendedCurrency> list;
        AlertDialog dialog;
        OnClick listener;
        Activity activity;

        SingleItemAdapter(Activity activity, List<ExtendedCurrency> list, AlertDialog dialog, OnClick listener) {
            this.list = list;
            this.dialog = dialog;
            this.listener = listener;
            this.activity = activity;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(activity).inflate(R.layout.currency_chooser_single_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
            ExtendedCurrency currency = list.get(i);
            holder.flagImage.setImageResource(currency.getFlag());
            holder.currencyName.setText(currency.getName());
            holder.currencyCode.setText(currency.getCode());

            if (!currency.getCode().equals(currency.getSymbol()))
                holder.currencySymbol.setText(currency.getSymbol());
            else
                holder.currencySymbol.setText("");

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            MaterialCardView rootCard;
            TextView currencyCode;
            TextView currencyName;
            TextView currencySymbol;
            ImageView flagImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                rootCard = itemView.findViewById(R.id.rootCard);
                currencyName = itemView.findViewById(R.id.currencyName);
                currencyCode = itemView.findViewById(R.id.currencyCode);
                currencySymbol = itemView.findViewById(R.id.currencySymbol);
                flagImage = itemView.findViewById(R.id.flagImage);
                rootCard.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                listener.onClick(list.get(getAdapterPosition()));
                dialog.dismiss();
            }
        }
    }

}