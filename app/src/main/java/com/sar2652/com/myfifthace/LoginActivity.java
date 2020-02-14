package com.sar2652.com.myfifthace;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN=9001;
    public String TAG = "";
    public GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    public SignInButton signInButton;
    public GoogleSignInAccount account;
    public GoogleApiClient mGoogleApiClient;

    public ProgressDialog progressDialog;
    public String email_id;
    public String firstName;
    public String familyName;

    @Override
    protected  void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButton = (SignInButton)findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Signing you in.....");
                progressDialog.show();
            }
        });
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestServerAuthCode(getResources().getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener()
        {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    public String get_url = URL.domain + "socialLogin";
    public String JSONResponse;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public boolean IS_RESULT_RECEIVED=false;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IS_RESULT_RECEIVED=true;
        progressDialog.setMessage("Result Received");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                account = task.getResult(ApiException.class);
                email_id=account.getEmail();
                firstName = account.getGivenName();
                familyName = account.getFamilyName();
                Log.d("ID is","Email id is "+email_id);
                getInfo(email_id, firstName, familyName);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);

            }
        }
    }

    public void getInfo(String email, final String firstName, String lastName)
    {
        final String id = email;
        progressDialog.setMessage("Getting your data...");
        Log.d("url", get_url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONResponse = response;
                        Log.d("TAG", response);
                        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        progressDialog.setMessage("Response Received");
                        Log.d("Email:", "This is email" + id);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            int id = jsonObject.getInt("id");
                            String first_name = jsonObject.getString("first_name");
                            String last_name = jsonObject.getString("last_name");
                            Log.v(TAG, "first_name :" + first_name + "last_name :" + last_name);
                            editor.putInt("id", id);
                            editor.putString("first_name", first_name);
                            editor.putString("last_name", last_name);
                            editor.commit();
                        }
                        catch (JSONException e){}
                        updateUI("in");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                //Log.d("Volley Error","Volley Error");
                //Toast.makeText(LoginActivity.this, "Volley error", Toast.LENGTH_SHORT).show();
                if(error instanceof ServerError)
                {Log.d("Error","Server error");
                    error.printStackTrace();}
                    if(error instanceof NetworkError)
                {Log.d("Error","Network error");}
                if (error instanceof NoConnectionError)
                {Log.d("Error","No Connection error");}
            }
        }) {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {

                Map<String,String> params=new HashMap<String, String>();
                params.put("email", id);
                params.put("first_name", firstName);
                params.put("last_name", familyName);
                params.put("social_login_type", "g");
                return params;
            }
        };
        int socketTimeout=60000;
        RetryPolicy policy=new DefaultRetryPolicy(socketTimeout,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        MySingleton.getInstance(LoginActivity.this).addToRequestQueue(stringRequest);
    }

    public void updateUI(String req)
    {
        if(req.equals("in"))
        {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            progressDialog.dismiss();
            finish();
        }
        else
        {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this,"Oops!! Something went wrong",Toast.LENGTH_SHORT).show();
        }
    }

    public void signIn() {
        mGoogleApiClient.connect();
        Log.d("Client",mGoogleApiClient.toString());
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}
