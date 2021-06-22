package com.techhexor.dpdcprinter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SingleMessage extends AppCompatActivity {

    private TextView textnormal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_message);
        textnormal = findViewById(R.id.single_message);

        String str = getIntent().getStringExtra("Selected_Value");
        textnormal.setText(str);


    }
}
