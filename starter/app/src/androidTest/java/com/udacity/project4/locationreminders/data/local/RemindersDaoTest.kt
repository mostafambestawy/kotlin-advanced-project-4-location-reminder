package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import org.junit.After
import org.junit.Test
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


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

    @Config(sdk = [29])
    @Test
    fun addReminder() = {

    }
//    TODO: Add testing implementation to the RemindersDao.kt

}