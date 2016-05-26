package com.tarungoyaldev.android.utilitiesapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    public void onClick(View view) {
        if (view instanceof Button) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.calculatorButton:
                    Intent intent = new Intent(this, CalculatorActivity.class);
                    startActivity(intent);
                    break;
                case R.id.unitConverterButton:
                    intent = new Intent(this, UnitConverterActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }
}
