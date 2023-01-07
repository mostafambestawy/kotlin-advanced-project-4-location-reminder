package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationActivityViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
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
            single { RemindersLocalRepository(get()) as ReminderDataSource }
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

    //    TODO: add End to End testing to the app
    @Test
    fun addReminder(): Unit = runBlocking {

        //GIVEN

        val scenario =
            ActivityScenario.launch<AuthenticationActivity>(AuthenticationActivity::class.java)

        dataBindingIdlingResource.monitorActivity(scenario)
        //THEN

        onView(withId(R.id.remindersListLayout)).check(ViewAssertions.matches(isDisplayed()))


        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())
        //THEN
        onView(withId(R.id.saveReminderLayout)).check(ViewAssertions.matches(isDisplayed()))


        //WHEN
        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("testTitle1"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("testDescription1"))
        onView(withId(R.id.selectedLocation)).perform(click())

        //THEN
        onView(withId(R.id.selectLocationLayout)).check(ViewAssertions.matches(isDisplayed()))



        withContext(Dispatchers.IO) {
            Thread.sleep(3000)
        }

    }
}
