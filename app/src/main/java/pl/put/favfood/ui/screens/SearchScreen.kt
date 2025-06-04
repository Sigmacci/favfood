package pl.put.favfood.ui.screens

import android.Manifest
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.serialization.Serializable
import pl.put.favfood.utils.auth.AuthState
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.restaurants.RestaurantListViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    restaurantListViewModel: RestaurantListViewModel,
    placesClient: PlacesClient
) {
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    val authState = authViewModel.authState.observeAsState()

    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.ICON_URL,
        Place.Field.NAME,
        Place.Field.TYPES,
        Place.Field.ADDRESS,
        Place.Field.LAT_LNG
    )

    var circle : CircularBounds? = null
    val restaurantList = restaurantListViewModel.restaurantList.observeAsState()

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)

                    circle = CircularBounds.newInstance(latLng, 1000.0)
                    restaurantListViewModel.clear()
                    val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                        .setIncludedTypes(listOf("restaurant", "cafe"))
                        .setMaxResultCount(10)
                        .build()

                    placesClient.searchNearby(searchNearbyRequest)
                        .addOnSuccessListener { response ->
                            for (place in response.places) {
                                restaurantListViewModel.addPlace(place)
                            }
                        }
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Login)
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
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
                    selected = true,
                    onClick = {
                        navController.navigate(Search) {
                            popUpTo(Search) {
                                inclusive = true
                            }
                        }
                    },
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
                modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(scrollState),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search for restaurants...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        singleLine = true,
//                        leadingIcon = {
//                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
//                        },
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = {
                        restaurantListViewModel.clear()
                        if (locationPermissionState.status.isGranted && searchQuery != "") {
                            val autocompleteRequest = FindAutocompletePredictionsRequest.builder()
                                .setQuery(searchQuery)
                                .setLocationRestriction(
                                    RectangularBounds.newInstance(
                                        LatLng(cameraPositionState.position.target.latitude - 0.1, cameraPositionState.position.target.longitude - 0.1),
                                        LatLng(cameraPositionState.position.target.latitude + 0.1, cameraPositionState.position.target.longitude + 0.1)
                                    )
                                ).setOrigin(cameraPositionState.position.target)
                                .setTypesFilter(listOf("restaurant", "cafe"))
                                .build()

                            placesClient.findAutocompletePredictions(autocompleteRequest)
                                .addOnSuccessListener { response ->
                                    if (response.autocompletePredictions.isNotEmpty()) {
                                        var i = 0
                                        for (restaurant in response.autocompletePredictions) {
                                            if (i++ < 10) {
                                                val fetchPlaceRequest = FetchPlaceRequest.builder(
                                                    restaurant.placeId,
                                                    placeFields
                                                ).build()

                                                placesClient.fetchPlace(fetchPlaceRequest)
                                                    .addOnSuccessListener {
                                                        Log.d("Place", it.place.name!!)
                                                        restaurantListViewModel.addPlace(it.place)
                                                    }
                                                    .addOnFailureListener { error ->
                                                        Log.e("Fetch one", error.toString())
                                                    }
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { error ->
                                    Log.e("Fetch all", error.toString())
                                }
                        } else if (searchQuery == "") {
                            val searchNearbyRequest = SearchNearbyRequest.builder(circle!!, placeFields)
                                .setIncludedTypes(listOf("restaurant", "cafe"))
                                .setMaxResultCount(10)
                                .build()

                            placesClient.searchNearby(searchNearbyRequest)
                                .addOnSuccessListener { response ->
                                    for (place in response.places) {
                                        restaurantListViewModel.addPlace(place)
                                    }
                                }
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (restaurantList.value!!.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Nothing was found",
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
object Search