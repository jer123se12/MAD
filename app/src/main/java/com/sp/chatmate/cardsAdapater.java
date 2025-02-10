package com.sp.chatmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class cardsAdapater extends RecyclerView.Adapter<cardsAdapater.ViewHolder>{
    private Context context;
    private List<folderModel> AC;
    cardsAdapater(Context context, List<folderModel> FM){
        this.context=context;
        this.AC=FM;

    }
    @NonNull
    @Override
    public cardsAdapater.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cards, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull cardsAdapater.ViewHolder holder, int position) {
        folderModel model=AC.get(position);
        holder.name.setText(model.getName());
        boolean isExpandable=model.getExpanded();
        holder.nestedRv.setVisibility(isExpandable? View.VISIBLE:View.GONE);
        if (isExpandable){
            holder.arrow.setImageResource(R.drawable.collpased);
        }else{
            holder.arrow.setImageResource(R.drawable.notcollapsed);
        }
        termAdapater nestedAdapater=new termAdapater(context,model.getCL());
        holder.nestedRv.setAdapter(nestedAdapater);
        holder.nestedRv.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setExpanded(!model.getExpanded());
                notifyItemChanged(position);
            }
        });

    }
    public class  ViewHolder extends RecyclerView.ViewHolder{
        CardView collapse;
        TextView name;
        ImageView arrow;
        RecyclerView nestedRv;
        public ViewHolder(final View itemView){
            super(itemView);
            collapse=itemView.findViewById(R.id.collapse);
            nestedRv=itemView.findViewById(R.id.inner_rv);
            name=itemView.findViewById(R.id.folder_tv);
            arrow=itemView.findViewById(R.id.arrow);
        }
    }

    @Override
    public int getItemCount() {
        return AC.size();
    }
}
