package com.udacity.project4


import android.app.Application
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationActivityViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.ToastMatcher
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var authenticationActivityViewModel: AuthenticationActivityViewModel


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                AuthenticationActivityViewModel(true)
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
            single {
                get() as Boolean
            }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        authenticationActivityViewModel = get() as AuthenticationActivityViewModel


        //clear the data to start fresh
        runBlocking {
            (getApplicationContext() as MyApp).testing = true
            repository.deleteAllReminders()
        }

    }

    @After
    fun endTesting() = runBlocking {
        (getApplicationContext() as MyApp).testing = false
        repository.deleteAllReminders()
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //    TODODONE: add End to End testing to the app
    @Test
    fun addReminder(): Unit = runBlocking {

        //GIVEN
        /** start app and bypass login logic**/
        val scenario =
            ActivityScenario.launch(AuthenticationActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        //THEN
        onView(withId(R.id.remindersListLayout)).check(matches(isDisplayed()))

        /** tap addReminderFAB **/
        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())
        //THEN
        onView(withId(R.id.saveReminderLayout)).check(matches(isDisplayed()))

        /** type in title ,description and tap selectLocation **/
        //WHEN
        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("testTitle1"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("testDescription1"))
        onView(withId(R.id.selectLocation)).perform(click())
        //THEN
        onView(withId(R.id.selectLocationLayout)).check(matches(isDisplayed()))

        /** select arbitrary location and submit selected location **/
        //WHEN
        onView(withId(R.id.mapFragment)).perform(click())
        onView(withId(R.id.submitLocationButton)).perform(click())
        //THEN
        onView(withId(R.id.saveReminderLayout)).check(matches(isDisplayed()))

        /** tap saveReminder **/
        //WHEN
        onView(withId(R.id.saveReminder)).perform(click())
        //THEN
        onView(withId(R.id.reminderDisplayedTitle)).check(matches(withText("testTitle1")))
        onView(withId(R.id.reminderDisplayedDescription)).check(matches(withText("testDescription1")))


    }

    @Test
    fun errEnterTitleSnackBar(): Unit = runBlocking {
        //GIVEN
        /** start app and bypass login logic**/
        val scenario =
            ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        //THEN
        onView(withId(R.id.remindersListLayout)).check(matches(isDisplayed()))

        /** tap addReminderFAB **/
        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())
        //THEN
        onView(withId(R.id.saveReminderLayout)).check(matches(isDisplayed()))

        /** tap saveReminder **/
        //WHEN
        onView(withId(R.id.saveReminder)).perform(click())
        //THEN
        //https://stackoverflow.com/questions/33111882/testing-snackbar-show-with-espresso
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(appContext.getString(R.string.err_enter_title))))
    }


    /**
     * for the open issue https://github.com/android/android-test/issues/803
     * run this test for SDK = 28 or below till issue closed
     * **/


    @Config(
        maxSdk = Build.VERSION_CODES.P,
        sdk = [Build.VERSION_CODES.P, Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1, Build.VERSION_CODES.N_MR1, Build.VERSION_CODES.N, Build.VERSION_CODES.M, Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1, Build.VERSION_CODES.KITKAT, Build.VERSION_CODES.KITKAT_WATCH],
    )
    @Test
    fun savedReminderToast(): Unit = runBlocking {

        //GIVEN
        /** start app and bypass login logic**/
        val scenario =
            ActivityScenario.launch(AuthenticationActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        //THEN
        onView(withId(R.id.remindersListLayout)).check(matches(isDisplayed()))

        /** tap addReminderFAB **/
        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())
        //THEN
        onView(withId(R.id.saveReminderLayout)).check(matches(isDisplayed()))

        /** type in title ,description and tap selectLocation **/
        //WHEN
        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("testTitle1"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("testDescription1"))
        onView(withId(R.id.selectLocation)).perform(click())
        //THEN
        onView(withId(R.id.selectLocationLayout)).check(matches(isDisplayed()))

        /** select arbitrary location and submit selected location **/
        //WHEN
        onView(withId(R.id.mapFragment)).perform(click())
        onView(withId(R.id.submitLocationButton)).perform(click())
        //THEN
        onView(withId(R.id.saveReminderLayout)).check(matches(isDisplayed()))

        /** tap saveReminder **/
        //WHEN
        onView(withId(R.id.saveReminder)).perform(click())

        withContext(Dispatchers.IO) {
            Thread.sleep(300)
        }

        //THEN
        //https://stackoverflow.com/questions/47092927/testing-toast-message-using-espresso-is-not-resolved
        onView(withText(R.string.reminder_saved)).inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
    }
}
