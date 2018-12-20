package com.originalstocks.missedcallnotify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private TextView phoneTextView, fetchedPhoneText;
    private Button saveButton, loadButton;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }
        }

        mFirestore = FirebaseFirestore.getInstance();

        saveButton = findViewById(R.id.saveButton);
        loadButton = findViewById(R.id.load_button);
        phoneTextView = findViewById(R.id.main_text);
        fetchedPhoneText = findViewById(R.id.fetchTextView);


        // getting data from Broadcast receiver
        getPhoneData();


    }

    public void getPhoneData() {
        Intent info = getIntent();
        final String phoneNumber = info.getStringExtra("phoneNumber");
        if (phoneNumber == null) {
            phoneTextView.setText("Give a missed call to this phone");
        } else {
            phoneTextView.setText("Missed call by : " + phoneNumber);
        }

        //  Storing the data at firestore...
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> userMap = new HashMap<>();
                userMap.put("Mobile_Number", phoneNumber);
                mFirestore.collection("users").document("one").set(userMap).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, phoneNumber + " is saved in database", Toast.LENGTH_LONG).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("TASK_FAILURE", "onFailure: " + e.getMessage());
                            }
                        });

            }
        });

        // get data
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFirestore.collection("users").document("one").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        String mobile = documentSnapshot.getString("Mobile_Number");
                        //Toast.makeText(MainActivity.this, "Fetched Number is : " + mobile, Toast.LENGTH_LONG).show();
                        fetchedPhoneText.setText("Fetched Number is : " + mobile);
                    }
                });

            }
        });
        if (phoneNumber == null) {
            Toast.makeText(this, "Give a missed call to your number.", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(false);
            loadButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
            loadButton.setEnabled(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPhoneData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPhoneData();
    }
}
