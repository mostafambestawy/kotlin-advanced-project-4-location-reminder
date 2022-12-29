package com.udacity.project4.loginregisteration

import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseViewModel
import org.koin.android.ext.android.inject

class LoginRegistrationFragment :BaseFragment(){
    override val _viewModel: BaseViewModel by inject()
}