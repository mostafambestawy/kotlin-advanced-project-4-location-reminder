package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runTest
import org.junit.*



@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    @Before
    fun initialize() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }
    @After
    fun close_db()  = database.close()


    @Test
    fun addReminder() = mainCoroutineRule.runTest{
    val reminder:ReminderDTO = ReminderDTO("testTitle1","testDescription1","Test Location 1",32.15524,30.3265)
    val id = reminder.id
    database.reminderDao().saveReminder(reminder)
    val savedReminder = database.reminderDao().getReminderById(id)
        Assert.assertEquals(savedReminder?.id,id)
    }
//    TODO: Add testing implementation to the RemindersDao.kt

}