package co.kr.itforone.peertalk.Util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ContactDbHelper extends SQLiteOpenHelper {
    public ContactDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    //db 생성
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = " CREATE TABLE if not exists address_book("
                    +"id integer primary key autoincrement,"
                    +"contact_no integer,"
                    +"user_id TEXT,"
                    +"name TEXT,"
                    +"hp TEXT,"
                    +"ranks TEXT,"
                    +"workplace TEXT,"
                    +"profile TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE if exists address_book";
        db.execSQL(sql);
        onCreate(db);
    }
}
