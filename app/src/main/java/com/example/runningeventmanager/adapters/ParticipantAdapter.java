package com.example.runningeventmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.models.Registration;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {
    
    private final Context context;
    private List<Registration> participantList;
    private final OnParticipantClickListener listener;
    
    public interface OnParticipantClickListener {
        void onParticipantClick(Registration registration);
    }
    
    public ParticipantAdapter(Context context, List<Registration> participantList, OnParticipantClickListener listener) {
        this.context = context;
        this.participantList = participantList;
        this.listener = listener;
    }
    
    public void updateParticipantList(List<Registration> newParticipantList) {
        this.participantList = newParticipantList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.participant_list_item, parent, false);
        return new ParticipantViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Registration registration = participantList.get(position);
        
        holder.participantNameTextView.setText(registration.getUserName());
        holder.registrationDateTextView.setText("Registered on: " + registration.getCreatedAt());
        holder.registrationStatusTextView.setText(registration.getStatus());
        
        // Set status color based on registration status
        switch (registration.getStatus()) {
            case "REGISTERED":
                holder.registrationStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "COMPLETED":
                holder.registrationStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "CANCELLED":
                holder.registrationStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                holder.registrationStatusTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onParticipantClick(registration);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return participantList != null ? participantList.size() : 0;
    }
    
    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        CircleImageView participantImageView;
        TextView participantNameTextView;
        TextView registrationDateTextView;
        TextView registrationStatusTextView;
        
        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            participantImageView = itemView.findViewById(R.id.participantImageView);
            participantNameTextView = itemView.findViewById(R.id.participantNameTextView);
            registrationDateTextView = itemView.findViewById(R.id.registrationDateTextView);
            registrationStatusTextView = itemView.findViewById(R.id.registrationStatusTextView);
        }
    }
} 