package ru.internetcloud.criminalintent

import android.app.Application
import ru.internetcloud.criminalintent.database.CrimeRepository

class CriminalIntentApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // делаю свои дела:
        CrimeRepository.initialize(this)
    }
}