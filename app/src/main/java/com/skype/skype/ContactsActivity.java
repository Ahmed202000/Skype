package com.skype.skype;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView contacts_list;
    ImageView findPeople;

    private DatabaseReference friendRequestRef ,contactsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private String userName="",profileImage="";
    private String calleBy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");


        //findPeople
        findPeople=findViewById(R.id.activity_contacts_btn_find_people);
        findPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(ContactsActivity.this,FindPeople.class));
            }
        });


        //contacts_list
        contacts_list= findViewById(R.id.activity_contacts_rv_contacts_list);
        contacts_list.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));




        //BottomNavigationView
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {

            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                    break;

                case R.id.navigation_notifications:
                    startActivity(new Intent(ContactsActivity.this, Notifications.class));
                    break;

                case R.id.navigation_setting:
                    startActivity(new Intent(ContactsActivity.this,Setting.class));
                    break;

                case R.id.navigation_logoUt:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ContactsActivity.this,Registration.class));
                    finish();
                    break;
            }

            return true;
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();

        checkForReceivingCall();


        ValidetUser();

        FirebaseRecyclerOptions<Contacts>options
                =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder>firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int i, @NonNull Contacts mdle)
            {
                final String listUserId=getRef(i).getKey();

                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            userName=dataSnapshot.child("name").getValue().toString();
                            profileImage=dataSnapshot.child("image").getValue().toString();

                            holder.find_people_user.setText(userName);
                            Picasso.get().load(profileImage).into(holder.img_find_people);
                        }
                        holder.item_contact_1_btn_call_video.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent callVedioIntent=new Intent(ContactsActivity.this, CallingActivity.class);
                                callVedioIntent.putExtra("user_id",listUserId);
                                startActivity(callVedioIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_1,parent,false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);

                return viewHolder;
            }
        };
        contacts_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall()
    {
        userRef.child(currentUserID)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("ringing"))
                        {
                            calleBy=dataSnapshot.child("ringing").getValue().toString();

                            Intent callingIntent=new Intent(ContactsActivity.this, CallingActivity.class);
                            callingIntent.putExtra("user_id",calleBy);
                            startActivity(callingIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                    }
                });
    }

    private void ValidetUser()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists())
                {
                    Intent settingIntent=new Intent(ContactsActivity.this,Setting.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    //Adapter
    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {

        TextView find_people_user;
        ImageView item_contact_1_btn_call_video,item_contact_1_btn_call;
        CircleImageView img_find_people;

        public ContactsViewHolder(@NonNull View item_view) {
            super(item_view);

            find_people_user=item_view.findViewById(R.id.item_contact_1_tv_find_people);
            img_find_people=item_view.findViewById(R.id.item_contact_1_img_find_people);
            item_contact_1_btn_call=item_view.findViewById(R.id.item_contact_1_btn_call);
            item_contact_1_btn_call_video=item_view.findViewById(R.id.item_contact_1_btn_call_video);

        }
    }

}
