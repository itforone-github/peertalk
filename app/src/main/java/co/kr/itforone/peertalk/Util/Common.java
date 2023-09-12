package co.kr.itforone.peertalk.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Common {
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getMyDeviceId(Context mContext) {
        String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public static void setDateTime(Context context){
        SimpleDateFormat format1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        String format_time1 = format1.format (System.currentTimeMillis());
        SharedPreferences pref = context.getSharedPreferences("logininfo", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("date",format_time1);
        editor.commit();
    }

    public static String getDateTime(Context context){
        SharedPreferences pref = context.getSharedPreferences("logininfo", context.MODE_PRIVATE);
        return pref.getString("date","");
    }
}
