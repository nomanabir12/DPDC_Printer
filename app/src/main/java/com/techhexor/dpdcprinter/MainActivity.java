package com.techhexor.dpdcprinter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText msg_box;
    private Button btn_cnct, btn_print, btn_format, btn_all_message, btn_clear, btn_discnct;
    private TextView lblPrinterName;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferposition;
    volatile boolean stopWorker;

    DB_Helper mydbhelper;
    private static final int REQUEST_CODE_STORAGE = 1;

    String token_no;
    int serial_no=0;
    String msgbox_raw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") != 0) {
            ActivityCompat.requestPermissions(this, new String[] { "android.permission.BLUETOOTH_ADMIN" }, REQUEST_CODE_STORAGE);
        }

        if (REQUEST_CODE_STORAGE > 0) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
            msg_box = findViewById(R.id.msg_box);

            btn_cnct =findViewById(R.id.btn_cnct);
            btn_print = findViewById(R.id.btn_print);
            btn_format = findViewById(R.id.btn_format);
            btn_all_message =findViewById(R.id.btn_all_message);
            btn_clear = findViewById(R.id.btn_clear);
            btn_discnct = findViewById(R.id.btn_disconnect);
            lblPrinterName =findViewById(R.id.textview);
            mydbhelper = new DB_Helper(this);
            SQLiteDatabase sqLiteDatabase = mydbhelper.getWritableDatabase();


            final BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();

            btn_discnct.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        disconnectBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

            btn_clear.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(msg_box.getText().toString())) {
                        show_message("MessageBox is Empty");
                    } else {
                        msg_box.setText("");
                    }


                }
            });

            btn_all_message.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ListData.class);
                    startActivity(intent);

                }
            });

            btn_format.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(msg_box.getText().toString())) {

                        if (msg_box.getText().toString().contains("Successful!")) {
                            if (msg_box.getText().toString().contains(", ")) {
                                msg_box.setText(msg_box.getText().toString().replace(", ", "\n"));
                            }
                            if (msg_box.getText().toString().contains(",")) {
                                msg_box.setText(msg_box.getText().toString().replace(",", "\n"));
                            }
                            if (msg_box.getText().toString().contains("is")) {
                                msg_box.setText(msg_box.getText().toString().replace("is ", "is\n"));
                            }

                            if (msg_box.getText().toString().contains(" for ")) {
                                msg_box.setText(msg_box.getText().toString().replace(" for ", "\n"));
                            }

                            msg_box.getText().delete(0, 12);
                            int length = msg_box.getText().length();
                            msg_box.getText().delete(length - 1, length);
                            msgbox_raw = msg_box.getText().toString();
                            token_no = msg_box.getText().toString();
                            token_no = token_no.substring(26, 51);
                            msg_box.getText().delete(27, 52);

                            token_no = token_no.replace("-", "");
                        } else {
                            show_message("Message is not Formatable");
                        }
                    } else if (TextUtils.isEmpty(msg_box.getText().toString())) {
                        show_message("MessageBox is empty");
                    }


                }
            });

            btn_print.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!TextUtils.isEmpty(msg_box.getText().toString())) {
                        try {
                            printData();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (TextUtils.isEmpty(msg_box.getText().toString())) {
                        show_message("MessageBox is empty");
                    }


                }
            });

            btn_cnct.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {

                        if (bAdapter == null) {
                            show_message("Bluetooth Not Supported");
                        } else if (bAdapter.isEnabled()) {
                            FindBluetoothDevice();
                            openBluetoothPrinter();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });
        }
    }

    void show_message(String msgString) {
        Toast.makeText(getApplicationContext(), msgString, Toast.LENGTH_LONG).show();
    }

    void FindBluetoothDevice(){

        try{

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter==null){
                lblPrinterName.setText("No Bluetooth Adapter found");
            }
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT,0);
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if(pairedDevice.size()>0){
                for(BluetoothDevice pairedDev:pairedDevice){
                    // My Bluetoth printer name is BTP_F09F1A
                    if(pairedDev.getName().equals("MTP-II")){
                        bluetoothDevice=pairedDev;
                        lblPrinterName.setText("Printer Connected: "+pairedDev.getName());
                        Toast.makeText(getApplicationContext(), "Printer Connected", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else {
                        show_message("Not Find");
                    }
                }
            }

            lblPrinterName.setText("Bluetooth Printer Attached");
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    void openBluetoothPrinter() throws IOException{
        try{

            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket=bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream=bluetoothSocket.getOutputStream();
            inputStream=bluetoothSocket.getInputStream();

            beginListenData();

        }catch (Exception ex){

        }
    }

    void beginListenData(){
        try{

            final Handler handler =new Handler();
            final byte delimiter = 10;
            stopWorker = false;
            readBufferposition = 0;
            readBuffer = new byte[1024];

            thread=new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker){
                        try{
                            int byteAvailable = inputStream.available();
                            if(byteAvailable>0){
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);

                                for(int i=0; i<byteAvailable; i++){
                                    byte b = packetByte[i];
                                    if(b==delimiter){
                                        byte[] encodedByte = new byte[readBufferposition];
                                        System.arraycopy(
                                                readBuffer,0,
                                                encodedByte,0,
                                                encodedByte.length
                                        );
                                        final String data = new String(encodedByte,"US-ASCII");
                                        readBufferposition=0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                lblPrinterName.setText(data);
                                            }
                                        });
                                    }else{
                                        readBuffer[readBufferposition++]=b;
                                    }
                                }
                            }
                        }catch(Exception ex){
                            stopWorker=true;
                        }
                    }

                }
            });

            thread.start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    void printData(){
        try{
            if (serial_no == 0) {
                serial_no = 300;
            }

            String currentDateandTime = new SimpleDateFormat("hh:mm a dd MMMM, yyyy").format(new Date());
            String msg =msg_box.getText().toString();
            msg+="\n";
            long rowid = mydbhelper.insertData(serial_no, msgbox_raw, currentDateandTime);
            serial_no++;
            if (rowid == -1) {
                show_message("Unsuccessfull");
            }
            else {
                show_message("Successfull");
            }

            String header_text =
                            "================================\n"+
                            "          Serial: #"+serial_no+"\n"+
                            "          PADMA HARDWARE       \n"+
                            "          +8801751539321       \n"+
                            "Date:"+currentDateandTime+
                            "\n================================\n";

            byte[] format = { 27, 33, 0 };

            format[2] = ((byte)(0x8 | format[2]));
            format[2] = ((byte)(0x0 | format[2]));
            outputStream.write(format);
            outputStream.write(header_text.getBytes(),0,header_text.getBytes().length);



            outputStream.write(msg.getBytes(),0,msg.getBytes().length);



            String footer_text =
                            "\n====================\n"
                                    +token_no+
                            "\n====================\n"
                            +"\n                    \n"
                            +"\n                    \n";

            format[2] = ((byte)(0x8 | format[2]));
            format[2] = ((byte)(0x10 | format[2]));
            format[2] = ((byte)(0x23 | format[2]));
            outputStream.write(format);
            outputStream.write(footer_text.getBytes(),0,footer_text.getBytes().length);


            lblPrinterName.setText("Printing Text...");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Disconnect Printer //
    void disconnectBT() throws IOException{
        try {
            BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            stopWorker=true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
            show_message("Printer Disconnected");

            if (!localBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth is OFF", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, About.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
