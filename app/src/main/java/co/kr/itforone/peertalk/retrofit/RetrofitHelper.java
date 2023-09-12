package co.kr.itforone.peertalk.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    public static RetrofitAPI getApiService(){

        return getRetrofit().create(RetrofitAPI.class);

    }

    public static Retrofit getRetrofit(){

        Gson gson = new GsonBuilder().setLenient().create();
        //http 로그를 보여주기(가로채기)
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(100, TimeUnit.MINUTES)
                .readTimeout(100, TimeUnit.MINUTES)
                .writeTimeout(100, TimeUnit.MINUTES)
                .build();
        //http://3.35.13.79/test_post/
        //https://itforone.co.kr/~peertalk/
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://itforone.co.kr/~peertalk/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    }

}
