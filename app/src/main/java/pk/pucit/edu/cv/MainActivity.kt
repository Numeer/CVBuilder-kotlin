package pk.pucit.edu.cv

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
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

class MainActivity : AppCompatActivity() {
//    public ActivityDetailBinding Binding;

    private lateinit var dbHelper: StudentDbHelper
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val degreeSpinner = findViewById<Spinner>(R.id.degree)
        val degreeOptions = arrayOf("BSIT", "BSCS", "BSSE.", "Other")
        val degreeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, degreeOptions)
        degreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        degreeSpinner.adapter = degreeAdapter

        val buildCvButton = findViewById<Button>(R.id.buildCvButton)

        dbHelper = StudentDbHelper(this)

        buildCvButton.setOnClickListener(View.OnClickListener {
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
            val gpaText = "CGPA: $cgpa"
            if (cgpa.toDouble()<0 || cgpa.toDouble() > 4)
            {
                Toast.makeText(this, "Cgpa must be greater than 0 and less than 4: $gpaText", Toast.LENGTH_LONG).show()
            }
            else
            {
                val cvText = """
                Roll Number: $rollNumber
                Name: $name
                CGPA: $cgpa
                Degree: $selectedDegree
                Gender: $selectedGender
                Date of Birth: $dateOfBirth
                Career Interests: ${careerInterests.joinToString(", ")}
            """.trimIndent()
                insertData(
                    rollNumber,
                    name,
                    cgpa,
                    selectedDegree,
                    selectedGender,
                    dateOfBirth,
                    careerInterests
                )
                Toast.makeText(this, "Data Inserted Successfully: $cvText", Toast.LENGTH_LONG).show()
            }
        })

        val displayStudentDataButton = findViewById<Button>(R.id.displayStudentDataButton)

        displayStudentDataButton.setOnClickListener {
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
                val id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID))
                val rollNumber = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_ROLL_NUMBER))
                val name = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_NAME))
                val cgpa = cursor.getDouble(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_CGPA))
                val degree = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_DEGREE))
                val gender = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_GENDER))
                val dateOfBirth = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_DATE_OF_BIRTH))
                val careerInterests = cursor.getString(cursor.getColumnIndex(StudentDatabase.StudentEntry.COLUMN_CAREER_INTEREST))

                val studentData = """
                    ID: $id
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
            }

            cursor.close()
        }
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val enterIdEditText = findViewById<EditText>(R.id.enterId)

        deleteButton.setOnClickListener {
            val id = enterIdEditText.text.toString().toIntOrNull()

            if (id == null) {
                Toast.makeText(this, "Please enter a valid integer ID.", Toast.LENGTH_LONG).show()
            } else {
                val deletedRows = deleteData(id)

                if (deletedRows > 0) {
                    Toast.makeText(this, "Record with ID $id deleted successfully.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Record with ID $id not found.", Toast.LENGTH_LONG).show()
                }
            }
            enterIdEditText.text = null
        }
        val updateButton = findViewById<Button>(R.id.updateButton)
        val enterUpdateId = findViewById<EditText>(R.id.enterUpdateId)

        updateButton.setOnClickListener {
            val idToUpdateText = enterUpdateId.text.toString()

            if (idToUpdateText.isNotEmpty()) {
                try {
                    val idToUpdate = idToUpdateText.toLong()

                    if (isStudentIdExists(idToUpdate)) {
                        val intent = Intent(this, UpdateActivity::class.java)
                        intent.putExtra("studentId", idToUpdate)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Student ID not found. Please enter a valid student ID.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid student ID.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please enter a student ID.", Toast.LENGTH_LONG).show()
            }
            enterUpdateId.text = null
        }
    }
    private fun isStudentIdExists(studentId: Long): Boolean {
        val dbHelper = StudentDbHelper(this)
        val db = dbHelper.readableDatabase

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(studentId.toString())

        val cursor = db.query(
            StudentDatabase.StudentEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val studentExists = cursor.count > 0
        cursor.close()
        return studentExists
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
    private fun deleteData(id: Int): Int {
        val db = dbHelper.writableDatabase
        val whereClause = "${BaseColumns._ID} = ?"
        val whereArgs = arrayOf(id.toString())
        return db.delete(StudentDatabase.StudentEntry.TABLE_NAME, whereClause, whereArgs)
    }
}
