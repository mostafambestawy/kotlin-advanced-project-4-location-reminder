package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

//    TODO: test the navigation of the fragments.
    /** live data testing os hat postValue and setValue not crashed
     * if not called from main thread **/
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun initialize() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        repository = RemindersLocalRepository(database.reminderDao(),mainCoroutineRule.dispatcher)

    }
    @After
    fun close_db()  = database.close()

    @Test
    fun clickFAB_NavigateToSaveFragment()= mainCoroutineRule.runTest{
        //GIVEN
        val mockedNavController = mock(NavController::class.java)

        var scenario:FragmentScenario<ReminderListFragment> =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockedNavController)
        }
        //WHEN
        scenario.apply {
            onView(withId(R.id.addReminderFAB)).perform(click())
        }

        //THEN
        verify(mockedNavController).navigate(ReminderListFragmentDirections.toSaveReminder())


    }

//    TODO: test the displayed data on the UI.

//    TODO: add testing for the error messages.
}