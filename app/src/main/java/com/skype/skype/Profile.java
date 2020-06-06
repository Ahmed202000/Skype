package com.skype.skype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.IllegalFormatCodePointException;

public class Profile extends AppCompatActivity {

    private String receiverUserID ="",receiverUserImage ="",receiverUserName ="";
    private ImageView background_profile_view;
    private TextView name_profil;
    private Button add_friends_request,decline_friends_request;

    private FirebaseAuth mAuth;
    private String SendUserID;
    private String currentStatue="new";
    private DatabaseReference friendRequestRef ,contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        SendUserID=mAuth.getCurrentUser().getUid();
        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friends Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID=getIntent().getExtras().get("user_id").toString();
        receiverUserImage=getIntent().getExtras().get("profile_image").toString();
        receiverUserName=getIntent().getExtras().get("profile_name").toString();


        background_profile_view=findViewById(R.id.background_profile_view);
        name_profil=findViewById(R.id.name_profil);
        add_friends_request=findViewById(R.id.add_friends_request);
        decline_friends_request=findViewById(R.id.decline_friends_request);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profil.setText(receiverUserName);


        mangeClickEvents();
    }

    private void mangeClickEvents()
    {

        friendRequestRef.child(SendUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String requestType=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if (requestType.equals("sent"))
                            {
                                currentStatue="request_sent";
                                add_friends_request.setText("Cancel Friend Request");
                            }
                            else if (requestType.equals("received"))
                            {
                                currentStatue="request_received";
                                add_friends_request.setText("Accept Friend Request");
                                decline_friends_request.setVisibility(View.VISIBLE);

                                decline_friends_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactsRef.child(SendUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                currentStatue="friends";
                                                add_friends_request.setText("Delete Contact");
                                            }
                                            else
                                            {
                                                currentStatue="new";
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });


        if (SendUserID.equals(receiverUserID))
        {
            add_friends_request.setVisibility(View.GONE);
        }
        else
        {
            add_friends_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (currentStatue.equals("new"))
                    {
                        SendFriendRequest();
                    }
                    if (currentStatue.equals("request_sent"))
                    {
                        CancelFriendRequest()
;                    }
                    if (currentStatue.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                   if (currentStatue.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                }
            });
        }
    }

    private void AcceptFriendRequest()
    {
        contactsRef.child(SendUserID).child(receiverUserID)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(SendUserID)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.child(SendUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(receiverUserID).child(SendUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        currentStatue="friends";
                                                                                        add_friends_request.setText("Delete Contact");

                                                                                        decline_friends_request.setVisibility(View.GONE);
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

    private void CancelFriendRequest()
    {
        friendRequestRef.child(SendUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendRequestRef.child(receiverUserID).child(SendUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                currentStatue="new";
                                                add_friends_request.setText("Add Friend");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendFriendRequest()
    {
        friendRequestRef.child(SendUserID).child(receiverUserID)
                .child("request_type").setValue("send")

                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                       if (task.isSuccessful())
                       {
                           friendRequestRef.child(receiverUserID).child(SendUserID)
                                   .child("request_type").setValue("received")
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task)
                                       {
                                           if (task.isSuccessful())
                                           {
                                               currentStatue="request_sent";
                                               add_friends_request.setText("Cancel Friend Request");
                                               Toast.makeText(Profile.this, "Friend Request Send", Toast.LENGTH_SHORT).show();
                                           }
                                       }
                                   });
                       }
                    }
                });
    }
}
