package com.example.androidgamekt.model

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.androidgamekt.R
import com.example.androidgamekt.data.AppDatabase
import com.example.androidgamekt.data.Prefs
import com.example.androidgamekt.data.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : Fragment() {
    private var usersSpinnerRef: Spinner? = null
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.signup_activity, container, false)

        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        val spCourse = view.findViewById<Spinner>(R.id.spCourse)
        val cvBirthDate = view.findViewById<CalendarView>(R.id.cvBirthDate)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteUser)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = adapter

        var selectedDate = ""

        // Users spinner to choose existing users
        val usersSpinner = Spinner(requireContext())
        (view as ViewGroup).addView(usersSpinner, 0)
        usersSpinnerRef = usersSpinner
        loadUsersIntoSpinner(usersSpinner)

        cvBirthDate.setOnDateChangeListener { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
        }

        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString()
            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Мужской"
                R.id.rbFemale -> "Женский"
                else -> "Не указан"
            }

            if (fullName.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, введите ФИО", Toast.LENGTH_SHORT).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (gender == "Не указан") {
                Toast.makeText(requireContext(), "Пожалуйста, выберите пол", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!fullName.matches(Regex("^[А-Яа-яA-Za-z\\s-]+$")) || fullName.split("\\s+".toRegex()).size < 2) {
                Toast.makeText(requireContext(), "ФИО должно содержать минимум 2 слова и только буквы, пробелы или дефисы", Toast.LENGTH_LONG).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            val course = spCourse.selectedItem.toString()
            val zodiacSign = getZodiacSign(selectedDate)

            val zodiacDrawable = when (zodiacSign) {
                "Овен" -> R.drawable.aries
                "Телец" -> R.drawable.taurus
                "Близнецы" -> R.drawable.gemini
                "Рак" -> R.drawable.cancer
                "Лев" -> R.drawable.leo
                "Дева" -> R.drawable.virgo
                "Весы" -> R.drawable.libra
                "Скорпион" -> R.drawable.scorpio
                "Стрелец" -> R.drawable.sagittarius
                "Козерог" -> R.drawable.capricorn
                "Водолей" -> R.drawable.aquarius
                "Рыбы" -> R.drawable.pisces
                else -> R.drawable.ic_launcher_background
            }
            ivZodiac.setImageResource(zodiacDrawable)

            val player = Player(fullName, gender, course, 0, selectedDate, zodiacSign)

            tvResult.text = """
                ФИО: ${player.fullName}
                Пол: ${player.gender}
                Курс: ${player.course}
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiacSign}
            """.trimIndent()

            lifecycleScope.launch {
                val (userId, created) = withContext(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(requireContext()).userDao()
                    val existing = dao.getByName(player.fullName)
                    val id = existing?.id ?: dao.insert(UserEntity(fullName = player.fullName))
                    id to (existing == null)
                }
                Prefs(requireContext()).setCurrentUserId(userId)
                Toast.makeText(requireContext(), if (created) "Пользователь создан и выбран" else "Пользователь выбран", Toast.LENGTH_SHORT).show()
                usersSpinnerRef?.let { loadUsersIntoSpinner(it) }
            }
        }

        btnDelete.setOnClickListener {
            val spinner = usersSpinnerRef ?: return@setOnClickListener
            val position = spinner.selectedItemPosition
            if (position <= 0) {
                Toast.makeText(requireContext(), "Сначала выберите пользователя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val deletedId = withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(requireContext())
                    val users = db.userDao().getAll()
                    val user = users.getOrNull(position - 1) ?: return@withContext null
                    db.userDao().deleteById(user.id)
                    user.id
                }
                if (deletedId != null) {
                    val prefs = Prefs(requireContext())
                    if (prefs.getCurrentUserId() == deletedId) {
                        prefs.setCurrentUserId(-1)
                    }
                    Toast.makeText(requireContext(), "Пользователь удалён", Toast.LENGTH_SHORT).show()
                    usersSpinnerRef?.let { loadUsersIntoSpinner(it) }
                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        usersSpinnerRef?.let { loadUsersIntoSpinner(it) }
    }

    private fun loadUsersIntoSpinner(spinner: Spinner) {
        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext()).userDao().getAll()
            }
            val names = listOf("Выбрать пользователя") + users.map { it.fullName }
            val usersAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = usersAdapter

            // Restore current selection if set
            val currentId = Prefs(requireContext()).getCurrentUserId()
            if (currentId != null) {
                val idx = users.indexOfFirst { it.id == currentId }
                if (idx >= 0) spinner.setSelection(idx + 1)
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    if (position > 0) {
                        val user = users[position - 1]
                        Prefs(requireContext()).setCurrentUserId(user.id)
                        Toast.makeText(requireContext(), "Текущий пользователь: ${user.fullName}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun getZodiacSign(date: String): String {
        if (date.isEmpty()) return "Не выбрана дата"

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = sdf.parse(date) ?: return "Ошибка даты"
            val calendar = Calendar.getInstance().apply { time = birthDate }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1

            return when {
                (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
                (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
                (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
                (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
                (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
                (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
                (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
                (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
                (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
                (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козерог"
                (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
                else -> "Рыбы"
            }
        } catch (e: Exception) {
            return "Ошибка обработки даты"
        }
    }
}