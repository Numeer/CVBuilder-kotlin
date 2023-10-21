package pk.pucit.edu.cv

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

class MainActivity : AppCompatActivity() {
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

        // Set an OnClickListener for the button
        buildCvButton.setOnClickListener(View.OnClickListener {
            // Gather user input from various fields
            val rollNumber = findViewById<EditText>(R.id.rollNumber).text.toString()
            val name = findViewById<EditText>(R.id.name).text.toString()
            val cgpa = findViewById<EditText>(R.id.cgpa).text.toString()
            val selectedDegree = degreeSpinner.selectedItem.toString()
            val selectedGender = if (findViewById<RadioButton>(R.id.male).isChecked) "Male" else "Female"
            val dateOfBirth = findViewById<DatePicker>(R.id.dateOfBirth).toString()
            val careerInterests = mutableListOf<String>()

            // Check for empty fields and show an error message
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

            val cvText = """
                Roll Number: $rollNumber
                Name: $name
                CGPA: $cgpa
                Degree: $selectedDegree
                Gender: $selectedGender
                Date of Birth: $dateOfBirth
                Career Interests: ${careerInterests.joinToString(", ")}
            """.trimIndent()

            Toast.makeText(this, cvText, Toast.LENGTH_LONG).show()
        })
    }
}
