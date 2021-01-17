package dev.muathamer.currencyconverter;

import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.ArrayList;
import java.util.HashMap;

import dev.muathamer.Application;

class CurrencyUtil {

    private static final String TAG = "CurrencyUtil";

    private static final HashMap<String, ExtendedCurrency> currencyCodeMap;
    private static final ArrayList<ExtendedCurrency> allSupportedCurrencies;

    static {
        currencyCodeMap = new HashMap<>();
        for (ExtendedCurrency currency : ExtendedCurrency.CURRENCIES) {
            currencyCodeMap.put(currency.getCode(), currency);
        }

        allSupportedCurrencies = new ArrayList<>();
        String[] currencyCodes = Application.getAppContext().getResources().getStringArray(R.array.currency_codes);
        for (String currencyCode : currencyCodes) {
            allSupportedCurrencies.add(currencyCodeMap.get(currencyCode));
        }
    }

    public static ExtendedCurrency getCurrencyByCode(String currencyCode) {
        return currencyCodeMap.get(currencyCode);
    }

    public static ArrayList<ExtendedCurrency> getAllSupportedCurrencies() {
        return allSupportedCurrencies;
    }
}
