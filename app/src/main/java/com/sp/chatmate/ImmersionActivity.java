package com.sp.chatmate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.common.util.concurrent.RateLimiter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.sp.chatmate.immersionAdapter;
class Video{
    String title;
    String desc;
    String id;
    RequestCreator img;
    float percentage;
    String captions;
    List<Integer> willSee;
    Video(String id,String title,String desc,RequestCreator img){
        this.title=title;
        this.id=id;
        this.desc=desc;
        this.img=img;
    }

}

 class RateLimitInterceptor implements Interceptor {
    private RateLimiter rateLimiter = RateLimiter.create(0.25);

    @Override
    public Response intercept(Chain chain) throws IOException {
        rateLimiter.acquire(1);
        return chain.proceed(chain.request());
    }
}

public class ImmersionActivity extends AppCompatActivity {

    EditText text;
    Button search;
    OkHttpClient client = new OkHttpClient();
    List<Video> finalList=new ArrayList<>();
    vocabHelper helper;
    percentageHelper Phelper;
    com.sp.chatmate.immersionAdapter adapter;
    Handler handler;


    final String url="https://youtube.googleapis.com/youtube/v3/search?part=snippet&key=AIzaSyBnh1NvcJEO76kHbhWHTXQllNlxoKzV9A0&q=";
    final String url2="https://api.supadata.ai/v1/youtube/transcript?text=true&lang=ja&url=https://www.youtube.com/watch?v=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_immersion);
        client=new OkHttpClient().newBuilder().addInterceptor(new RateLimitInterceptor()).build();
        handler=new Handler(Looper.getMainLooper());
        helper=new vocabHelper(ImmersionActivity.this, "japanese");
        Phelper=helper.getPercentage();

        new navDrawerInit(
                ImmersionActivity.this,
                findViewById(R.id.nav_view),
                FirebaseAuth.getInstance(),
                FirebaseDatabase.getInstance("https://langify-a017b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users"),
                findViewById(R.id.immerse_layout),
                findViewById(R.id.menu_icon),
                menuItems.IMMERSION
        ).init();

        text=findViewById(R.id.ytquery);
        search=findViewById(R.id.ytsearch);
        RecyclerView recyclerView=findViewById(R.id.immerse_recycler);
        adapter=new immersionAdapter(ImmersionActivity.this,finalList,helper,handler) ;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ImmersionActivity.this));
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query=text.getText().toString();
                // Making a GET request with OkHttp

                Request request = new Request.Builder()
                        .url(url+query)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(ImmersionActivity.this, "please check query", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        JSONObject obj=new JSONObject();
                        List<Video> videoIds=new ArrayList<>();
                        try {
                            obj=new JSONObject(response.body().string());
                            JSONArray videos= obj.getJSONArray("items");
                            for (int i=0;i<videos.length();i++) {
                                JSONObject snippet = videos.getJSONObject(i).getJSONObject("snippet");
                                JSONObject id = videos.getJSONObject(i).getJSONObject("id");
                                if (id.getString("kind").equals("youtube#video")) {
                                    Video v = new Video(id.getString("videoId"), snippet.getString("title"), snippet.getString("description"), Picasso.get().load(snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url")));
                                    videoIds.add(v);

                                    Log.i("vid", id.getString("videoId"));

                                }
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        List<Video> updatedVideos=new ArrayList<>();
                        for (int i=0;i<videoIds.size();i++){
                            Video v=videoIds.get(i);
                            int index=i;
                            Request request = new Request.Builder()
                                    .url(url2+v.id)
                                    .header("x-api-key","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IjEifQ.eyJpc3MiOiJuYWRsZXMiLCJpYXQiOiIxNzM5NDE0MzY2IiwicHVycG9zZSI6ImFwaV9hdXRoZW50aWNhdGlvbiIsInN1YiI6IjE1ZDFmYzM5ZGY2YzQ2NjQ4ZDNiNzk4ZWEwN2MzMWI0In0.St5aY7V9HUUfINTCcCpK0jcNAj8uGTmfdSl6B6TeSAM")
                                    .build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    JSONObject res=new JSONObject();
                                    try{
                                        res=new JSONObject(response.body().string());
                                        if (res.getString("lang").equals("ja")){
                                            v.captions=res.getString("content");
                                            updatedVideos.add(v);
                                        }
                                        parseVideos(updatedVideos);

                                    }catch (Exception e){
                                        Log.e("error",e.toString());
                                    }
                                }
                            });


                        }

                    }
                });

            }
        });

    }
    public void parseVideos(List<Video> videos){
        for (int i=0;i<videos.size();i++){
            Video v=videos.get(i);
            videos.set(i,Phelper.getPercentage(v));
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.updateData(videos);

            }
        });
    }
}

