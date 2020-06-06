package com.skype.skype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Notifications extends AppCompatActivity {

    RecyclerView notifications_rv_find_people_list;
    private DatabaseReference friendRequestRef ,contactsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friends Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");


        notifications_rv_find_people_list=findViewById(R.id.activity_notifications_rv_find_people_list);
        notifications_rv_find_people_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(friendRequestRef.child(currentUserID),Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,NotificationsViewHolder>firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options ) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationsViewHolder holder,
                                            int i, @NonNull Contacts model)
            {
                holder.accept_friend_request.setVisibility(View.VISIBLE);
                holder.cancel_friend_request.setVisibility(View.VISIBLE);

               final String listUserId=getRef(i).getKey();
                DatabaseReference requestTypeRef=getRef(i).child("request_type").getRef();

                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            String type=dataSnapshot.getValue().toString();

                            if (type.equals("received"))
                            {
                                holder.cartView.setVisibility(View.VISIBLE);

                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild("image"))
                                        {
                                            final String imageStr=dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(imageStr).into(holder.img_find_people);

                                        }
                                        final String nameStr=dataSnapshot.child("name").getValue().toString();
                                        holder.find_people_user.setText(nameStr);


                                        //btn Accept
                                        holder.accept_friend_request.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                contactsRef.child(currentUserID).child(listUserId)
                                                        .child("Contact").setValue("Saved")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    contactsRef.child(listUserId).child(currentUserID)
                                                                            .child("Contact").setValue("Saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        friendRequestRef.child(currentUserID).child(listUserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if (task.isSuccessful())
                                                                                                        {
                                                                                                            friendRequestRef.child(listUserId).child(currentUserID)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                                        {
                                                                                                                            if (task.isSuccessful())
                                                                                                                            {
                                                                                                                                Toast.makeText(Notifications.this, "New Contact Saved ", Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });
                                            }
                                        });


                                        //btn Cancel
                                        holder.cancel_friend_request.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                friendRequestRef.child(currentUserID).child(listUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(listUserId).child(currentUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        Toast.makeText(Notifications.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });                                            }
                                        });
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else
                            {
                                holder.cartView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType)
            {
                View view= LayoutInflater.from(p.getContext()).inflate(R.layout.item_find_people,p,false);
                NotificationsViewHolder viewHolder=new NotificationsViewHolder(view);

                return viewHolder;
            }
        };
        notifications_rv_find_people_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    //Adapter
    public static class NotificationsViewHolder extends RecyclerView.ViewHolder
    {

        TextView find_people_user;
        Button cancel_friend_request,accept_friend_request;
        ImageView img_find_people;
        RelativeLayout cartView;

        public NotificationsViewHolder(@NonNull View item_view) {
            super(item_view);

            find_people_user=item_view.findViewById(R.id.item_find_people_tv_find_people);
            img_find_people=item_view.findViewById(R.id.item_find_people_img_find_people);
            cartView=item_view.findViewById(R.id.item_find_people_cart_view);
            cancel_friend_request=item_view.findViewById(R.id.item_find_people_btn_cancel_friend_request);
            accept_friend_request=item_view.findViewById(R.id.item_find_people_btn_accept_friend_request);

        }
    }
}
