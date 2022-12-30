package com.udacity.project4.loginregisteration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
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
    companion object {
        const val SIGN_IN_RESULT_CODE = 1
    }
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


                loginRegistrationViewModel.type.observe(viewLifecycleOwner){
                    when(it!!){
                        LoginRegistrationType.Registration->{updateRegisterUI()}
                        LoginRegistrationType.Login -> {updateLoginUI()}

                    }
                }
                fun navigateToReminderListFragment(){
                    findNavController().navigate(LoginRegistrationFragmentDirections.actionLoginRegistrationFragmentToReminderListFragment())
                }
                fun navigateToLoginRegistrationFragment(authenticationStatus: AuthenticationStatus){
                    //TODO navigateToLoginRegistrationFragment
                }
                loginRegistrationViewModel.authenticationStatus.observe(viewLifecycleOwner){
                    if(it != null){
                        when(it)
                        {
                            AuthenticationStatus.Authenticated -> navigateToReminderListFragment()
                            AuthenticationStatus.UnAuthenticated -> navigateToLoginRegistrationFragment(AuthenticationStatus.UnAuthenticated)
                            AuthenticationStatus.NewUser -> navigateToLoginRegistrationFragment(AuthenticationStatus.NewUser)
                        }
                    }
                }
                loginRegistrationViewModel.loginRegisterEvent.observe(viewLifecycleOwner){
                    if(it){
                        loginRegistrationViewModel.onLoginRegister()
                       launchSignInFlow();
                    }
                }




                return binding.root
    }

    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
       startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    "TAG",
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )

            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i("TAG", "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}