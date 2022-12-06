package eber.loreto.criminallntent

import androidx.lifecycle.ViewModel

private const val TAG = "CrimeListViewModel"

class CrimeListViewModel : ViewModel(){
    private val crimeRepository = CrimeRepository.get()
    //val crimes = crimeRepository.getCrimes()
    private val _crimes: MutableStateFlow<List<Crime>> = MutableStateFlow(emptyList())
    val crimes: StateFlow<List<Crime>>
        get() = _crimes.asStateFlow()

    init {
        viewModelScope.launch {
            crimeRepository.getCrimes().collect {
                _crimes.value = it
            }
        }
    }
    suspend fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

}