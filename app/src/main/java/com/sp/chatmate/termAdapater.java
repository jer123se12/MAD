package com.sp.chatmate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class termAdapater extends RecyclerView.Adapter<termAdapater.ViewHolder>{
    private Context context;
    private List<Card> AC;
    termAdapater(Context context, List<Card> FM){
        this.AC=FM;

    }
    @NonNull
    @Override
    public termAdapater.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_term, parent, false);
        return new termAdapater.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull termAdapater.ViewHolder holder, int position) {
        Card model=AC.get(position);
        holder.ter.setText(model.term);
        holder.def.setText(model.definition);
        holder.id=model.id;

    }
    public class  ViewHolder extends RecyclerView.ViewHolder{
        TextView ter;
        TextView def;
        int id;
        public ViewHolder(final View itemView){
            super(itemView);
            ter=itemView.findViewById(R.id.term_tv);
            def=itemView.findViewById(R.id.def_tv);
        }
    }

    @Override
    public int getItemCount() {
        return AC.size();
    }
}
