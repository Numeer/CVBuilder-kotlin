package pk.pucit.edu.cv
import android.provider.BaseColumns

object StudentDatabase {
    object StudentEntry : BaseColumns {
        const val TABLE_NAME = "students"
        const val COLUMN_ROLL_NUMBER = "roll_number"
        const val COLUMN_NAME = "name"
        const val COLUMN_CGPA = "cgpa"
        const val COLUMN_DEGREE = "degree"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_DATE_OF_BIRTH = "date_of_birth"
        const val COLUMN_CAREER_INTEREST = "career_interest"

        const val DEGREE_BACHELORS = "Bachelor's"
        const val DEGREE_MASTERS = "Master's"
        const val DEGREE_PHD = "Ph.D."
        const val DEGREE_OTHER = "Other"

        const val CAREER_ACADEMIA = "Academia"
        const val CAREER_INDUSTRY = "Industry"
        const val CAREER_BUSINESS = "Business"
    }
}
