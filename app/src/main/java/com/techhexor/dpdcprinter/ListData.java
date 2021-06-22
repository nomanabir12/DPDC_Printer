package com.techhexor.dpdcprinter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListData extends AppCompatActivity {

    DB_Helper mydbhelper;
    private ListView mylist;
    private EditText searchbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        mylist = findViewById(R.id.listView1);
        searchbox = findViewById(R.id.search_box);

        mydbhelper = new DB_Helper(this);
        loaddata();


        searchbox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchdata();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void loaddata() {
        ArrayList<String> listDatArrayList = new ArrayList<String>();

        Cursor cursor = mydbhelper.showAllData();

        if (cursor.getCount() == 0) {
            showMessage("No Data Available");
        } else {
            while (cursor.moveToNext()) {
                listDatArrayList.add("Serial No: "+cursor.getString(1)+"\n"+"Date: "+cursor.getString(3)+"\n"+
                        cursor.getString(2)	);

            }
        }
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.textview, listDatArrayList);
        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = parent.getItemAtPosition(position).toString();
                Intent localIntent = new Intent(ListData.this, SingleMessage.class);
                localIntent.putExtra("Selected_Value", str);
                ListData.this.startActivity(localIntent);

            }
        });

    }

    private void searchdata() {
        ArrayList<String> listDatArrayList = new ArrayList<String>();

        Cursor cursor = this.mydbhelper.searchData(this.searchbox.getText().toString());

        if (cursor.getCount() == 0) {
            showMessage("No Data Available");
        } else {
            while (cursor.moveToNext()) {
                listDatArrayList.add("Serial No: "+cursor.getString(1)+"\n"+"Date: "+cursor.getString(3)+"\n"+
                        cursor.getString(2)	);

            }
        }
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.textview, listDatArrayList);
        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = parent.getItemAtPosition(position).toString();
                Intent localIntent = new Intent(ListData.this, SingleMessage.class);
                localIntent.putExtra("Selected_Value", str);
                ListData.this.startActivity(localIntent);

            }
        });

    }

    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }
}
