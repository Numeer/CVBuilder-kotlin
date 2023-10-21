package pk.pucit.edu.cv

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import android.widget.TextView
import android.util.Log

class MainActivity : AppCompatActivity() {
//    public ActivityDetailBinding Binding;

    private lateinit var dbHelper: StudentDbHelper
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the degree Spinner
        val degreeSpinner = findViewById<Spinner>(R.id.degree)
        val degreeOptions = arrayOf("Bachelor's", "Master's", "Ph.D.", "Other")
        val degreeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, degreeOptions)
        degreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        degreeSpinner.adapter = degreeAdapter

        // Get a reference to the "Build a CV" button
        val buildCvButton = findViewById<Button>(R.id.buildCvButton)

        // Initialize your database helper
        dbHelper = StudentDbHelper(this)

        // Set an OnClickListener for the button
        buildCvButton.setOnClickListener(View.OnClickListener {
            // Gather user input from various fields
            val rollNumber = findViewById<EditText>(R.id.rollNumber).text.toString()
            val name = findViewById<EditText>(R.id.name).text.toString()
            val cgpa = findViewById<EditText>(R.id.cgpa).text.toString()
            val selectedDegree = degreeSpinner.selectedItem.toString()
            val selectedGender = if (findViewById<RadioButton>(R.id.male).isChecked) "Male" else "Female"
            val datePicker = findViewById<DatePicker>(R.id.dateOfBirth)
            val dateOfBirth = formatDateOfBirth(datePicker)
            val careerInterests = mutableListOf<String>()

            if (rollNumber.isEmpty() || name.isEmpty() || cgpa.isEmpty() || dateOfBirth.isEmpty()) {
                Toast.makeText(this, "Please fill in all the required fields.", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            if (findViewById<CheckBox>(R.id.academia).isChecked) {
                careerInterests.add("Academia")
            }
            if (findViewById<CheckBox>(R.id.industry).isChecked) {
                careerInterests.add("Industry")
            }
            if (findViewById<CheckBox>(R.id.business).isChecked) {
                careerInterests.add("Business")
            }
            insertData(rollNumber, name, cgpa, selectedDegree, selectedGender, dateOfBirth, careerInterests)

            val cvText = """
                Roll Number: $rollNumber
                Name: $name
                CGPA: $cgpa
                Degree: $selectedDegree
                Gender: $selectedGender
                Date of Birth: $dateOfBirth
                Career Interests: ${careerInterests.joinToString(", ")}
            """.trimIndent()

            Toast.makeText(this, "Data Inserted Successfully: $cvText", Toast.LENGTH_LONG).show()
        })

        val displayStudentDataButton = findViewById<Button>(R.id.displayStudentDataButton)

        displayStudentDataButton.setOnClickListener {
            val dbHelper = StudentDbHelper(this)
            val db = dbHelper.readableDatabase

            val projection = arrayOf(
                StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER,
                StudentDatabase.StudentEntry.COLUMN_NAME,
                StudentDatabase.StudentEntry.COLUMN_CGPA,
                StudentDatabase.StudentEntry.COLUMN_DEGREE,
                StudentDatabase.StudentEntry.COLUMN_GENDER,
                StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH,
                StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST
            )

            val cursor: Cursor = db.query(
                StudentDatabase.StudentEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
            )

            val studentDataTextView = findViewById<TextView>(R.id.studentDataTextView)
            studentDataTextView.text = ""

            while (cursor.moveToNext()) {
                val rollNumber = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER))
                val name = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_NAME))
                val cgpa = cursor.getDouble(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_CGPA))
                val degree = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_DEGREE))
                val gender = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_GENDER))
                val dateOfBirth = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH))
                val careerInterests = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST))

                val studentData = """
            Roll Number: $rollNumber
            Name: $name
            CGPA: $cgpa
            Degree: $degree
            Gender: $gender
            Date of Birth: $dateOfBirth
            Career Interests: $careerInterests
        """.trimIndent()
                studentDataTextView.append(studentData)
                studentDataTextView.append("\n\n")
                //displayStudentData(rollNumber, name, cgpa, degree, gender, dateOfBirth, careerInterests)
            }

            cursor.close()
        }
    }

    private fun insertData(
        rollNumber: String,
        name: String,
        cgpa: String,
        degree: String,
        gender: String,
        dateOfBirth: String,
        careerInterests: List<String>
    )
    {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER, rollNumber)
        values.put(StudentDatabase.StudentEntry.COLUMN_NAME, name)
        values.put(StudentDatabase.StudentEntry.COLUMN_CGPA, cgpa.toDouble())
        values.put(StudentDatabase.StudentEntry.COLUMN_DEGREE, degree)
        values.put(StudentDatabase.StudentEntry.COLUMN_GENDER, gender)
        values.put(StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH, dateOfBirth)
        values.put(StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST, careerInterests.joinToString(", "))

        db.insert(StudentDatabase.StudentEntry.TABLE_NAME, null, values)
    }

    private fun formatDateOfBirth(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1
        val year = datePicker.year

        return "$day-$month-$year"
    }
}
