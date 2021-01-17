package dev.muathamer.currencyconverter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import dev.muathamer.currencyconverter.CurrencyLayoutHandler.Type;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final CurrencyConverter currencyConverter = new CurrencyConverter(this);
    private final DomesticCurrency domesticCurrency = new DomesticCurrency(this);
    private CurrencyLayoutHandler srcCurrencyLayoutHandler;
    private CurrencyLayoutHandler destCurrencyLayoutHandler;
    private MaterialButton swapButton;
    private MaterialButton clearValuesButton;
    private TextView conversionRateTextView;
    private TextInputEditText srcValueTextView;
    private TextInputEditText destValueTextView;
    private LineChart chart;
    private CircularProgressIndicator loadingIndicator;
    private ExtendedCurrency srcCurrency, destCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //Force night mode
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView(); //Initialize member view variables

        requestLocation();

        clearValuesButton.setOnClickListener(this::clearValuesButton);
        swapButton.setOnClickListener(this::swapCurrencies);
        srcCurrencyLayoutHandler.setOnClickListener(
                v -> showCurrencyChooser(this::setSrcCurrency, Type.SOURCE)
        );
        destCurrencyLayoutHandler.setOnClickListener(
                v -> showCurrencyChooser(this::setDestCurrency, Type.DESTINATION)
        );

        setupSrcValueOnChangeHandler();
        updateCurrencyLayouts();
    }

    private void styleChart() {
        int white = getColor(R.color.white), purple = getColor(R.color.purple_200);

        chart.getAxisLeft().setTextColor(white);
        chart.getXAxis().setTextColor(white);
        chart.getLegend().setTextColor(purple);
        chart.getLineData().setValueTextColor(white);
        chart.getData().setHighlightEnabled(false);

        Legend l = chart.getLegend();
        l.setTextColor(purple);

        XAxis x = chart.getXAxis();
        x.setTextColor(white);
        YAxis y1 = chart.getAxisLeft();
        y1.setTextColor(white);
    }

    private void updateChart(List<String> conversionDates, List<Double> conversionRates) {
        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 0; i < conversionRates.size(); i++) {
            double yValue = conversionRates.get(i);
            yValues.add(new Entry(i, (float) yValue));
        }

        int color = this.getColor(R.color.purple_200);

        LineDataSet dataSet = new LineDataSet(yValues, "Historic Conversion Rates");
        dataSet.setColor(color);
        dataSet.setCircleColor(color);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(conversionDates));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

        styleChart();
    }

    private void showCurrencyChooser(DialogHandler.OnClick listener, Type type) {
        String fromOrTo = type == Type.SOURCE ? "from" : "to";
        DialogHandler.createSingleItemDialog(MainActivity.this, CurrencyUtil.getAllSupportedCurrencies(),
                "Choose a currency to convert " + fromOrTo + ":", listener);
    }

    private void requestLocation() {
        domesticCurrency.requestLocationPermission();
        String[] countryCodeAndAddress = domesticCurrency.getCountryCodeAndAddress();
        if (countryCodeAndAddress != null) {
            if (countryCodeAndAddress.length == 2) {
                String countryCode = countryCodeAndAddress[0];
                String address = countryCodeAndAddress[1];
                ExtendedCurrency srcCurrency = CurrencyUtil.getCurrencyByCode(countryCode);
                if (srcCurrency != null) {
                    setSrcCurrency(srcCurrency);

                    Snackbar.make(findViewById(R.id.rootConstraintLayout), "Domestic currency has been detected for " + address + " as: " + countryCode, Snackbar.LENGTH_INDEFINITE).setAction("Dismiss", v -> {
                    }).show();
                }
            } else if (countryCodeAndAddress.length == 1) {
                Snackbar.make(findViewById(R.id.rootConstraintLayout), "Could not identify domestic currency for " + countryCodeAndAddress[0] + ".", Snackbar.LENGTH_INDEFINITE).setAction("Dismiss", v -> {
                }).show();
            }
            return;
        }

        Snackbar.make(findViewById(R.id.rootConstraintLayout), "Could not identify domestic currency via location.", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        domesticCurrency.onRequestPermissionsResult(requestCode, grantResults);
        requestLocation();
    }

    private void setSrcCurrency(ExtendedCurrency currency) {
        Runnable loading = () -> setViewsToLoading(true);
        Runnable finishedLoading = () -> {
            setViewsToLoading(false);
            updateCurrencyLayouts();
        };
        Log.d(TAG, "setting srcCurrency");
        currencyConverter.setSrcCurrency(currency, loading, finishedLoading);
        srcCurrency = currency;
    }

    private void setDestCurrency(ExtendedCurrency currency) {
        Runnable loading = () -> setViewsToLoading(true);
        BiConsumer<List<String>, List<Double>> finishedLoading = (dates, rates) -> {
            setViewsToLoading(false);
            updateCurrencyLayouts();

            updateChart(dates, rates);
        };
        Log.d(TAG, "setting destCurrency");
        currencyConverter.setDestCurrency(currency, loading, finishedLoading);
        destCurrency = currency;
    }

    private void setViewsToLoading(boolean isLoading) {
        srcValueTextView.setEnabled(!isLoading);
        chart.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupSrcValueOnChangeHandler() {
        srcValueTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                onInputValueChanged();
            }
        });
    }

    private void onInputValueChanged() {
        clearValuesButton.setEnabled(!srcValueTextView.getText().toString().isEmpty());

        // update toValue using conversion rate
        String str = srcValueTextView.getText().toString();
        if (str.isEmpty() || srcCurrency == null || destCurrency == null) {
            destValueTextView.setText("");
            return;
        }

        double fromValue = Double.parseDouble(str);
        double toValue = currencyConverter.getConvertedAmount(fromValue);
        String toValueStr = String.format("%.3f", toValue);
        destValueTextView.setText(toValueStr);
    }

    private void updateCurrencyLayouts() {
        Log.d(TAG, "updating views");

        srcCurrencyLayoutHandler.setCurrency(srcCurrency);
        destCurrencyLayoutHandler.setCurrency(destCurrency);

        onInputValueChanged();

        swapButton.setEnabled(srcCurrency != null && destCurrency != null);
        conversionRateTextView.setText(currencyConverter.getConversionRateString());
    }

    private void swapCurrencies(View view) {
        if (srcCurrency == null || destCurrency == null)
            return;

        ExtendedCurrency temp = srcCurrency;
        setSrcCurrency(destCurrency);
        setDestCurrency(temp);
    }

    private void clearValuesButton(View view) {
        srcValueTextView.setText("");
        destValueTextView.setText("");

//        this.srcCurrency = null;
//        this.destCurrency = null;
//        updateCurrencyLayouts();
    }

    private void initView() {
        ConstraintLayout srcCurrencyLayout = findViewById(R.id.srcCurrencyLayout);
        ConstraintLayout destCurrencyLayout = findViewById(R.id.destCurrencyLayout);
        TextView srcCurrencyName = findViewById(R.id.srcCurrencyName);
        TextView destCurrencyName = findViewById(R.id.destCurrencyName);

        //Outsource currency layouts handling
        this.srcCurrencyLayoutHandler = new CurrencyLayoutHandler(Type.SOURCE, srcCurrencyLayout, srcCurrencyName);
        this.destCurrencyLayoutHandler = new CurrencyLayoutHandler(Type.DESTINATION, destCurrencyLayout, destCurrencyName);

        swapButton = findViewById(R.id.swapButton);
        clearValuesButton = findViewById(R.id.clearValuesButton);
        conversionRateTextView = findViewById(R.id.conversionRateTextView);
        srcValueTextView = findViewById(R.id.fromValueTextView);
        destValueTextView = findViewById(R.id.toValueTextView);
        chart = findViewById(R.id.chart);
        loadingIndicator = findViewById(R.id.loadingIndicator);
    }
}