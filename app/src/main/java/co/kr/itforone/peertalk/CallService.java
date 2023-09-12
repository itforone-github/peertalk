package co.kr.itforone.peertalk;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;

import co.kr.itforone.peertalk.Util.ContactDbHelper;

public class CallService extends Service {
    WindowManager.LayoutParams params;
    public static String phoneNumber_extra;
    private final IBinder binder = new CallService.LocalBinder();
    Intent currentIntent;
    NotificationManager manager = null;
    NotificationCompat.Builder builder = null;
    class LocalBinder extends Binder {
        CallService getService() {
            return CallService.this;
        }
    }

    private WindowManager windowManager;

    View rootView;
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.9); //Display 사이즈의 90%
        params = new WindowManager.LayoutParams(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        rootView = layoutInflater.inflate(R.layout.dialog_alarm, null);

        //안드로이드 Oreo에서는 빌드로 들어가야 함
        builder = new NotificationCompat.Builder(getApplicationContext(),"default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("전화");
        builder.setContentText("실행중");
        Intent notificationIntent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default","기본채널",NotificationManager.IMPORTANCE_DEFAULT));
        }

        startForeground(1,builder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            currentIntent = intent;

            TextView tv_type = rootView.findViewById(R.id.tv_type);
            TextView tv_name = rootView.findViewById(R.id.tv_name);
            TextView tv_wr1 = rootView.findViewById(R.id.tv_wr1);
            TextView tv_wr2 = rootView.findViewById(R.id.tv_wr2);
            TextView tv_number = rootView.findViewById(R.id.tv_number);
            Button closeBtn = rootView.findViewById(R.id.closeBtn);

            tv_type.setText(intent.getStringExtra("type"));
            tv_name.setText(intent.getStringExtra("name"));
            tv_wr1.setText(intent.getStringExtra("ranks"));
            tv_wr2.setText(intent.getStringExtra("workplace"));
            tv_number.setText(intent.getStringExtra("phone_number"));
            String number = intent.getStringExtra("phone_number");


            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //sendSMS(number,"문자를 보내드립니다");
                    removePopup();
                }
            });
            Log.d("profile",intent.getStringExtra("profile").length()+"///111");
            if(intent.getStringExtra("profile").length()!=0){
                Glide.with(getApplicationContext())
                        .load(intent.getStringExtra("profile"))
                        .override(50,50)
                        .circleCrop()
                        .into((ImageView) rootView.findViewById(R.id.dialog_pfimg));

            }else{
                Log.d("profile1","no-image");
                Glide.with(getApplicationContext())
                        .load(R.drawable.lnb_mb_noimg)
                        .override(50,50)
                        .circleCrop()
                        .into((ImageView) rootView.findViewById(R.id.dialog_pfimg));
            }

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNumber_extra = intent.getExtras().getString("phone_number");
            SharedPreferences pref = getSharedPreferences("logininfo", MODE_PRIVATE);



            String mb_id = pref.getString("id", "");

            Constraints.Builder builder = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED);
            //Worker에 데이터 보내기 설정
            Data.Builder data = new Data.Builder();

            data.putString("number",phoneNumber_extra);

            windowManager.addView(rootView, params);

            //WorkRequest workRequest = new OneTimeWorkRequest.Builder(ParseWorker.class)
            //      .build();




            setExtra(intent);
        }catch (Exception e){
            Log.d("error",e.toString());
            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }

    private  void setExtra(Intent intent){
        if (intent == null){
            return;
        }

    }
    public void removePopup() {
        try{
            stopSelf();
            if (rootView != null && windowManager != null) windowManager.removeView(rootView);

        }catch (Exception e){

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        try{
            removePopup();
        }catch (Exception e){

        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

/*
    public void sendSMS(String number,String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number,null,message,null,null);
    }*/
}
