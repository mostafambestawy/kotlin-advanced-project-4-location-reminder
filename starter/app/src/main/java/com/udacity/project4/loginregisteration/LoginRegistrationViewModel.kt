package com.udacity.project4.loginregisteration


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.project4.utils.enums.AuthenticationStatus

import com.udacity.project4.utils.enums.LoginRegistrationType

class LoginRegistrationViewModel : ViewModel() {
    val type:MutableLiveData<LoginRegistrationType> = MutableLiveData(LoginRegistrationType.Registration)
    fun toggleType(){
        when(type.value){
            LoginRegistrationType.Registration -> setLoginType()
            LoginRegistrationType.Login -> setRegistrationType()
            else -> {}
        }
    }
    private fun setLoginType(){
        type.value =LoginRegistrationType.Login
    }
    private fun setRegistrationType(){
        type.value =LoginRegistrationType.Registration
    }


}