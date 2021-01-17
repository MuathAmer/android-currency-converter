package dev.muathamer.currencyconverter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.icu.util.Currency;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

class DomesticCurrency {

    private static final String TAG = "DomesticCurrency";

    private final Activity context;

    public DomesticCurrency(Activity context) {
        this.context = context;
    }

    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        List<String> providers = lm.getProviders(false);
        for (String provider : providers) {
            Location l = lm.getLastKnownLocation(provider);
            if (l != null) {
                return l;
            }
        }
        return null;
    }

    public String[] getCountryCodeAndAddress() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return null;

        Location location = getLocation();
        Log.d(TAG, ":::LOCATION::: " + location);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context);

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (Exception e) {
            return null;
        }

        Log.d(TAG, ":::ADDRESSES::: " + addresses.size());
        if (addresses.size() == 0)
            return null;

        Address address = addresses.get(0);
        String addressLine;
        if (address.getLocality() != null && address.getCountryName() != null)
            addressLine = address.getLocality() + ", " + address.getCountryName();
        else addressLine = address.getAddressLine(0).replaceAll("\\d","");

        Currency currency = null;
        try {
            Locale locale = new Locale(address.getCountryCode(), address.getCountryCode());
            currency = Currency.getInstance(locale);
            Log.d(TAG, "Locale: " + locale);
        } catch (Exception e) {
            return new String[]{addressLine};
        }

        if (currency == null || currency.getCurrencyCode().isEmpty()) return null;

        Log.d(TAG, "Currency code: " + currency.getCurrencyCode());

        Log.d(TAG, "Address: " + addressLine);

        return new String[]{
                currency.getCurrencyCode(),
                addressLine
        };
    }
}
