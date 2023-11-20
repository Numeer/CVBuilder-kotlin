package pk.pucit.edu.cv
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UpdateActivity : AppCompatActivity() {

    private var studentId: Long = -1
    private lateinit var updatedRollNumber: EditText
    private lateinit var updatedName: EditText
    private lateinit var updatedCGPA: EditText
    private lateinit var updatedDegree: String
    private lateinit var updatedGender: String
    private lateinit var updatedDateOfBirth: String
    private lateinit var updatedCareerInterests: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        val degreeSpinner = findViewById<Spinner>(R.id.updatedDegree)
        val degreeOptions = arrayOf("Bachelor's", "Master's", "Ph.D.", "Other")
        val degreeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, degreeOptions)
        degreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        degreeSpinner.adapter = degreeAdapter

        updatedRollNumber = findViewById(R.id.updatedRollNumber)
        updatedName = findViewById(R.id.updatedName)
        updatedCGPA = findViewById(R.id.updatedCGPA)
        updatedDegree = degreeSpinner.selectedItem.toString()

        val maleRadioButton = findViewById<RadioButton>(R.id.updatedMale)
        val femaleRadioButton = findViewById<RadioButton>(R.id.updatedFemale)
        val datePicker = findViewById<DatePicker>(R.id.updatedDateOfBirth)

        val academiaCheckBox = findViewById<CheckBox>(R.id.updatedAcademia)
        val industryCheckBox = findViewById<CheckBox>(R.id.updatedIndustry)
        val businessCheckBox = findViewById<CheckBox>(R.id.updatedBusiness)

        val saveUpdateButton = findViewById<Button>(R.id.saveUpdateButton)

        studentId = intent.getLongExtra("studentId", -1)

        if (studentId.toInt() == -1) {
            Toast.makeText(this, "Invalid Student ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        loadStudentData(studentId)

        saveUpdateButton.setOnClickListener {
            val newRollNumber = updatedRollNumber.text.toString()
            val newName = updatedName.text.toString()
            val newCGPA = updatedCGPA.text.toString()
            val newDegree = degreeSpinner.selectedItem.toString()

            val gender = if (maleRadioButton.isChecked) "Male" else if (femaleRadioButton.isChecked) "Female" else ""

            val dob = formatDateOfBirth(datePicker)

            val interestList = mutableListOf<String>()
            if (academiaCheckBox.isChecked) {
                interestList.add("Academia")
            }
            if (industryCheckBox.isChecked) {
                interestList.add("Industry")
            }
            if (businessCheckBox.isChecked) {
                interestList.add("Business")
            }

            updateStudentRecord(studentId, newRollNumber, newName, newCGPA, newDegree, gender, dob, interestList)
            finish()
        }
    }

    @SuppressLint("Range")
    private fun loadStudentData(studentId: Long) {
        val dbHelper = StudentDbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER,
            StudentDatabase.StudentEntry.COLUMN_NAME,
            StudentDatabase.StudentEntry.COLUMN_CGPA,
            StudentDatabase.StudentEntry.COLUMN_DEGREE,
            StudentDatabase.StudentEntry.COLUMN_GENDER,
            StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH,
            StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST,
        )

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(studentId.toString())

        val cursor = db.query(
            StudentDatabase.StudentEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val rollNumber = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER))
            val name = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_NAME))
            val cgpa = cursor.getDouble(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_CGPA))

            updatedRollNumber.setText(rollNumber)
            updatedName.setText(name)
            updatedCGPA.setText(cgpa.toString())

            updatedDegree = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_DEGREE))

            val gender = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_GENDER))
            if (gender == "Male") {
                findViewById<RadioButton>(R.id.updatedMale).isChecked = true
            } else {
                findViewById<RadioButton>(R.id.updatedFemale).isChecked = true
            }

            updatedDateOfBirth = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH))

            // Load career interests and update the checkboxes based on them
            val interests = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST))
            val interestsList = interests.split(", ")
            val academiaCheckBox = findViewById<CheckBox>(R.id.updatedAcademia)
            val industryCheckBox = findViewById<CheckBox>(R.id.updatedIndustry)
            val businessCheckBox = findViewById<CheckBox>(R.id.updatedBusiness)

            academiaCheckBox.isChecked = interestsList.contains("Academia")
            industryCheckBox.isChecked = interestsList.contains("Industry")
            businessCheckBox.isChecked = interestsList.contains("Business")
        }
        cursor.close()
    }

    private fun updateStudentRecord(
        studentId: Long,
        newRollNumber: String,
        newName: String,
        newCGPA: String,
        newDegree: String,
        newGender: String,
        newDateOfBirth: String,
        newCareerInterests: List<String>
    ) {
        val dbHelper = StudentDbHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues()
        values.put(StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER, newRollNumber)
        values.put(StudentDatabase.StudentEntry.COLUMN_NAME, newName)
        values.put(StudentDatabase.StudentEntry.COLUMN_CGPA, newCGPA.toDouble())
        values.put(StudentDatabase.StudentEntry.COLUMN_DEGREE, newDegree)
        values.put(StudentDatabase.StudentEntry.COLUMN_GENDER, newGender)
        values.put(StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH, newDateOfBirth)
        values.put(StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST,  newCareerInterests.joinToString(", "))

        val whereClause = "${BaseColumns._ID} = ?"
        val whereArgs = arrayOf(studentId.toString())

        val updatedRows = db.update(
            StudentDatabase.StudentEntry.TABLE_NAME,
            values,
            whereClause,
            whereArgs
        )

        if (updatedRows > 0) {
            Toast.makeText(this, "Student record updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to update student record", Toast.LENGTH_SHORT).show()
        }
    }
    private fun formatDateOfBirth(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1
        val year = datePicker.year

        return "$day-$month-$year"
    }
}
