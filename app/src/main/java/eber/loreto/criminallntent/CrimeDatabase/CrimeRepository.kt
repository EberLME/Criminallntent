package eber.loreto.criminallntent.CrimeDatabase

private const val DATABASE_NAME = "crime-database"

class CrimeRepository  private constructor(context: Context, private val coroutineScope: CoroutineScope = GlobalScope
){

    private val database: CrimeDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CrimeDatabase::class.java,
            DATABASE_NAME
        )
        .addMigrations(migration_2_3)
        //.createFromAsset(DATABASE_NAME)
        .build()

    fun getCrimes(): Flow<List<Crime>>
        = database.crimeDao().getCrimes()
    suspend fun getCrime(id: UUID): Crime = database.crimeDao().getCrime(id)
    fun updateCrime(crime: Crime) {
        coroutineScope.launch {
            //suspend fun updateCrime(crime: Crime) {

                database.crimeDao().updateCrime(crime)
        }
    }

    suspend fun addCrime(crime: Crime) {
        database.crimeDao().addCrime(crime)
    }

    companion object {
        private var INSTANCE: CrimeRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }
        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}