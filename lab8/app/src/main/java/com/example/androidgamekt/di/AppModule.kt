package com.example.androidgamekt.di

import com.example.androidgamekt.model.GameSettingsViewModel
import com.example.androidgamekt.model.GameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { GameViewModel() }
    viewModel { GameSettingsViewModel() }
}

