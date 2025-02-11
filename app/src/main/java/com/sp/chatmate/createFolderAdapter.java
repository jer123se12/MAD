package com.sp.chatmate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class createFolderAdapter extends RecyclerView.Adapter<createFolderAdapter.ViewHolder>{
    private Context context;
    private List<Card> AC=new ArrayList<>();
    private List<Card> folderCards=new ArrayList<>();
    vocabHelper helper;
    createFolderAdapter(Context context,vocabHelper helper){
        this.helper=helper;

    }
    public List<Card> getFolderCards(){
        return folderCards;
    }
    public void setFolderCards(List<Card> cards){
        this.folderCards=cards;
    }
    public void search(String term){

        this.AC=helper.search(term);
        Log.i("length of array",String.valueOf(this.AC.size()));
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public createFolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_create_folder, parent, false);
        context= parent.getContext();
        return new createFolderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull createFolderAdapter.ViewHolder holder, int position) {
        Card model = AC.get(position);
        Log.i("term", model.term);
        holder.ter.setText(model.term);
        holder.def.setText(model.definition.replace("\n", " "));
        for(int i=0;i<folderCards.size();i++){
            if(folderCards.get(i).id==model.id){
                holder.delete_btn.setText("remove");
                break;
            }
        }
        holder.id = model.id;
        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<folderCards.size();i++){
                    if(folderCards.get(i).id==model.id){
                        folderCards.remove(i);
                        notifyItemChanged(position);
                        return;
                    }
                }
                folderCards.add(helper.getCard(model.id));
                holder.delete_btn.setText("remove");
                notifyItemChanged(position);

            }
        });
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView ter;
        TextView def;
        int id;
        Button delete_btn;

        public ViewHolder(final View itemView){
            super(itemView);
            ter=itemView.findViewById(R.id.term_tv);
            delete_btn=itemView.findViewById(R.id.delete_btn);
            def=itemView.findViewById(R.id.def_tv);
        }
    }

    @Override
    public int getItemCount() {
        return AC.size();
    }
}
