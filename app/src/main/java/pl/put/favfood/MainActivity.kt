package pl.put.favfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.put.favfood.ui.theme.FavFoodTheme
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel
import pl.put.favfood.utils.navigation.Navigation
import pl.put.favfood.utils.restaurants.RestaurantListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        val userListViewModel: UserListViewModel by viewModels()
        val restaurantListViewModel: RestaurantListViewModel by viewModels()
        setContent {
            FavFoodTheme {
                Navigation(authViewModel = authViewModel, userListViewModel = userListViewModel, restaurantListViewModel = restaurantListViewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FavFoodTheme {
        Greeting("Android")
    }
}