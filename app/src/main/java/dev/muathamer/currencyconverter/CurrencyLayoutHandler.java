package dev.muathamer.currencyconverter;

import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.mynameismidori.currencypicker.ExtendedCurrency;

public class CurrencyLayoutHandler {

    private static final String TAG = "CurrencyLayoutHandler";
    private final MaterialCardView cardView;
    private final ImageView flagImageView;
    private final TextView currencyCodeTextView;
    private final TextView currencyName;
    private final Type type;
    public CurrencyLayoutHandler(Type type, ConstraintLayout rootLayout, TextView currencyName) {
        this.type = type;
        this.currencyName = currencyName;

        cardView = (MaterialCardView) rootLayout.getViewById(R.id.currencyCard);
        flagImageView = (ImageView) rootLayout.getViewById(R.id.flag);
        currencyCodeTextView = (TextView) rootLayout.getViewById(R.id.currencyCode);
    }

    public void setCurrency(ExtendedCurrency currency) {
        if (currency == null) {
            currencyCodeTextView.setText(type == Type.SOURCE ? "From" : "To");
            flagImageView.setImageResource(R.drawable.ic_baseline_attach_money_24);
            currencyName.setText(type == Type.SOURCE ? "Select currency first" : "");
        } else {
            currencyCodeTextView.setText(currency.getCode());
            flagImageView.setImageResource(currency.getFlag());
            currencyName.setText(currency.getName());
        }

    }

    public void setOnClickListener(OnClickListener onClickListener) {
        cardView.setOnClickListener(onClickListener);
    }

    public enum Type {SOURCE, DESTINATION}
}
