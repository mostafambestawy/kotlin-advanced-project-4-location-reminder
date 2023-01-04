package com.udacity.project4.locationreminders.savereminder

import androidx.test.ext.junit.runners.AndroidJUnit4

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.test.runTest
import org.junit.*
import com.udacity.project4.locationreminders.data.dto.Result
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun initialize(){
        /** Warning
        Do not do the following, do not initialize the tasksViewModel with its definition:
        val viewModel = viewModel(repository)
        This will cause the same instance to be used for all tests. This is something you should
        because each test should have a fresh instance of the subject under test (the ViewModel in this case).
         **/
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

    }

    @After
    fun stop_koin(){
        stopKoin()
    }

    @Config(sdk = [29])
    @Test
    fun saveReminder() = mainCoroutineRule.runTest{
        val reminder:ReminderDataItem = ReminderDataItem("testTitle2","testDescription2","Test Location 2",32.15524,30.3265)
        val id = reminder.id
        saveReminderViewModel.saveReminder(reminder)
        val savedReminder:Result.Success<ReminderDTO> = fakeDataSource.getReminder(id) as Result.Success<ReminderDTO>
        Assert.assertEquals((savedReminder.data as ReminderDTO).title,"testTitle2")
    }

//TODO: provide testing to the SaveReminderView and its live data objects



}