package com.udacity.project4.loginregisteration

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.enums.LoginRegistrationType

class LoginRegistrationViewModel(val app: Application) :BaseViewModel(app) {
    val type = MutableLiveData(LoginRegistrationType.Login)


}