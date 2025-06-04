package pl.put.favfood.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import pl.put.favfood.ui.screens.Home
import pl.put.favfood.ui.screens.HomeScreen
import pl.put.favfood.ui.screens.Login
import pl.put.favfood.ui.screens.LoginScreen
import pl.put.favfood.ui.screens.Map
import pl.put.favfood.ui.screens.MapScreen
import pl.put.favfood.ui.screens.Register
import pl.put.favfood.ui.screens.RegisterScreen
import pl.put.favfood.ui.screens.RestaurantScreen
import pl.put.favfood.ui.screens.Search
import pl.put.favfood.ui.screens.SearchScreen
import pl.put.favfood.ui.screens.Users
import pl.put.favfood.ui.screens.UsersScreen
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel
import pl.put.favfood.utils.restaurants.RestaurantListViewModel

@androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    userListViewModel: UserListViewModel,
    restaurantListViewModel: RestaurantListViewModel
) {
    val navController = rememberNavController()

    val context = LocalContext.current
    if (!Places.isInitialized()) {
        Places.initializeWithNewPlacesApiEnabled(context, "AIzaSyCxiDMXK_9_E0q2VzjNtHghWfupEsJu5aw")
    }
    val placesClient = Places.createClient(context)

    NavHost(
        navController = navController,
        startDestination = Login
    ) {
        composable<Login> {
            LoginScreen(navController, authViewModel, userListViewModel)
        }
        composable<Register> {
            RegisterScreen(navController, authViewModel, userListViewModel)
        }
        composable<Home> {
            val args = it.toRoute<Home>()
            HomeScreen(navController, authViewModel, userListViewModel, restaurantListViewModel, placesClient, args.uid)
        }
        composable<Map> {
            val args = it.toRoute<Map>()
            val latlng = LatLng(args.latitude, args.longitude)
            MapScreen(navController, latlng)
        }
        composable<Search> {
            SearchScreen(navController, authViewModel, restaurantListViewModel, placesClient)
        }
        composable<RestaurantScreen> {
            val args = it.toRoute<RestaurantScreen>()
            RestaurantScreen(args.placeId, navController, authViewModel, placesClient, userListViewModel)
        }
        composable<Users> {
            UsersScreen(navController, authViewModel, userListViewModel)
        }
    }
}