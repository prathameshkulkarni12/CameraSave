package com.example.camerasave.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;



import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TAB_Info= "Camera";
    public  static  final String COL_Name="ID";
    public static final String COL_Pass="IMG_PATH";

    public SQLiteHelper(Context context){
        super(context,"Camera",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createInfo="create table " + TAB_Info +" ("+COL_Name + " INT," + COL_Pass+ " String)";
        db.execSQL(createInfo);

    }
    public void addEntry( int id, String image) throws SQLiteException {
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO Camera VALUES (?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1, String.valueOf(id));
        statement.bindString(2, image);

        statement.executeInsert();
    }
    public  String getPath(int id){
        String path = null;
        SQLiteDatabase database=getWritableDatabase();
        String q="Select IMG_PATH from Camera where ID = "+id;
        Cursor cursor=database.rawQuery(q,null);
        while (cursor.moveToNext()){
            path=cursor.getString(cursor.getColumnIndex("IMG_PATH"));
        }
        cursor.close();
        database.close();
        return path;
    }

    public int getID(){
        int id=0;
        SQLiteDatabase database=getWritableDatabase();
        String q="Select MAX(ID) from Camera";
        Cursor cursor=database.rawQuery(q,null);
        while (cursor.moveToNext()){
            id=cursor.getInt(cursor.getColumnIndex("MAX(ID)"));

        }
        cursor.close();
        database.close();
        return id;
    }
    public  void deleteEntry(int id){
        SQLiteDatabase database=getWritableDatabase();
        String q="Delete from Camera Where ID ="+id;
        database.execSQL(q);
        database.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
