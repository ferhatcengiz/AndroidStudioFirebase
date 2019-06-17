package com.brute.ferhat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersListActivity extends AppCompatActivity {

    private RecyclerView allUsersList;
    private DatabaseReference PostRef;
    private RecyclerView AllUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users_list);

        PostRef = FirebaseDatabase.getInstance().getReference().child("Users");

        AllUsersList = (RecyclerView)findViewById(R.id.all_users_list);
        AllUsersList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        AllUsersList.setLayoutManager(linearLayoutManager);

        DisplayAllUsers();
    }

    private void DisplayAllUsers()
    {
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Users, UsersViewHolder>
                (
                        Users.class,
                        R.layout.all_users_layout,
                        UsersViewHolder.class,
                        PostRef

                ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position)
            {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
            }
        };
        AllUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public UsersViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
        }
        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.all_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

    }
}