/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.List;

//TODO revamp this activity it is just a place holder for now
public class MainActivity extends AppCompatActivity implements View.OnKeyListener, View.OnClickListener {

    EditText username;
    EditText password;
    TextView textView;
    ImageView imageView;
    RelativeLayout relativeLayout;
    Button navigateToHome;

    public void login(View view){
        parseQuery();
    }

    private void navigate(){
        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
        startActivity(intent);
    }

        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == keyEvent.ACTION_DOWN) {
                parseQuery();
            }
            return false;
        }

        private void parseQuery(){
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username.getText().toString());
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (objects.isEmpty()) {
                        signUp();
                    } else {
                        logIn();
                    }
                }
            });
        }

        public void onSwitch(View view){
            if (navigateToHome.getText().equals("Sign up")) {
                navigateToHome.setText("Login");
                textView.setText("or Sign up");
            } else {
                navigateToHome.setText("Sign up");
                textView.setText("or Login");
            }
        }

        public void onPress(View view) {
            if (username.getText().toString().matches("") || password.getText().toString().matches("") ){
                Toast.makeText(this, "Please Enter a valid Username / Password", Toast.LENGTH_SHORT).show();
            } else {
                if (navigateToHome.getText().equals("Sign up")){
                    signUp();
                    Log.i("unique", "test");
                } else {
                    logIn();
                }
            }

        }

        public void logIn() {
            if (navigateToHome.getText().equals("Login")) {
                ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            Toast.makeText(com.parse.starter.MainActivity.this, "Log in Successful", Toast.LENGTH_SHORT).show();
                            navigate();
                        } else {
                            Toast.makeText(com.parse.starter.MainActivity.this, "Log in unsuccessful: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    }
                });
            } else {
                Toast.makeText(this, "This account already exists, please login", Toast.LENGTH_SHORT).show();
            }
        }

        public void signUp(){
            if (navigateToHome.getText().equals("Sign up")) {
                final ParseUser user = new ParseUser();
                user.setUsername(username.getText().toString());
                user.setPassword(password.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(com.parse.starter.MainActivity.this, "Sign up Successful", Toast.LENGTH_SHORT).show();
                            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(com.parse.starter.MainActivity.this, " Sign up successful", Toast.LENGTH_SHORT).show();
                                        navigate();
                                    } else {
                                        Toast.makeText(com.parse.starter.MainActivity.this, "Sign up unsuccessful " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(com.parse.starter.MainActivity.this, "Sign up Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Account does not exist, please sign up", Toast.LENGTH_SHORT).show();
            }
        }



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            setTitle("IdeaNetwork");
            username = (EditText)findViewById(R.id.username);
            password = (EditText)findViewById(R.id.password);
            textView = (TextView)findViewById(R.id.textView);
            relativeLayout = (RelativeLayout)findViewById(R.id.relativeLayout);
            relativeLayout.setOnClickListener(this);
            navigateToHome = (Button)findViewById(R.id.navigateToHome);
            password.setOnKeyListener(this);
            if (ParseUser.getCurrentUser() !=null) {
                ParseUser.getCurrentUser().logOut();
            }
            ParseAnalytics.trackAppOpenedInBackground(getIntent());
        }


    @Override
    public void onClick(View v) {
        //TODO when youve added the logo to the main page set up a condition for it here like the relativelayout
        if (v.getId() == R.id.relativeLayout ) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }
}




