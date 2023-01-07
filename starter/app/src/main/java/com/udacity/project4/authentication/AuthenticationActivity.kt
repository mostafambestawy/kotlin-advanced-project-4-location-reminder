package com.udacity.project4.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

import com.udacity.project4.utils.enums.AuthenticationStatus
import com.udacity.project4.utils.enums.LoginRegistrationType
import kotlin.properties.Delegates

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object {
        const val SIGN_IN_RESULT_CODE = 1
    }

    private lateinit var authenticationActivityViewModel: AuthenticationActivityViewModel
    private lateinit var binding: ActivityAuthenticationBinding
    private var testing by Delegates.notNull<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //         TODODONE: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

        //          TODDOONE: If the user was authenticated, send him to RemindersActivity



        authenticationActivityViewModel =
            ViewModelProvider(this)[AuthenticationActivityViewModel::class.java]

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        binding.authenticationViewModel = authenticationActivityViewModel
        binding.lifecycleOwner = this

        //testing = intent.getBooleanExtra("testing",false)
        testing = (applicationContext as MyApp).testing
        authenticationActivityViewModel.testing = testing

        fun updateRegisterUI() {
            binding.title.text = getString(R.string.register_new_user)
            binding.loginRegisterButton.text = getString(R.string.register_now)
            binding.bottomText.text = getString(R.string.have_an_account)
            authenticationActivityViewModel.type.value = LoginRegistrationType.Registration
        }

        fun updateLoginUI() {
            binding.title.text = getString(R.string.login_title)
            binding.loginRegisterButton.text = getString(R.string.login_now)
            binding.bottomText.text = getString(R.string.havent_account)
            authenticationActivityViewModel.type.value = LoginRegistrationType.Login
        }

        fun navigateToRemindersActivity() {
            //findNavController(R.id.nav_graph).navigate(R.id.mainActivity)
            val intent = Intent(this, RemindersActivity::class.java)
            //TODO use navigation
            startActivity(intent)
        }

        authenticationActivityViewModel.authenticationStatus.observe(this) {
            if (it != null) {
                when (it) {
                    AuthenticationStatus.Authenticated -> navigateToRemindersActivity()
                    AuthenticationStatus.UnAuthenticated -> {}

                }
            }
        }
        authenticationActivityViewModel.loginRegisterEvent.observe(this) {
            if (it) {
                if(authenticationActivityViewModel.testing)
                {
                    navigateToRemindersActivity()
                }
                else {
                    launchSignInFlow();
                }
                authenticationActivityViewModel.onLoginRegister()

            }
        }
        authenticationActivityViewModel.typeEvent.observe(this) {
            if (it) {
                authenticationActivityViewModel.onTypeChanged()
                if (authenticationActivityViewModel.type.value == LoginRegistrationType.Login) updateLoginUI()
                else updateRegisterUI()
            }
        }

        val authenticationHappened = isAuthenticationTryHappened()
        if (authenticationHappened) updateLoginUI()
        else updateRegisterUI()


//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        setContentView(binding.root)

    }

    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), AuthenticationActivity.SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthenticationActivity.SIGN_IN_RESULT_CODE) {
            saveToAuthenticationTryHappenedSharedPreferences()
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

    private fun saveToAuthenticationTryHappenedSharedPreferences() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(getString(com.udacity.project4.R.string.authentication_try_happened), true)
            apply()
        }

    }

    private fun isAuthenticationTryHappened(): Boolean {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val defaultValue = false
        val value =
            sharedPref?.getBoolean(getString(R.string.authentication_try_happened), defaultValue)
        return value!!
    }
}
