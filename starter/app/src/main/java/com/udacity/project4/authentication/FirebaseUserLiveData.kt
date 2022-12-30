package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser?>() {
    private val firebaseAuth = FirebaseAuth.getInstance()
   // var newUser:Boolean = true
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
     //   newUser = false
    }


    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }


    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}