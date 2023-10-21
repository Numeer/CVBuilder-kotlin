package pk.pucit.edu.cv

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class StudentDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "students.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_STUDENTS_TABLE = """
            CREATE TABLE ${StudentDatabase.StudentEntry.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER} TEXT,
                ${StudentDatabase.StudentEntry.COLUMN_NAME} TEXT,
                ${StudentDatabase.StudentEntry.COLUMN_CGPA} REAL,
                ${StudentDatabase.StudentEntry.COLUMN_DEGREE} TEXT,
                ${StudentDatabase.StudentEntry.COLUMN_GENDER} TEXT,
                ${StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH} TEXT,
                ${StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST} TEXT
            );
        """.trimIndent()

        db.execSQL(SQL_CREATE_STUDENTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database schema upgrades here
    }
}