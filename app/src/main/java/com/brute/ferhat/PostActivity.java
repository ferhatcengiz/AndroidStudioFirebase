package com.brute.ferhat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private static final int Gallery_pick = 1;              //****** open galery için
    private Uri ImageUri;
    private String Description;

    private StorageReference PostImagesReference;       //****** gönderilen fotografın referansı
    private DatabaseReference UsersRef,PostsRef;            //********** firebase'de databaseden userref ve postref'i çekmek için
    private FirebaseAuth mAuth;

    private  String saveCurrentDate, saveCurrentTime,postRandomName,downloadUrl,current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                                                //************ onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();                         //mevcut kullanıcya ulaşmak için
        current_user_id = mAuth.getCurrentUser().getUid();          //mevcut kullanıcının idsini firebaseden çektik

        PostImagesReference = FirebaseStorage.getInstance().getReference();
        //****** gönderilen fotografın referansı çekilir
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");        //userref firebaseden çekmek için(firebase de users diye child olusturur)
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");        //postsref firebase databaseden çekmek için

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);           //post act içindeki img text button
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription =(EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);           //toolbar yani geri tusu
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {                 //****** Select Post Image
            @Override
            public void onClick(View v)
            {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {                //****** Update Post Button
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();                                                     // fotoğraf yükle için doğrulama yani her sey doluysa okey
            }
        });


    }

    private void ValidatePostInfo()                                         //fotoğraf yükle için doğrulama yani her sey dolu mu??
    {
        Description = PostDescription.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();                                                    //firebase storage'e kaydetme
        }
    }

    private void StoringImageToFirebaseStorage()                                                //firebase storage'e kaydetme ?????????????
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filepath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");    //****

        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show();



                    SavingPostInformationToDatabase();

                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SavingPostInformationToDatabase()
    {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();                     //userın fullnameini çektik
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();             //userın ppsini çektik

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);

                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        SendUserToMainActiviy();
                                        Toast.makeText(PostActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(PostActivity.this, "Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {            }
        });
    }

    private void OpenGallery()                      //****** Galeri açma
    {
        Intent galleryIntent = new Intent();                                // Setup Activity copy+paste yapdık
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_pick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)           //image kaydetme
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)                 //post activity'de iken geri tusuna basıp home sayfasına gider
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActiviy();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActiviy()                    //main act gider
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
