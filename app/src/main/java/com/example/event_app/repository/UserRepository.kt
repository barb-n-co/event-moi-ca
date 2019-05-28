package com.example.event_app.repository

import android.net.Uri
import com.example.event_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

private const val USERS = "users"

object UserRepository {

    var currentUser: BehaviorSubject<User> = BehaviorSubject.create()
    var fireBaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = database.getReference(USERS)

    fun resetPassword(email: String) {
        fireBaseAuth.sendPasswordResetEmail(email)
    }

    fun getAllUsers(): Observable<List<User>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            usersRef, DataSnapshotMapper.listOf(User::class.java)
        ).toObservable()
    }

    fun getUserNameFromFirebase() {
        fireBaseAuth.currentUser?.uid?.let { it ->
            RxFirebaseDatabase.observeSingleValueEvent(
                usersRef.child(it)
            ) { dataSnapshot ->
                dataSnapshot.getValue(User::class.java)
            }
                .subscribe({ user ->
                    user?.let {
                        currentUser.onNext(it)
                    }
                }, {
                    Timber.e(it)
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

    fun deleteAccount(idUser: String) {
        fireBaseAuth.currentUser?.delete()?.addOnCompleteListener {
            usersRef.child(idUser).removeValue()
        }
    }

    fun registerUser(email: String, password: String, name: String): Flowable<Boolean> {

        return RxFirebaseAuth.createUserWithEmailAndPassword(fireBaseAuth, email, password)
            .map { authResult ->
                authResult.user?.let { firebaseUser ->
                    val user = User(
                        firebaseUser.uid,
                        name,
                        firebaseUser.email,
                        "${authResult.user.uid}/${authResult.user.uid}.jpeg"
                    )
                    val userAuth = fireBaseAuth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build()
                    userAuth?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        setNameFirebase(firebaseUser.uid, name, email, "${authResult.user.uid}/${authResult.user.uid}.jpeg")
                    }
                    currentUser.onNext(user)

                }
                authResult.user != null
            }
            .toFlowable()
    }

    fun updateUser(uid: String, email: String, name: String, photoUrl: String) {

        fireBaseAuth.currentUser?.let { user ->

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(photoUrl))
                .build()
            user.updateProfile(profileUpdates).addOnCompleteListener {
                if (it.isSuccessful) {
                    setNameFirebase(uid, name, email, photoUrl)
                }
            }
        }

    }

    fun getUserById(userId: String): Maybe<User> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            usersRef.child(userId), User::class.java
        )
    }

    private fun setNameFirebase(uid: String, name: String, email: String, photoUrl: String?) {
        val user: User = if (photoUrl == null) {
            User(uid, name, email)
        } else {
            User(uid, name, email, photoUrl)
        }
        val disposeBag = CompositeDisposable()
        RxFirebaseDatabase.setValue(usersRef.child(uid), user)
            .subscribe(
                {
                    Timber.d("user successfully added to database")
                    disposeBag.dispose()
                    currentUser.onNext(user)
                },
                {
                    Timber.e(it)
                    disposeBag.dispose()
                }
            ).addTo(disposeBag)
    }


}
