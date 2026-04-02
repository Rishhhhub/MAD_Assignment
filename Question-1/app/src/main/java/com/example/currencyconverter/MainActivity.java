package com.example.currencyconverter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    EditText etAmount;
    Spinner spinnerFrom, spinnerTo;
    Button btnConvert, btnSwap, btnSettings;
    TextView tvResult, tvRate;

    String[] currencies = {"INR", "USD", "JPY", "EUR"};
    double[] ratesInINR = {1.0, 83.5, 0.55, 90.2};

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply saved theme BEFORE super.onCreate
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Link views
        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwap = findViewById(R.id.btnSwap);
        btnSettings = findViewById(R.id.btnSettings);
        tvResult = findViewById(R.id.tvResult);
        tvRate = findViewById(R.id.tvRate);

        // Setup spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
        spinnerFrom.setSelection(1); // USD
        spinnerTo.setSelection(0);   // INR

        // Buttons
        btnConvert.setOnClickListener(v -> convertCurrency());

        btnSwap.setOnClickListener(v -> {
            int from = spinnerFrom.getSelectedItemPosition();
            int to = spinnerTo.getSelectedItemPosition();
            spinnerFrom.setSelection(to);
            spinnerTo.setSelection(from);
        });

        // Opens Settings page
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void convertCurrency() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter an amount first", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int fromIndex = spinnerFrom.getSelectedItemPosition();
        int toIndex = spinnerTo.getSelectedItemPosition();

        double amountInINR = amount * ratesInINR[fromIndex];
        double result = amountInINR / ratesInINR[toIndex];

        tvResult.setText(String.format("%.2f", result));

        double rate = ratesInINR[fromIndex] / ratesInINR[toIndex];
        tvRate.setText(currencies[fromIndex] + " 1 = " + currencies[toIndex] + " " + String.format("%.2f", rate));
    }
    @Override
    protected void onResume() {
        super.onResume();
        // When returning from Settings, recreate if theme changed
        boolean isDark = prefs.getBoolean("dark_mode", false);
        boolean currentlyDark = (AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES);
        if (isDark != currentlyDark) {
            recreate();
        }
    }
}