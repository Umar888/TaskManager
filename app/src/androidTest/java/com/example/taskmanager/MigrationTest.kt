package com.example.taskmanager

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.taskmanager.data.local.TaskDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), TaskDatabase::class.java)

    @Test
    fun migrate5To6_keepsExistingTasks() {
        helper.createDatabase(DB, 5).use { db ->
            db.execSQL(
                "INSERT INTO tasks (title, description, priority, dueDate, status) " +
                    "VALUES ('Existing', 'from 2.0', 'High', 1700000000000, 'In Progress')"
            )
        }

        // validateDroppedTables + schema check against the exported 6.json
        helper.runMigrationsAndValidate(DB, 6, true, TaskDatabase.MIGRATION_5_6).use { db ->
            db.query("SELECT title, priority, status, reminderMinutes, `repeat`, checklist FROM tasks").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Existing", c.getString(0))
                assertEquals("High", c.getString(1))
                assertEquals("In Progress", c.getString(2))
                assertTrue(c.isNull(3))
                assertEquals("None", c.getString(4))
                assertTrue(c.isNull(5))
            }
        }
    }

    private companion object {
        const val DB = "migration-test"
    }
}
