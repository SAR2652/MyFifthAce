package com.sar2652.com.myfifthace;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    GoogleSignInAccount user;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    public int t;
    String url=URL.domain+"retrieveClientData";
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    SharedPreferences object;
    SharedPreferences.Editor objectedit;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Getting your stuff...");
        progressDialog.show();

        user = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        object=getSharedPreferences("user", Context.MODE_PRIVATE);
        objectedit=object.edit();

    }


}

