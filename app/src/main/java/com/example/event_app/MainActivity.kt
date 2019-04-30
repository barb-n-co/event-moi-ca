package com.example.event_app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


const val TAG = "TAG_Event"

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")



        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                Log.d(TAG, "Value is: " + value!!)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        loginButton.setOnClickListener {
            val email = (email_et.text ?: "" ).toString()
            val password = (password_et.text ?: "" ).toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            val user = mAuth.currentUser
                            Log.d(TAG, "current User : $user")
                            myRef.child(user?.uid!!).setValue("Hello, World!")
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", it.exception)
                            Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }

                    }
            }

        }
    }



    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        //updateUI(currentUser)
        Log.d(TAG, "current User : $currentUser")
        if (currentUser != null) {
            // Name, email address, and profile photo Url
            val name = currentUser.displayName
            val email = currentUser.email
            val photoUrl = currentUser.photoUrl

            // Check if user's email is verified
            val emailVerified = currentUser.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = currentUser.uid
            Log.d(TAG, "User info : uid = $uid , name = $name , email = $email , photo URL = $photoUrl , is email verified ? $emailVerified")
        }
    }
}

/*
* mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }

                // ...
            }
        });
* */

/*
* mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }

                // ...
            }
        });
* */