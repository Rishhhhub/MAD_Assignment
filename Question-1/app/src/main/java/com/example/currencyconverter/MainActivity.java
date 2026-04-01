package com.example.currencyconverter;

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
    Button btnConvert, btnSwap, btnTheme;
    TextView tvResult, tvRate;

    String[] currencies = {"INR", "USD", "JPY", "EUR"};
    double[] ratesInINR = {1.0, 83.5, 0.55, 90.2};

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply theme BEFORE super.onCreate
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link views
        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwap = findViewById(R.id.btnSwap);
        btnTheme = findViewById(R.id.btnTheme);
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

        // Update moon/sun icon based on current theme
        btnTheme.setText(isDark ? "☀️" : "🌙");

        // Convert
        btnConvert.setOnClickListener(v -> convertCurrency());

        // Swap
        btnSwap.setOnClickListener(v -> {
            int from = spinnerFrom.getSelectedItemPosition();
            int to = spinnerTo.getSelectedItemPosition();
            spinnerFrom.setSelection(to);
            spinnerTo.setSelection(from);
        });

        // Theme toggle
        btnTheme.setOnClickListener(v -> toggleTheme());
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

        // Show rate hint like "USD 1 = INR 83.50"
        double rate = ratesInINR[fromIndex] / ratesInINR[toIndex];
        tvRate.setText(currencies[fromIndex] + " 1 = " + currencies[toIndex] + " " +
                String.format("%.2f", rate));
    }

    private void toggleTheme() {
        boolean isDark = prefs.getBoolean("dark_mode", false);
        boolean newMode = !isDark;

        prefs.edit().putBoolean("dark_mode", newMode).apply();
        AppCompatDelegate.setDefaultNightMode(
                newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Recreate so UI refreshes with new theme
        recreate();
    }
}