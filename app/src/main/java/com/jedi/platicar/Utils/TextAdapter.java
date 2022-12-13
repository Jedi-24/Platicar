package com.jedi.platicar.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.jedi.platicar.Models.TextModal;
import com.jedi.platicar.R;

import java.util.ArrayList;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.TextViewHolder> {

    private ArrayList<TextModal> texts;
    private FirebaseAuth mAuth;

    public TextAdapter(ArrayList<TextModal> texts){
        this.texts = texts;
    }

    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_texts_layout,parent,false);
        TextViewHolder holder = new TextViewHolder(view);

        mAuth = FirebaseAuth.getInstance();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
        String currUserId = mAuth.getCurrentUser().getUid();

        TextModal text = texts.get(position);

        String fromUid = text.getFrom();
        String fromTextType = text.getType();

        if(fromTextType.equals("text")){
            if(fromUid.equals(currUserId)){ // current user is the sender;
                holder.receiverTexts.setVisibility(View.INVISIBLE);
                holder.senderTexts.setVisibility(View.VISIBLE);
                holder.senderTexts.setText(text.getMessage());
            }
            else{
                holder.senderTexts.setVisibility(View.INVISIBLE);
                holder.receiverTexts.setVisibility(View.VISIBLE);
                holder.receiverTexts.setText(text.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder{

        TextView receiverTexts, senderTexts;
        public TextViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverTexts = itemView.findViewById(R.id.receiver_text_);
            senderTexts = itemView.findViewById(R.id.sender_text_);
        }

    }
}
