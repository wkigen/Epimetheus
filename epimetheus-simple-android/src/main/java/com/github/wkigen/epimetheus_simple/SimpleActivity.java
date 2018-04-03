package com.github.wkigen.epimetheus_simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.reflect.Method;

public class SimpleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        Patch patch = new Patch();
        TextView showText = findViewById(R.id.tx_show);
        showText.setText(patch.print());

    }
}
