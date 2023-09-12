package co.kr.itforone.peertalk;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import co.kr.itforone.peertalk.retrofit.RetrofitAPI;
import co.kr.itforone.peertalk.retrofit.RetrofitHelper;
import co.kr.itforone.peertalk.retrofit.responseModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("WorkerHasAPublicModifier")
public class ParseWorker extends Worker {
    Context mContext;

    public ParseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext=context;
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @NonNull
    @Override
    public Result doWork() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String hp = getInputData().getString("number");

        SharedPreferences pref = mContext.getSharedPreferences("logininfo", MODE_PRIVATE);
        String mb_id = pref.getString("id", "");

        RetrofitAPI networkService = RetrofitHelper.getRetrofit().create(RetrofitAPI.class);
        try{
            Log.d("dowork","dowork");
            Call<responseModel> call = networkService.getList(mb_id, hp);
            call.execute();
            mContext.getMainExecutor().execute(() -> Toast.makeText(mContext, "전송", Toast.LENGTH_SHORT).show());
           /* Response<responseModel> response = call.execute();
            responseModel data = null;
            Log.d("http-code",response.code()+"");
            Log.d("is_success",response.isSuccessful()+"");
            if(response.code() == 200){
                data = response.body();
            }else{
                data = null;
            }*/

        }catch (Exception e){
            mContext.getMainExecutor().execute(() -> Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show());
            Log.d("error",e.toString());

        }





        return Result.success();
    }

}
