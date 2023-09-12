package co.kr.itforone.peertalk;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;

import java.util.List;

import co.kr.itforone.peertalk.Util.ContactDbHelper;
import co.kr.itforone.peertalk.Util.Dialog_manager;
import co.kr.itforone.peertalk.retrofit.RetrofitAPI;
import co.kr.itforone.peertalk.retrofit.RetrofitHelper;
import co.kr.itforone.peertalk.retrofit.responseModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Callin extends BroadcastReceiver {
    WindowManager.LayoutParams params;
    private WindowManager windowManager;
    protected View rootView;
    private Context context_public;
    Intent serviceIntent;
    private  int incomming_flg = 0;
    private Dialog_manager am = Dialog_manager.getInstance();
    private int running_flg = 0;
    private RetrofitAPI retrofitAPI;
    Intent serviceIntent2;
    public SQLiteDatabase db;//SQLite db를 가져오기
    public ContactDbHelper helper;//SQLite db 가져오기

    public void shownoti(String name, String numbers, String type){

        //LayoutInflater layoutInflater = context_public.getAc().getLayoutInflater();


        RemoteViews remoteViews = new RemoteViews(context_public.getPackageName(), R.layout.dialog_alarm);
        remoteViews.setTextViewText(R.id.tv_type, type);
        remoteViews.setTextViewText(R.id.tv_name,  name);
        remoteViews.setTextViewText(R.id.tv_number,  numbers);
        String channelId = "peertalk";
        Intent intent = new Intent(context_public.getApplicationContext(),Calling.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setClass(this, MainActivity.class);

        PendingIntent fullscreen = PendingIntent.getActivity(context_public.getApplicationContext(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context_public.getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .setContentTitle(type)
                        .setContentText(numbers)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(remoteViews)
                      //  .setContent(remoteViews)
                       // .setCustomBigContentView(remoteViews)
                        .setFullScreenIntent(fullscreen,true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


        NotificationManager notificationManager =
                (NotificationManager) context_public.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "peertalk",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

        }

        notificationManager.notify(3001 /* ID of notification */, notificationBuilder.build());

    }
    //전화걸 때
    @Override
    public void onReceive(Context context, Intent intent) {

        /* db 가져오기 */
        helper = new ContactDbHelper(context,"contact.db",null,1);
        db = helper.getWritableDatabase();
        helper.onCreate(db);

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> proceses = am.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo process : proceses){

            if(process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){

                if(process.processName.equals(context.getPackageName())){
                    running_flg=1;
                }
                else{
                    running_flg=0;
                }
            }

        }

        context_public = context;
        String action = intent.getAction();
        Log.d("test_call-action", action);

        if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)){

            String savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            Log.d("test_call", "outcall_"+savedNumber);

        /*    Intent serviceIntent = new Intent(context,Calling.class);
            serviceIntent.putExtra("number", savedNumber);
            context.startService(serviceIntent);*/
            incomming_flg=0;

        }

        else {

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNumber_extra = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);


            PhoneStateListener phoneStateListener = new PhoneStateListener(){

                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    Log.d("test_call_number1", String.valueOf(state));
                    if(phoneNumber_extra!=null && !phoneNumber_extra.isEmpty()) {
                        switch (state) {
                            //전화가 올 때
                            case TelephonyManager.CALL_STATE_RINGING:
                                //Toast.makeText(context_public.getApplicationContext(), "현재 " + phoneNumber_extra + " 번호로 통화가 오는중입니다.", Toast.LENGTH_LONG).show();
                                Log.d("test_call", "ringing");
                                incomming_flg=1;
                                if(running_flg==0) {
                                    //  주소록 동기화
                                    SharedPreferences pref = context.getSharedPreferences("logininfo", context.MODE_PRIVATE);
                                    String mb_id = pref.getString("id", "");
                                    serviceIntent2 = new Intent(context, CallService.class);
                                    serviceIntent2.putExtra("phone_number",phoneNumber_extra);
                                    String sql ="select * from address_book where hp='"+phoneNumber_extra+"'";
                                    Cursor cursor = db.rawQuery(sql,null);
                                    if(cursor.moveToFirst()){
                                        Log.d("profile11",cursor.getString(7));
                                        serviceIntent2.putExtra("type","수신중");
                                        serviceIntent2.putExtra("name",cursor.getString(3));
                                        serviceIntent2.putExtra("ranks",cursor.getString(5));
                                        serviceIntent2.putExtra("workplace",cursor.getString(6));
                                        serviceIntent2.putExtra("profile",cursor.getString(7));
                                        Log.d("hp111",phoneNumber_extra);
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                context.startForegroundService(serviceIntent2);
                                            } else {
                                                context.startService(serviceIntent2);
                                            }
                                        }catch(Exception e){

                                        }
                                    }
                                }

                                break;
                            //전화가 끊길 때
                            case TelephonyManager.CALL_STATE_IDLE:
                               // context.stopService(serviceIntent2);

                                //Toast.makeText(context_public.getApplicationContext(), "현재 " + phoneNumber_extra + " 번호로 통화가 종료되었습니다.", Toast.LENGTH_LONG).show();
                                Log.d("test_call", "disconnect");
                                break;
                            //전화를 할 때
                            case TelephonyManager.CALL_STATE_OFFHOOK:

                                if(incomming_flg==0)
                                {

                                    //  주소록 동기화
                                    SharedPreferences pref = context.getSharedPreferences("logininfo", context.MODE_PRIVATE);
                                    String mb_id = pref.getString("id", "");
                                    serviceIntent2 = new Intent(context, CallService.class);
                                    serviceIntent2.putExtra("phone_number",phoneNumber_extra);
                                    String sql ="select * from address_book where hp='"+phoneNumber_extra+"'";
                                    Cursor cursor = db.rawQuery(sql,null);
                                    if(cursor.moveToFirst()){
                                        serviceIntent2.putExtra("type","발신중");
                                        serviceIntent2.putExtra("name",cursor.getString(3));
                                        serviceIntent2.putExtra("ranks",cursor.getString(5));
                                        serviceIntent2.putExtra("workplace",cursor.getString(6));
                                        serviceIntent2.putExtra("profile",cursor.getString(7));
                                        Log.d("hp111",phoneNumber_extra);

                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                context.startForegroundService(serviceIntent2);
                                            } else {
                                                context.startService(serviceIntent2);
                                            }
                                        }catch(Exception e){

                                        }
                                    }


                                }
                                Log.d("test_call", "connect");
                                break;

                        }
                    }
                }
            };

            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);



        }
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    };
}

