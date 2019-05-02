package com.example.event_app.repository

import com.example.event_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

object UserRepository {

    var currentUser: BehaviorSubject<User> = BehaviorSubject.create()
    var fireBaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val usersRef: DatabaseReference = database.getReference("users")


    //Method to listen to changes inside dataBase from a node
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

    fun testUserConnected(): Observable<Boolean> {
        return RxFirebaseAuth.observeAuthState(fireBaseAuth)
            .map { authResult -> authResult.currentUser != null }
    }

    fun logUser(email: String, password: String): Flowable<Boolean> {
        return RxFirebaseAuth.signInWithEmailAndPassword(fireBaseAuth, email, password)
            .map { authResult ->
                authResult.user?.let {
                    val user = User(
                        authResult.user.uid,
                        authResult.user.displayName,
                        authResult.user.email,
                        authResult.user.photoUrl
                    )
                    currentUser.onNext(user)
                }
                authResult.user != null
            }
            .toFlowable()
    }

    fun registerUser(email: String, password: String): Flowable<Boolean> {

        return RxFirebaseAuth.createUserWithEmailAndPassword(fireBaseAuth, email, password)
            .map { authResult ->
                authResult.user?.let {
                    val user = User(
                        authResult.user.uid,
                        authResult.user.displayName,
                        authResult.user.email,
                        authResult.user.photoUrl
                    )
                    currentUser.onNext(user)
                }
                authResult.user!= null
            }
            .toFlowable()
    }
}