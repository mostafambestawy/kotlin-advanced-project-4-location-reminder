package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB.createRemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {
    private lateinit var reminderDataSource: ReminderDataSource

//    TODODONE: test the navigation of the fragments.
    /** live data testing os hat postValue and setValue not crashed
     * if not called from main thread **/
    /*@get:Rule
    val rule = InstantTaskExecutorRule()
*/
    /* @get:Rule
     var mainCoroutineRule = MainCoroutineRule()
 */
    @Before
    fun initialize() {
        // stop main koin
        stopKoin()
        //use Koin Library as a service locator
        val mModule = module {
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { createRemindersDao(getApplicationContext()) }
            single {
                SaveReminderViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
        }

        startKoin {
            modules(listOf(mModule))
        }
        //initialize reminderDataSource
        reminderDataSource = get()


        runBlocking {
            reminderDataSource.deleteAllReminders()
        }

    }


    @After
    fun delete_all() = runBlocking {
        reminderDataSource.deleteAllReminders()
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

    @Test
    fun clickFAB_NavigateToSaveFragment() = runBlocking {
        //GIVEN

        val mockedNavController = mock(NavController::class.java)

        val scenario: FragmentScenario<ReminderListFragment> =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }
        dataBindingIdlingResource.monitorFragment(scenario)
        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())
        //THEN
        verify(mockedNavController).navigate(ReminderListFragmentDirections.toSaveReminder())


    }

    //    TODODONE: test the displayed data on the UI.
    @Test
    fun testUI(): Unit = runBlocking {
        //GIVEN
        val reminder: ReminderDTO =
            ReminderDTO("testTitle1", "testDescription1", "Test Location 1", 32.15524, 30.3265)
        reminderDataSource.saveReminder(reminder)
        val mockedNavController = mock(NavController::class.java)
        //WHEN
        val scenario: FragmentScenario<ReminderListFragment> =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }
        dataBindingIdlingResource.monitorFragment(scenario)

        //THEN
        onView(withId(R.id.reminderDisplayedTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDisplayedDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDisplayedLocation)).check(matches(isDisplayed()))

        onView(withId(R.id.reminderDisplayedTitle)).check(matches(withText(reminder.title)))
        onView(withId(R.id.reminderDisplayedDescription)).check(matches(withText(reminder.description)))
        onView(withId(R.id.reminderDisplayedLocation)).check(matches(withText(reminder.location)))


    }


    //    TODODONE: add testing for the error messages.
    @Test
    fun testClickSaveForTitleMissingReminder(): Unit = runBlocking {
        //GIVEN

        val mockedNavController = mock(NavController::class.java)
        //WHEN
        val scenario: FragmentScenario<SaveReminderFragment> =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.saveReminder)).perform(click())

        val errEnterTitleMsg: String =
            (getApplicationContext() as Context).getString(R.string.err_enter_title)
        onView(withText(errEnterTitleMsg)).check(matches(isDisplayed()))

    }
    @Test
    fun testClickSaveForLocationMissingReminder(): Unit = runBlocking {
        //GIVEN

        val mockedNavController = mock(NavController::class.java)
        //WHEN
        val scenario: FragmentScenario<SaveReminderFragment> =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("testTitle1"))
        onView(withId(R.id.saveReminder)).perform(click())

        val errSelectLocationMsg:String = (getApplicationContext() as Context).getString(R.string.err_select_location)
        onView(withText(errSelectLocationMsg)).check(matches(isDisplayed()))

    }


}