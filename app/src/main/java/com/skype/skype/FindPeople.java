package com.skype.skype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.sql.Ref;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPeople extends AppCompatActivity {


    private RecyclerView find_people_list;
    private EditText search;

    private String str="";
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        search=findViewById(R.id.activity_find_people_tv_search);
        find_people_list=findViewById(R.id.activity_find_people_rv_find_people_list);
        find_people_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        //Text Search
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSeq, int start, int before, int count)
            {
                if (search.getText().toString().equals(""))
                {
                    Toast.makeText(FindPeople.this, "Please enter the search word", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    str=charSeq.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options=null;
        if (str.equals(""))
        {
            options=
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef,Contacts.class)
                    .build();
        }
        else
        {
            options=
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(userRef
                                    .orderByChild("name").startAt(str)
                                    .endAt(str + "\uf8ff")
                                    ,Contacts.class)
                            .build();
        }
        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts  model)
                    {
                        holder.contact_user.setText(model.getName());
                        Picasso.get().load(model.getImage()).into(holder.contact_people);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                String user_id= getRef(position).getKey();
                                Intent intent=new Intent(FindPeople.this,Profile.class);
                                intent.putExtra("user_id",user_id);
                                intent.putExtra("profile_image",model.getImage());
                                intent.putExtra("profile_name",model.getName());
                                startActivity(intent);
                            }
                        });


                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType)
                    {
                        View view= LayoutInflater.from(p.getContext()).inflate(R.layout.item_contact,p,false);
                        FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);

                        return viewHolder;
                    }
                };
        find_people_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //Adapter
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {

        TextView contact_user;
        Button call,call_video;
        ImageView contact_people;
        RelativeLayout contact_cartView,item_contact_rl;

        public FindFriendsViewHolder(@NonNull View item_view) {
            super(item_view);

            contact_user=item_view.findViewById(R.id.item_contact_tv_find_people);
            contact_people=item_view.findViewById(R.id.item_contact_img_find_people);
            contact_cartView=item_view.findViewById(R.id.item_contact_cart_view);

        }
    }
}
