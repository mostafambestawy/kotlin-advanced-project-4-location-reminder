package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun initialize() = mainCoroutineRule.runTest{
        /** Warning
        Do not do the following, do not initialize the tasksViewModel with its definition:
        val viewModel = viewModel(repository)
        This will cause the same instance to be used for all tests. This is something you should
        because each test should have a fresh instance of the subject under test (the ViewModel in this case).
         **/
        fakeDataSource = FakeDataSource()
        fakeDataSource.deleteAllReminders()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

    }

    @After
    fun stop_koin(){
        stopKoin()
    }
    @Config(sdk = [29])
    @Test
    fun saveReminder() = mainCoroutineRule.runTest{
        val reminders:List<ReminderDTO> = listOf(
            ReminderDTO("testTitle1","testDescription1","Test Location 1",32.15524,30.3265),
            ReminderDTO("testTitle2","testDescription2","Test Location 2",32.15524,30.3265),
            ReminderDTO("testTitle3","testDescription3","Test Location 3",32.15524,30.3265),
            ReminderDTO("testTitle4","testDescription4","Test Location 4",32.15524,30.3265),
            ReminderDTO("testTitle5","testDescription5","Test Location 5",32.15524,30.3265))
        for(reminder in reminders) {
            fakeDataSource.saveReminder(reminder)
        }
        remindersListViewModel.loadReminders()
        Assert.assertEquals(remindersListViewModel.remindersList.value?.size,5)
    }
    //TODODONE: provide testing to the RemindersListViewModel and its live data objects



}