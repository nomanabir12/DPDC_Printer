package com.techhexor.dpdcprinter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DB_Helper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "prepaid_meter_database.db";
    private static final String TABLE_NAME = "prepaid_meter_info_table";
    private static final String COL_1 = "_id";
    private static final String COL_2 = "serial_no";
    private static final String COL_3 = "message";
    private static final String COL_4 = "date";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE "+TABLE_NAME+" ("+COL_1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COL_2+" INTEGER, "+COL_3+" VARCHAR(255), "+COL_4+" VARCHAR(255))";


    private static final int DATABASE_VERSION = 1;

    private Context context;



    public DB_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_QUERY);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public int getLastSerialNumber()
    {
        SQLiteDatabase dbDatabase = this.getWritableDatabase();
        Cursor cursor =  dbDatabase.rawQuery("SELECT "+COL_1+" FROM "+TABLE_NAME+" ORDER BY "+COL_1+" DESC LIMIT 1", null);
        int i = 0;
        if (cursor!=null && cursor.moveToFirst()) {
            i = Integer.parseInt(cursor.getString(0).toString());
        }
        return i;
    }

    void show_message(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    public long insertData(int serial_no, String msg, String date) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, serial_no);
        contentValues.put(COL_3, msg);
        contentValues.put(COL_4, date);

        long rowdid = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        return rowdid;
    }

    public Cursor showAllData() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        return cursor;
    }

    public Cursor searchData(String paramString)
    {
        return getWritableDatabase().rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+COL_2+" LIKE '%" + paramString + "%'", null);
    }
}
