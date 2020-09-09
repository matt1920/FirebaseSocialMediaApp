package com.example.firebasesocialmediaapp;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView postListView;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ImageView sentPostImageView;
    private TextView txtDescription;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        sentPostImageView = findViewById(R.id.sentPostsImageView);
        txtDescription = findViewById(R.id.txtDescription);

        firebaseAuth = FirebaseAuth.getInstance();

        postListView = findViewById(R.id.postListView);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        postListView.setAdapter(adapter);
        dataSnapshots = new ArrayList<>();
        postListView.setOnItemClickListener(this);
        postListView.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                dataSnapshots.add(dataSnapshot);
                String fromWhomUsername = (String) dataSnapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i = 0;

                for (DataSnapshot snapshot : dataSnapshots){
                    if (snapshot.getKey().equals(dataSnapshot.getKey())){
                        dataSnapshots.remove(i);
                        usernames.remove(i);

                    }
                    i++;
                }
                adapter.notifyDataSetChanged();
                sentPostImageView.setImageResource(R.drawable.placeholder);
                txtDescription.setText("");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        DataSnapshot myDataSnapshot = dataSnapshots.get(i);
        String downloadLink = (String) myDataSnapshot.child("imageLink").getValue();
        Picasso.get().load(downloadLink).into(sentPostImageView);
        txtDescription.setText((String) myDataSnapshot.child("des").getValue());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {

            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_images").child((String)dataSnapshots
                                .get(i).child("imageIdentifier").getValue())
                                .removeValue();

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(firebaseAuth.getCurrentUser()
                        .getUid()).child("received_posts")
                                .child(dataSnapshots.get(i).getKey()).removeValue();

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return false;
    }
}