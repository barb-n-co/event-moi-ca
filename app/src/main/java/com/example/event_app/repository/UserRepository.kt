package com.example.event_app.repository

import android.content.Context
import android.widget.Toast
import com.example.event_app.model.User
import com.example.event_app.ui.activity.LoginActivity
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.utils.SingletonHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import timber.log.Timber

class UserRepository private constructor(private val context: Context) {

    companion object : SingletonHolder<UserRepository, Context>(::UserRepository)

    var currentUser: User? = null
    var fireBaseAuth : FirebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val usersRef : DatabaseReference = database.getReference("users")


    fun addListenerOnRef(ref: DatabaseReference) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                Timber.d("Value is: $value")

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Timber.w("Failed to read value. ${error.toException()}")
            }
        })
    }

    fun logUser(email: String, password: String) {

        fireBaseAuth.addAuthStateListener {
            Timber.d(it.uid)
            if (it.currentUser != null) {
                    Timber.d("signInWithEmail:success")
                    val user = fireBaseAuth.currentUser
                    val newUser = User(user?.uid, user?.displayName, user?.email, user?.photoUrl)
                    currentUser = newUser
                    MainActivity.start(context as LoginActivity)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w("signInWithEmail:failure ${it.pendingAuthResult?.exception}")
                    
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }
        fireBaseAuth.signInWithEmailAndPassword(email,password)

    }

    fun registerUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {

            fireBaseAuth.addAuthStateListener {
                Timber.d(it.uid)
                if (it.currentUser != null) {
                    Timber.d("current User : ${it.currentUser}")
                    val user = fireBaseAuth.currentUser
                    val newUser = User(user?.uid, user?.displayName, user?.email, user?.photoUrl)
                    currentUser = newUser
                    MainActivity.start(context as LoginActivity)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w("createUserWithEmail:failure ${it.pendingAuthResult?.exception}")
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            fireBaseAuth.createUserWithEmailAndPassword(email, password)
        }
    }
}