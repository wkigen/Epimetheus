package com.github.wkigen.epimetheus_simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.wkigen.epimetheus.EpimetheusManager;

import java.lang.reflect.Method;

public class SimpleActivity extends AppCompatActivity {

    TextView showText = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        Patch patch = new Patch();
        showText = findViewById(R.id.tx_show);
        showText.setText(patch.print());

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpimetheusManager.installHot(getApplication());

                Patch patch = new Patch();
                showText.setText(patch.print());
            }
        });
    }
}
