package com.udacity.project4.authentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.utils.enums.AuthenticationStatus
import com.udacity.project4.utils.enums.LoginRegistrationType

class AuthenticationActivityViewModel(mTesting: Boolean = false) : ViewModel() {

    val type: MutableLiveData<LoginRegistrationType> =
        MutableLiveData(LoginRegistrationType.Registration)

    var testing = mTesting

    val authenticationStatus = FirebaseUserLiveData().map {
        if (!testing) {
            if (it != null) {
                AuthenticationStatus.Authenticated
            } else {
                AuthenticationStatus.UnAuthenticated
            }
        }
        else {  AuthenticationStatus.Authenticated}
    }

    fun toggleType() {
        when (type.value) {
            LoginRegistrationType.Registration -> setLoginType()
            LoginRegistrationType.Login -> setRegistrationType()
            else -> {}
        }
    }


    var loginRegisterEvent = MutableLiveData(false)


    fun loginRegister() {
        loginRegisterEvent.value = true
    }

    fun onLoginRegister() {
        loginRegisterEvent.value = false
    }

    private fun setLoginType() {
        type.value = LoginRegistrationType.Login
        typeEvent.value = true
    }

    var typeEvent = MutableLiveData(false)

    private fun setRegistrationType() {
        type.value = LoginRegistrationType.Registration
        typeEvent.value = true;
    }

    fun onTypeChanged() {
        typeEvent.value = false
    }
}