package com.developeralamin.blood;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.developeralamin.blood.Adapter.UserAdapter;
import com.developeralamin.blood.auth.LoginActivity;
import com.developeralamin.blood.email.SentemailActivity;
import com.developeralamin.blood.group.CategorySelectedActivity;
import com.developeralamin.blood.model.User;
import com.developeralamin.blood.notification.NotificationsActivity;
import com.developeralamin.blood.profile.ProfileActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    ProgressBar progressbar;
    RecyclerView recyclerView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private CircleImageView nav_user_image;
    private TextView nav_user_name, nav_user_email, nav_user_bloodgroup, nav_user_type;
    private DatabaseReference databaseReference;

    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        toolbar = findViewById(R.id.toobar);
        progressbar = findViewById(R.id.progressbar);
        recyclerView = findViewById(R.id.recyclerView);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MainActivity.this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


        userList = new ArrayList<>();
        userAdapter = new UserAdapter(MainActivity.this, userList);

        recyclerView.setAdapter(userAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = snapshot.child("type").getValue().toString();
                if (type.equals("donor")){
                    readRecipients();
                }else {
                    readDonors();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        nav_user_image = navigationView.getHeaderView(0).findViewById(R.id.nav_user_image);
        nav_user_name = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name);
        nav_user_email = navigationView.getHeaderView(0).findViewById(R.id.nav_user_email);
        nav_user_bloodgroup = navigationView.getHeaderView(0).findViewById(R.id.nav_user_bloodgroup);
        nav_user_type = navigationView.getHeaderView(0).findViewById(R.id.nav_user_type);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String name = snapshot.child("name").getValue().toString();
                    nav_user_name.setText(name);

                    String email = snapshot.child("email").getValue().toString();
                    nav_user_email.setText(email);

                    String bloodgroup = snapshot.child("bloodgroup").getValue().toString();
                    nav_user_bloodgroup.setText(bloodgroup);

                    String type = snapshot.child("type").getValue().toString();
                    nav_user_type.setText(type);

//                    String imageUrl = snapshot.child("profilepictureurl").getValue().toString();
//                    if (!imageUrl.equals("no_pic_uploaded")) {
//                        Glide.with(getApplicationContext()).load(imageUrl).into(nav_user_image);
//
//                    }

                    if (snapshot.hasChild("profilepictureurl")){
                        String imageUrl = snapshot.child("profilepictureurl").getValue().toString();
                        Glide.with(getApplicationContext()).load(imageUrl).into(nav_user_image);
                    }else {
                        nav_user_image.setImageResource(R.drawable.profile);
                    }

                    Menu nav_menu = navigationView.getMenu();

                    if (type.equals("donor")){
                        nav_menu.findItem(R.id.sendEmail).setTitle("Received Emails");
                        nav_menu.findItem(R.id.notification).setVisible(true);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readDonors() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("donor");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if (userList.isEmpty()){
                    Toast.makeText(MainActivity.this, "No recipients", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readRecipients() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("recipient");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if (userList.isEmpty()){
                    Toast.makeText(MainActivity.this, "No recipients", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case R.id.aplus:
               intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
               intent.putExtra("group", "A+");
               startActivity(intent);
                break;

            case R.id.aminus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "A-");
                startActivity(intent);
                break;

            case R.id.bplus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "B+");
                startActivity(intent);
                break;

            case R.id.bminus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "B-");
                startActivity(intent);
                break;


            case R.id.abplus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "AB+");
                startActivity(intent);
                break;

            case R.id.abminus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "AB-");
                startActivity(intent);
                break;

            case R.id.oplus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "O+");
                startActivity(intent);
                break;

            case R.id.ominus:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "O-");
                startActivity(intent);
                break;

            case R.id.compatible:
                intent = new Intent(MainActivity.this, CategorySelectedActivity.class);
                intent.putExtra("group", "Compatible with me");
                startActivity(intent);
                break;

            case R.id.sendEmail:
                intent = new Intent(MainActivity.this, SentemailActivity.class);
                startActivity(intent);
                break;

            case R.id.notification:
                intent = new Intent(MainActivity.this, NotificationsActivity.class);
                startActivity(intent);
                break;

            case R.id.profile:
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}