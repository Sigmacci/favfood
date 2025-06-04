package pl.put.favfood.utils.db

data class Restaurant(
    val id : String = "",
    val name : String = "",
    val latitude : Double = 0.0,
    val longitude : Double = 0.0,
    val address : String = ""
)
