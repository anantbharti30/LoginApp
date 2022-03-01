package com.example.interviewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth=FirebaseAuth.getInstance();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out:{
                logOut();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        
        // log out from google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(Home.this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account!=null)
        {
            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(),"Logged out successfully",Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Failed to log out!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            mAuth.signOut();
            Toast.makeText(getApplicationContext(),"Logged out successfully",Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }
}
