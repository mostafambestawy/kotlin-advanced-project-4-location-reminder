package com.udacity.project4.loginregisteration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.databinding.FragmentLoginRegistrationBinding
import com.udacity.project4.utils.enums.AuthenticationStatus
import com.udacity.project4.utils.enums.LoginRegistrationType
import kotlinx.android.synthetic.main.fragment_login_registration.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginRegistrationFragment : Fragment(){
    //override val _viewModel: BaseViewModel by inject()
    private lateinit var loginRegistrationViewModel:LoginRegistrationViewModel;
    private lateinit var binding: FragmentLoginRegistrationBinding
            override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

                binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_login_registration,container,false)
                loginRegistrationViewModel = ViewModelProvider(this)[LoginRegistrationViewModel::class.java]
      //          binding.viewModel = _viewModel
                binding.loginRegistrationViewModel = loginRegistrationViewModel

                fun updateRegisterUI(){
                    binding.title.text = getString(R.string.register_new_user)
                    binding.loginRegisterButton.text = getString(R.string.register_now)
                    binding.bottomText.text = getString(R.string.have_an_account)
                }
                fun updateLoginUI(){
                    binding.title.text = getString(R.string.login_title)
                    binding.loginRegisterButton.text = getString(R.string.login_now)
                    binding.bottomText.text = getString(R.string.havent_account)
                }
                fun navigateToReminderListFragment(){
                    //TODO navigateToReminderListFragment
                }

                loginRegistrationViewModel.type.observe(viewLifecycleOwner){
                    when(it!!){
                        LoginRegistrationType.Registration->{updateRegisterUI()}
                        LoginRegistrationType.Login -> {updateLoginUI()}

                    }
                }

                return binding.root
    }
}