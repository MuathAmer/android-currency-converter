package dev.muathamer.currencyconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

class CurrencyConverter {

    private static final String TAG = "CurrencyConverter";

    private final Context context;
    private final HashMap<String, Double> currentConversionRatesToAllCurrencies;
    private final ArrayList<String> historicConversionDates;
    private final ArrayList<Double> historicConversionRates;
    private ExtendedCurrency srcCurrency;
    private ExtendedCurrency destCurrency;

    public CurrencyConverter(Context context) {
        this.context = context;
        currentConversionRatesToAllCurrencies = new HashMap<>();
        historicConversionDates = new ArrayList<>();
        historicConversionRates = new ArrayList<>();
    }

    public void setSrcCurrency(ExtendedCurrency srcCurrency, Runnable before, Runnable after) {
        this.srcCurrency = srcCurrency;
        currentConversionRatesToAllCurrencies.clear();
        historicConversionRates.clear();

        before.run();

        //get allcurrencies map
        RequestQueue queue = Volley.newRequestQueue(context);
        String queryPath = "/latest?from=" + srcCurrency.getCode();
        String url = context.getString(R.string.currency_exchange_api) + queryPath;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Display the first 500 characters of the response string.
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject ratesObjs = jsonObject.getJSONObject("rates");
                        for (Iterator<String> it = ratesObjs.keys(); it.hasNext(); ) {
                            String key = it.next();
                            double value = ratesObjs.getDouble(key);
                            currentConversionRatesToAllCurrencies.put(key, value);
                        }
                        currentConversionRatesToAllCurrencies.put(srcCurrency.getCode(), 1.0);
                        Log.d(TAG, "finished fetching values to all currencies");
                        Log.d(TAG, currentConversionRatesToAllCurrencies.toString());
                    } catch (JSONException e) {
                        Log.d(TAG, "error fetching values to all currencies");
                        e.printStackTrace();
                    }
                    after.run();
                }, error -> Log.d(TAG, "Error: " + error)
        );

        queue.add(stringRequest);
    }

    public void setDestCurrency(ExtendedCurrency destCurrency, Runnable before, BiConsumer<List<String>, List<Double>> after) {
        this.destCurrency = destCurrency;
        historicConversionRates.clear();

        before.run();

        //get fromtocurrency over period for chart
        //get allcurrencies map
        RequestQueue queue = Volley.newRequestQueue(context);
        String queryPath = "/2020-01-01..?from=" + srcCurrency.getCode() + "&to=" + destCurrency.getCode();
        String url = context.getString(R.string.currency_exchange_api) + queryPath;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Display the first 500 characters of the response string.
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject ratesObjs = jsonObject.getJSONObject("rates");
                        for (Iterator<String> it = ratesObjs.keys(); it.hasNext(); ) {
                            String key = it.next();
                            double value = ratesObjs.getJSONObject(key).getDouble(destCurrency.getCode());
                            historicConversionDates.add(key);
                            historicConversionRates.add(value);
                        }
//                        Log.d(TAG,historicConversionRates);
                        Log.d(TAG, "finished fetching historic values");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "error fetching historic values");
                    }

                    after.accept(historicConversionDates, historicConversionRates);
                }, error -> Log.d(TAG, "Error: " + error)
        );

        queue.add(stringRequest);
    }

    public double getConvertedAmount(double fromAmount) {
        try {
            Log.d(TAG, "getting conversion amount");
            double conversionRate = currentConversionRatesToAllCurrencies.get(destCurrency.getCode());
            Log.d(TAG, "Conversion rate: " + conversionRate);
            return fromAmount * conversionRate;
        } catch (Exception e) {
            return -1.0;
        }
    }

    @SuppressLint("DefaultLocale")
    public String getConversionRateString() {
        double rate;
        if (srcCurrency != null && destCurrency != null
                && (rate = getConvertedAmount(1)) != -1) {
            return String.format("1 %s  =  %.3f %s", srcCurrency.getSymbol(), rate, destCurrency.getSymbol());
        } else {
            // move to resources
            return "Choose from and to currencies";
        }
    }
}
