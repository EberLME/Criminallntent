package eber.loreto.criminallntent

data class Crime(
    @Primarykey val id: UUID,
    val title: String,
    val date: Date,
    val isSolved: Boolean
    val suspect: String = ""
    val photoFileName: String? = null

)
