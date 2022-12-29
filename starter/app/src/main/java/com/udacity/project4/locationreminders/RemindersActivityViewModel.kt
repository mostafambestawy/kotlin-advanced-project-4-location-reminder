package com.udacity.project4.locationreminders

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.enums.AuthenticationStatus
import com.udacity.project4.utils.enums.LoginRegistrationType

class RemindersActivityViewModel(val app: Application) : BaseViewModel(app) {
    val authenticationStatus = MutableLiveData(AuthenticationStatus.NewUser)
}