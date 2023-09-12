package co.kr.itforone.peertalk;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.ParseException;

public class WebviewJavainterface {
    SQLiteDatabase db;//SQLite db를 가져오기
    MainActivity mainActivity;
    public WebviewJavainterface(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @JavascriptInterface
    public void openserch() {

        mainActivity.choosehp();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @JavascriptInterface
    public void openlist() throws ParseException {

        mainActivity.calledlist();

    }

    @JavascriptInterface
    public void call_number(String number) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+number));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivity.startActivity(i);

    }
    /*
    @JavascriptInterface
    public void tmp_shownoti (String name, String number, String type) {

        mainActivity.shownoti(name,number,type);

    }*/



    @JavascriptInterface
    public void setLogininfo(String id,String password) {
        // Toast.makeText(mainActivity.getApplicationContext(),"setlogin",Toast.LENGTH_LONG).show();
        SharedPreferences pref = mainActivity.getSharedPreferences("logininfo", mainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("id",id);
        editor.putString("pwd",password);
        editor.commit();

    }

    @JavascriptInterface
    public void setlogout() {

        // Toast.makeText(activity.getApplicationContext(),"logout",Toast.LENGTH_LONG).show();
        SharedPreferences pref = mainActivity.getSharedPreferences("logininfo", mainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

    }

    @JavascriptInterface
    public void setContact(int contact_no,String name,String hp,String ranks,String workplace,String profile){
        //휴대폰번호로 조회하고 없으면 insert 하기
        String sql = "select * from address_book where hp = '"+hp+"'";
        Cursor cursor = MainActivity.db.rawQuery(sql,null);

        //여기에 인서트 하기
        if (cursor.moveToFirst() == false){
            Log.d("cursor1","insert");
            ContentValues values = new ContentValues();
            values.put("contact_no",contact_no);
            values.put("name",name);
            values.put("hp",hp);
            values.put("ranks",ranks);
            values.put("workplace",workplace);
            values.put("profile",profile);
            MainActivity.db.insert("address_book",null,values);
        }
    }
    @JavascriptInterface
    public void updateContact(int contact_no,String name,String hp,String ranks,String workplace,String profile){

        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("hp",hp);
        values.put("ranks",ranks);
        values.put("workplace",workplace);
        values.put("profile",profile);
        Log.d("cursor1","update");
        MainActivity.db.update("address_book",values,"contact_no=?",new String[]{String.valueOf(contact_no)});
    }
    @JavascriptInterface
    public void removeContact(String hp){
        String sql = "delete from address_book where hp = '"+hp+"'";
        MainActivity.db.execSQL(sql);

    }
}
