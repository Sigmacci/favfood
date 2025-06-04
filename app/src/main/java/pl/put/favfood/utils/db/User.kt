package pl.put.favfood.utils.db

data class User(
    val username : String = "",
    val email : String = "",
    var restaurants : List<String> = emptyList()
)