package com.brute.ferhat;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;                                       //*******************navigation
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;                                          //all users posts recycler View
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;        //navdaki pp
    private TextView NavProfileUserName;            //nav username
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostsRef;                            //****** User Extence           //PostsRef firebaseden Posts child'ın verileri çekilir

    String currentUserID;                   //mevcut kullanıcı idsi

    @Override
    protected void onCreate(Bundle savedInstanceState) {                       //************onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();            //***
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");   //****** User
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");    //******* Posts ların child'ı


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);              //en üst kısım
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Anasayfa");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);            //sağ üstteki image butonu

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);                                                         //***drawer layout
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open,R.string.drawer_closer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);

        postList = (RecyclerView)findViewById(R.id.all_users_post_list);                        //recycler view için listeleme
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);            //nav view
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);       //nav img
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);          //nav username

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {          //Nav'daki pp ve username değişimi
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {                //navigation menu ıtem
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) 
            {
                UserMenuSelector(item);
                return false;
            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {        //sağ üst image butonu
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts();                                 //***************** tüm kullanıcıların postları için
    }



    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =               //Post'dan çekme
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_post_layout,
                                PostsViewHolder.class,
                                PostsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                        final  String PostKey = getRef(position).getKey();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());


/*
                      viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickActivity.class );
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

*/
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }

    private void SendUserToPostActivity()                                   //*** mainact iken post atmak için postact e gider
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();              //Eger kullanıcı girişi yapılmamışsa Login Activity'e git
        }
        else
        {
            CheckUserExistence();                   //Eger kullanıcı mevcut ise
        }
    }
    private void CheckUserExistence()                                     //kullanıcının varlığını kontrol et??????????????
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void SendUserToSetupActivity()                                  //*************** Setup Activity
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
    }



    private void SendUserToLoginActivity()                                     //*****************Login Activity
    {
        Intent loginIntent = new Intent( MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)                       //************toggle için
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void UserMenuSelector(MenuItem item)                    ///************sol menu secimi
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_settings:
                SendUserToSetupActivity();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
            case R.id.users:
                mAuth.signOut();
                SendToUsersActivity();
                break;

        }
    }

   private void SendToUsersActivity()
    {
        Intent usersIntent = new Intent( MainActivity.this,AllUsersListActivity.class);
        startActivity(usersIntent);
        finish();
    }

}
