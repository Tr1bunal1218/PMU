package com.example.playerregistration

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var spinnerCourse: Spinner
    private lateinit var seekBarDifficulty: SeekBar
    private lateinit var tvDifficultyValue: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var spinnerYear: Spinner
    private lateinit var ivZodiac: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var tvOutput: TextView

    private var selectedDay = 1
    private var selectedMonth = 0 // 0-based
    private var selectedYear = 2000

    private val years = (1900..2100).toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Привязка виджетов
        etName = findViewById(R.id.etName)
        radioGroupGender = findViewById(R.id.radioGender)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        seekBarDifficulty = findViewById(R.id.seekBarDifficulty)
        tvDifficultyValue = findViewById(R.id.tvDifficultyValue)
        calendarView = findViewById(R.id.calendarView)
        spinnerYear = findViewById(R.id.spinnerYear)
        ivZodiac = findViewById(R.id.ivZodiac)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvOutput = findViewById(R.id.tvOutput)

        // Настраиваем Spinner курсов
        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter

        // SeekBar: показываем значение
        tvDifficultyValue.text = "Сложность: ${seekBarDifficulty.progress}"
        seekBarDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficultyValue.text = "Сложность: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Инициализация даты из CalendarView
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendarView.date
        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
        selectedMonth = cal.get(Calendar.MONTH)
        selectedYear = cal.get(Calendar.YEAR)

        // Настройка Spinner для годов
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        spinnerYear.setSelection(years.indexOf(selectedYear))

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long
            ) {
                val newYear = years[position]
                if (newYear != selectedYear) {
                    selectedYear = newYear
                    val cal = Calendar.getInstance()
                    cal.set(selectedYear, selectedMonth, selectedDay)
                    calendarView.date = cal.timeInMillis
                    updateZodiacImage(selectedDay, selectedMonth)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Показываем знак исходя из текущей выбранной даты
        updateZodiacImage(selectedDay, selectedMonth)

        // Слушатель для изменения даты
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDay = dayOfMonth
            selectedMonth = month
            selectedYear = year

            // Обновляем Spinner при изменении года
            val index = years.indexOf(year)
            if (index != -1) {
                spinnerYear.setSelection(index)
            }

            updateZodiacImage(selectedDay, selectedMonth)
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Введите ФИО"
                return@setOnClickListener
            }

            val genderId = radioGroupGender.checkedRadioButtonId
            val gender = if (genderId != -1) findViewById<RadioButton>(genderId).text.toString() else "Не указан"

            val course = spinnerCourse.selectedItem.toString()
            val difficulty = seekBarDifficulty.progress
            val zodiac = getZodiacSign(selectedDay, selectedMonth)
            val birthDateString = "$selectedDay.${selectedMonth + 1}.$selectedYear"

            val player = Player(name, gender, course, difficulty, birthDateString, zodiac)

            val outputText = """
                |ФИО: ${player.fullName}
                |Пол: ${player.gender}
                |Курс: ${player.course}
                |Сложность: ${player.difficulty}
                |Дата рождения: ${player.birthDate}
                |Знак зодиака: ${player.zodiac}
            """.trimMargin()

            tvOutput.text = outputText

            updateZodiacImage(selectedDay, selectedMonth)
        }
    }

    private fun updateZodiacImage(day: Int, month0Based: Int) {
        val sign = getZodiacSign(day, month0Based)
        val resName = "ic_${sign.lowercase()}"
        val resId = resources.getIdentifier(resName, "drawable", packageName)
        if (resId != 0) {
            ivZodiac.setImageResource(resId)
        } else {
            ivZodiac.setImageResource(android.R.drawable.ic_menu_help)
        }
    }

    private fun getZodiacSign(day: Int, month0Based: Int): String {
        val month = month0Based + 1
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "aries"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "taurus"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "gemini"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "cancer"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "leo"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "virgo"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "libra"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "scorpio"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "sagittarius"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "capricorn"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "aquarius"
            (month == 2 && day >= 19) || (month == 3 && day <= 20) -> "pisces"
            else -> "unknown"
        }
    }
}
