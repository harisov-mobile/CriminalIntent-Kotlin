package ru.internetcloud.criminalintent.model

import androidx.lifecycle.ViewModel
import ru.internetcloud.criminalintent.database.CrimeRepository

class CrimeListViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

}