package com.example.interviewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText phoneNumber;
    Pinview otp;
    Button sendOtp,verifyOtp;
    SignInButton signInButton;
    FirebaseAuth mAuth;
    String mVerificationId,phoneNum;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    TextView resendTimer,enterOtpTxt,otherNum;
    CountryCodePicker ccp;
    ProgressDialog dialog;
    private GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN=100;
    TextInputLayout phoneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();

        mAuth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(MainActivity.this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            finish();
            startActivity(new Intent(MainActivity.this,Home.class));
        }
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                dialog.setMessage("Verifying...");
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MainActivity.this,"Failed to send OTP!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(MainActivity.this,"Invalid Mobile Number",Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(MainActivity.this,"Quota Over",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"OTP sent",Toast.LENGTH_SHORT).show();
                mVerificationId = verificationId;
                mResendToken = token;
                ccp.setVisibility(View.GONE);
                phoneNumber.setVisibility(View.GONE);
                phoneLayout.setVisibility(View.GONE);
                enterOtpTxt.setVisibility(View.VISIBLE);
                sendOtp.setVisibility(View.GONE);
                verifyOtp.setVisibility(View.VISIBLE);
                otp.setVisibility(View.VISIBLE);
                resendTimer.setVisibility(View.VISIBLE);
                otherNum.setVisibility(View.VISIBLE);
                new CountDownTimer(120000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        resendTimer.setEnabled(false);
                        resendTimer.setText(millisUntilFinished / 1000+" s");
                    }
                    public void onFinish() {
                        resendTimer.setText("Resend OTP");
                        resendTimer.setEnabled(true);
                    }
                }.start();
            }
        };


        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneNumber.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(),"Enter phone number!",Toast.LENGTH_SHORT).show();
                else{
                    dialog.setMessage("Sending OTP ...");
                    dialog.show();
                    phoneNum=ccp.getSelectedCountryCodeWithPlus()+phoneNumber.getText().toString().trim();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNum,
                            60,
                            TimeUnit.SECONDS,
                            MainActivity.this,
                            mCallbacks);
                }
            }
        });

        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otp.getValue().isEmpty())
                    Toast.makeText(getApplicationContext(),"Enter OTP!",Toast.LENGTH_SHORT).show();
                else{
                    dialog.setMessage("Verifying...");
                    dialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp.getValue());
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        resendTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Sending OTP ...");
                dialog.show();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNum,
                        60,
                        TimeUnit.SECONDS,
                        MainActivity.this,
                        mCallbacks,
                        mResendToken);
            }
        });

        otherNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ccp.setVisibility(View.VISIBLE);
                phoneLayout.setVisibility(View.VISIBLE);
                phoneNumber.setVisibility(View.VISIBLE);
                phoneNumber.setText("");
                sendOtp.setVisibility(View.VISIBLE);
                verifyOtp.setVisibility(View.GONE);
                otp.setVisibility(View.GONE);
                otherNum.setVisibility(View.GONE);
                enterOtpTxt.setVisibility(View.GONE);
                resendTimer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(),"Error "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,"Logged in successfully",Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(),Home.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,"Logged in successfully",Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(),Home.class));
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(MainActivity.this,"Invalid OTP!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void setUp() {
        phoneNumber= (EditText) findViewById(R.id.sign_in_phone);
        sendOtp= (Button) findViewById(R.id.send_otp);
        otp=findViewById(R.id.otp);
        verifyOtp= (Button) findViewById(R.id.verify_otp);
        resendTimer=(TextView)findViewById(R.id.resend_timer);
        signInButton=findViewById(R.id.sign_in_with_google);
        ccp = findViewById(R.id.ccp);
        enterOtpTxt=findViewById(R.id.enter_otp_txt);
        otherNum=findViewById(R.id.other_number);
        phoneLayout=findViewById(R.id.phone_field);
        otherNum.setVisibility(View.GONE);
        enterOtpTxt.setVisibility(View.GONE);
        otp.setVisibility(View.GONE);
        verifyOtp.setVisibility(View.GONE);
        resendTimer.setVisibility(View.GONE);
    }
}