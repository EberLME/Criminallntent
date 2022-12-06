package eber.loreto.criminallntent.CrimeDatabase

import android.app.Application

class CriminalIntentApplication : Application{
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}