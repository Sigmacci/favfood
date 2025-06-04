package pl.put.favfood.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable
import pl.put.favfood.utils.auth.AuthState
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel
import pl.put.favfood.utils.restaurants.RestaurantListViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userListViewModel: UserListViewModel,
    restaurantListViewModel: RestaurantListViewModel,
    placesClient: PlacesClient,
    uid: String
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Login)
            else -> Unit
        }
    }
    val user = FirebaseAuth.getInstance().currentUser

    val isMyPage = user?.uid == uid || uid == ""
    var username = remember { mutableStateOf("") }

    val restaurantList = restaurantListViewModel.restaurantList.observeAsState()
    val scrollState = rememberScrollState()

    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.ICON_URL,
        Place.Field.NAME,
        Place.Field.TYPES,
        Place.Field.ADDRESS,
        Place.Field.LAT_LNG
    )

    LaunchedEffect(Unit) {
        restaurantListViewModel.clear()
        val usedUid = if (isMyPage) user?.uid else uid
        userListViewModel.getUserByUid(usedUid!!) {
            if (it != null) {
                username.value = it.username
                for (restaurantId in it.restaurants) {
                    val fetchPlaceRequest =
                        FetchPlaceRequest.builder(restaurantId, placeFields).build()

                    placesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener { response ->
                            restaurantListViewModel.addPlace(response.place)
                        }
                }
            } else {
                Log.e("No user", "No user found")
            }
        }
    }

    Scaffold(
        bottomBar =  {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate(Home()) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "") },
                    label = { Text(text = "Home") },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Users) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "") },
                    label = { Text(text = "Users") },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Search) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "") },
                    label = { Text(text = "Search") },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { authViewModel.signOut() },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "") },
                    label = { Text(text = "Sign Out") },
                    alwaysShowLabel = true
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                if (isMyPage) {
                    Text(
                        text = "Hello, ${user?.displayName}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Check your favorite restaurants",
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        text = "${username.value}'s Favs",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (restaurantList.value!!.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No favorite places yet",
                            textAlign = TextAlign.Center
                        )
                    }
                }
                for (restaurant in restaurantList.value!!) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().height(112.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(
                                    model = restaurant.iconUrl,
                                    contentDescription = "",
                                    modifier = Modifier.size(32.dp),
                                    alignment = Alignment.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = restaurant.name!!.take(20) + if (restaurant.name!!.length > 20) "..." else "",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = restaurant.address!!,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                IconButton(onClick = {
                                    navController.navigate(RestaurantScreen(restaurant.id!!))
                                }) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Serializable
data class Home(var uid : String = "")