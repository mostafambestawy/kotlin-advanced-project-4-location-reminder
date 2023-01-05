package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    var shouldReturnError: Boolean = false
    var fakeReminders: MutableList<ReminderDTO> = mutableListOf(
        ReminderDTO("testTitle1", "testDescription1", "Test Location 1", 32.15524, 30.3265)
    )

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) Result.Error("Fake Error")
        else {
            if (fakeReminders.isEmpty()) Result.Error("No Reminders")
            else Result.Success(fakeReminders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        fakeReminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) Result.Error("Fake Error")
        else {
            val reminder = fakeReminders.first { it.id == id }
            if (reminder != null) {
                Result.Success(reminder)
            } else
                Result.Error("reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        fakeReminders.clear()
    }


}