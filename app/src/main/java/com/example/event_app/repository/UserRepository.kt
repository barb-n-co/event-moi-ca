package com.example.event_app.repository

import com.example.event_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import durdinapps.rxfirebase2.RxFirebaseDatabase



object UserRepository {

    var currentUser: BehaviorSubject<User> = BehaviorSubject.create()
    var fireBaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    val usersRef: DatabaseReference = database.getReference("users")


    fun testUserConnected(): Observable<Boolean> {
        return RxFirebaseAuth.observeAuthState(fireBaseAuth)
            .map { authResult -> authResult.currentUser != null }
    }

    fun getUserNameFromFirebase() {
        fireBaseAuth.currentUser?.uid?.let {
            RxFirebaseDatabase.observeSingleValueEvent(
                usersRef.child(it)
            ) { dataSnapshot ->
                dataSnapshot.getValue(User::class.java)
            }
                .subscribe({ user ->
                    user?.let {
                        currentUser.onNext(it)
                    }
                })
        }
    }

    fun logUser(email: String, password: String): Flowable<Boolean> {
        return RxFirebaseAuth.signInWithEmailAndPassword(fireBaseAuth, email, password)
            .map { authResult ->
                authResult.user?.let {
                    getUserNameFromFirebase()
                }
                authResult.user != null
            }
            .toFlowable()
    }

    fun registerUser(email: String, password: String, name: String): Flowable<Boolean> {

        return RxFirebaseAuth.createUserWithEmailAndPassword(fireBaseAuth, email, password)
            .map { authResult ->
                authResult.user?.let {
                    val user = User(
                        it.uid,
                        name,
                        it.email
                    )
                    currentUser.onNext(user)
                    setNameFirebase(it.uid, name, email)
                }
                authResult.user!= null
            }
            .toFlowable()
    }

    private fun setNameFirebase(uid: String, name: String, email: String){
        val user = User(uid, name, email)
        usersRef.child(uid).setValue(user)
    }
}