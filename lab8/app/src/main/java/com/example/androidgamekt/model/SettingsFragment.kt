package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SettingsFragment : Fragment() {

    private val settingsViewModel: GameSettingsViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sbGameSpeed = view.findViewById<SeekBar>(R.id.sbGameSpeed)
        val tvGameSpeed = view.findViewById<TextView>(R.id.tvGameSpeedValue)
        val sbBugSpeed = view.findViewById<SeekBar>(R.id.sbBugSpeed)
        val tvBugSpeed = view.findViewById<TextView>(R.id.tvBugSpeedValue)
        val sbMaxRoaches = view.findViewById<SeekBar>(R.id.sbMaxRoaches)
        val tvMaxRoaches = view.findViewById<TextView>(R.id.tvMaxRoachesValue)
        val sbBonusInterval = view.findViewById<SeekBar>(R.id.sbBonusInterval)
        val tvBonusInterval = view.findViewById<TextView>(R.id.tvBonusIntervalValue)
        val sbRoundDuration = view.findViewById<SeekBar>(R.id.sbRoundDuration)
        val tvRoundDuration = view.findViewById<TextView>(R.id.tvRoundDurationValue)
        val spDifficulty = view.findViewById<Spinner>(R.id.spDifficulty)

        // Setup difficulty spinner
        val difficulties = arrayOf("Легкий", "Средний", "Сложный")
        val difficultyAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficulties)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDifficulty.adapter = difficultyAdapter

        spDifficulty.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val difficulty = difficulties[position]
                settingsViewModel.setDifficulty(difficulty)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        sbGameSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(1)
                tvGameSpeed.text = formatGameSpeed(value)
                if (fromUser) {
                    settingsViewModel.setGameSpeed(value)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbBugSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(1)
                tvBugSpeed.text = formatBugSpeed(value)
                if (fromUser) {
                    settingsViewModel.setBugSpeed(value)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbMaxRoaches.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(1)
                tvMaxRoaches.text = formatMaxBugs(value)
                if (fromUser) {
                    settingsViewModel.setMaxBugs(value)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbBonusInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(0)
                tvBonusInterval.text = formatBonusInterval(value)
                if (fromUser) {
                    settingsViewModel.setBonusInterval(value)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbRoundDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(5)
                tvRoundDuration.text = formatRoundDuration(value)
                if (fromUser) {
                    settingsViewModel.setRoundDuration(value)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        settingsViewModel.settings.observe(viewLifecycleOwner) { settings ->
            if (sbGameSpeed.progress != settings.gameSpeed) {
                sbGameSpeed.progress = settings.gameSpeed
            }
            if (sbBugSpeed.progress != settings.bugSpeed) {
                sbBugSpeed.progress = settings.bugSpeed
            }
            if (sbMaxRoaches.progress != settings.maxBugs) {
                sbMaxRoaches.progress = settings.maxBugs
            }
            if (sbBonusInterval.progress != settings.bonusIntervalSeconds) {
                sbBonusInterval.progress = settings.bonusIntervalSeconds
            }
            if (sbRoundDuration.progress != settings.roundDurationSeconds) {
                sbRoundDuration.progress = settings.roundDurationSeconds
            }
            // Set difficulty spinner selection
            val difficultyIndex = difficulties.indexOf(settings.difficulty)
            if (difficultyIndex >= 0 && spDifficulty.selectedItemPosition != difficultyIndex) {
                spDifficulty.setSelection(difficultyIndex)
            }
            tvGameSpeed.text = formatGameSpeed(settings.gameSpeed)
            tvBugSpeed.text = formatBugSpeed(settings.bugSpeed)
            tvMaxRoaches.text = formatMaxBugs(settings.maxBugs)
            tvBonusInterval.text = formatBonusInterval(settings.bonusIntervalSeconds)
            tvRoundDuration.text = formatRoundDuration(settings.roundDurationSeconds)
        }
    }

    private fun formatGameSpeed(value: Int) = "Скорость появления тараканов: $value"

    private fun formatBugSpeed(value: Int) = "Скорость движения: $value"

    private fun formatMaxBugs(value: Int) = "Макс. тараканов: $value"

    private fun formatBonusInterval(value: Int) = "Интервал бонусов: ${value}с"

    private fun formatRoundDuration(value: Int) = "Длительность раунда: ${value}с"
}
