package com.developeralamin.blood.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.developeralamin.blood.MainActivity;
import com.developeralamin.blood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DonorRegistrationActivity extends AppCompatActivity {

    private CircleImageView profile_image;

    private TextInputEditText inputName,inputId,inputNumber,
                                inputEmail,inputPassword;

    private Spinner blodGroupsSpinner;


    private TextView backButton;

    private Button registerButton;

    private Uri resultUri;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_registration);

        profile_image = findViewById(R.id.profile_image);
        inputName = findViewById(R.id.inputName);
        inputId = findViewById(R.id.inputId);
        inputNumber = findViewById(R.id.inputNumber);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        blodGroupsSpinner = findViewById(R.id.blodGroupsSpinner);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DonorRegistrationActivity.this,LoginActivity.class));
                finish();
            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatUser();
            }
        });

    }

    private void creatUser() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String name = inputName.getText().toString();
        String idnumber = inputId.getText().toString();
        String phonenubmer = inputNumber.getText().toString();
        String bloodGroup = blodGroupsSpinner.getSelectedItem().toString();

        if (email.isEmpty()) {
            inputEmail.setError("Enter your Email");
            inputEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            inputPassword.setError("Enter your Password");
            inputPassword.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            inputName.setError("Enter your Name");
            inputName.requestFocus();
            return;
        }
        if (idnumber.isEmpty()) {
            inputId.setError("Enter your ID");
            inputId.requestFocus();
            return;
        }
        if (phonenubmer.isEmpty()) {
            inputNumber.setError("Enter your Phone");
            inputNumber.requestFocus();
            return;
        }
        if (bloodGroup.equals("Select your blood group")) {
            Toast.makeText(DonorRegistrationActivity.this, "Select your blood group", Toast.LENGTH_SHORT).show();
            return;
        }else {

            progressDialog.setMessage("Registering..............");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()){
                        String error = task.getException().toString();
                        Toast.makeText(DonorRegistrationActivity.this, "Error" + error, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String currentUserId = auth.getCurrentUser().getUid();
                        databaseReference = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(currentUserId);

                        HashMap userInfo = new HashMap();

                        userInfo.put("id", currentUserId);
                        userInfo.put("name", name);
                        userInfo.put("email", email);
                        userInfo.put("idnumber", idnumber);
                        userInfo.put("phonenumber", phonenubmer);
                        userInfo.put("bloodgroup", bloodGroup);
                        userInfo.put("type", "donor");
                        userInfo.put("search", "donor"+bloodGroup);
                        userInfo.put("profilepictureurl", "no_pic_uploaded");

                        databaseReference.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(DonorRegistrationActivity.this, "Date set Successful", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }

                                finish();
                                
                            }
                        });


                        if (resultUri !=null){
                            final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                                    .child("profile images").child(currentUserId);

                            Bitmap bitmap = null;

                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                            }catch (IOException e){
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                            byte[] data  = byteArrayOutputStream.toByteArray();
                            UploadTask uploadTask = filePath.putBytes(data);

                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DonorRegistrationActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    if (taskSnapshot.getMetadata() !=null && taskSnapshot.getMetadata().getReference() !=null){
                                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String imageUrl = uri.toString();
                                                Map newImageMap = new HashMap();
                                                newImageMap.put("profilepictureurl", imageUrl);

                                                databaseReference.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(DonorRegistrationActivity.this, "Image url added to database successfully", Toast.LENGTH_SHORT).show();
                                                        }else {
                                                            Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                                finish();
                                          }
                                      });
                                  }
                                }
                            });

                            Intent intent = new Intent(DonorRegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            progressDialog.dismiss();

                        }
                                
                    }
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null){
            resultUri = data.getData();
            profile_image.setImageURI(resultUri);

        }
    }
}