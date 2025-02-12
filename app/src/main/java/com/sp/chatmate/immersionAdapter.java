package com.sp.chatmate;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class immersionAdapter extends RecyclerView.Adapter<immersionAdapter.ViewHolder>{
    private Context context;
    private List<Video> VI;
    vocabHelper helper;
    Handler handler;
    immersionAdapter(Context context, List<Video> FM, vocabHelper helper,Handler handler){
        this.VI=FM;
        this.helper=helper;
        this.context=context;
        this.handler=handler;
    }
    public void updateData(List<Video>vi){
        this.VI=vi;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_immerse, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video v=VI.get(position);
        holder.title.setText(v.title);
        holder.desc.setText(v.desc);
        holder.percentage.setText(String.valueOf(v.percentage));
        v.img.into(holder.thumb);
        holder.click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vie) {
                for(int i=0;i<v.willSee.size();i++){
                    helper.seen(v.willSee.get(i));
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, webView.class);
                        intent.putExtra("url", "https://www.youtube.com/watch?v=" + v.id);
                        context.startActivity(intent);
                    }
                });
            }
        });

    }

    public class  ViewHolder extends RecyclerView.ViewHolder{
        ImageView thumb;
        TextView title;
        TextView desc;
        TextView percentage;
        LinearLayout click;
        public ViewHolder(final View itemView){
            super(itemView);
            thumb=itemView.findViewById(R.id.thumbnail);
            title=itemView.findViewById(R.id.title_immerse);
            desc=itemView.findViewById(R.id.desc_immerse);
            percentage=itemView.findViewById(R.id.percentage);
            click=itemView.findViewById(R.id.clicksensor);
        }
    }

    @Override
    public int getItemCount() {
        return VI.size();
    }
}