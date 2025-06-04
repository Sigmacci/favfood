package pl.put.favfood.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable
import pl.put.favfood.utils.auth.AuthState
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel

@Composable
fun RestaurantScreen(
    placeId: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    placesClient: PlacesClient,
    userListViewModel: UserListViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.TYPES,
        Place.Field.ADDRESS,
        Place.Field.LAT_LNG,
        Place.Field.PHOTO_METADATAS,
        Place.Field.EDITORIAL_SUMMARY,
        Place.Field.PHONE_NUMBER,
        Place.Field.OPENING_HOURS
    )

    var place by remember { mutableStateOf<Place?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val user = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient.fetchPlace(fetchPlaceRequest)
            .addOnSuccessListener { response ->
                place = response.place

                val metadata = place?.photoMetadatas?.firstOrNull()
                if (metadata != null) {
                    val fetchPhotoRequest = FetchResolvedPhotoUriRequest.builder(metadata)
                        .build()
                    placesClient.fetchResolvedPhotoUri(fetchPhotoRequest)
                        .addOnSuccessListener {
                            photoUri = it.uri
                        }
                }
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
                    onClick = { navController.navigate("") },
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
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            if (place != null && photoUri != null) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = if (photoUri != null) photoUri else "",
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = place?.name ?: "Unknown",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = place?.editorialSummary ?: "No summary available",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .then(Modifier.width(IntrinsicSize.Max))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Address:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp) // Adjust based on longest label
                            )
                            Text(
                                text = place?.address ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Phone number:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp) // same width
                            )
                            Text(
                                text = place?.phoneNumber ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Opening hours:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp) // same width
                            )
                            if (place?.openingHours != null) {
                                val period = place?.openingHours?.periods?.getOrNull(0)
                                val openTime = period?.open?.time
                                val closeTime = period?.close?.time

                                Text(
                                    text = if (openTime != null && closeTime != null) {
                                        "${openTime.hours}:${openTime.minutes.toString().padStart(2, '0')}" +
                                                " - ${closeTime.hours}:${closeTime.minutes.toString().padStart(2, '0')}"
                                    } else {
                                        "Opening hours not available"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            } else {
                                Text(
                                    text = "24/7",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                onClick = {
                                    navController.popBackStack()
                                },
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "")
                            }
                            Text(
                                text = "Close",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                onClick = {
                                    val uid = user?.uid
                                    val placeId = place?.id

                                    if (uid != null && placeId != null) {
                                        userListViewModel.addOrRemoveFavByUid(uid, placeId)
                                    } else {
                                        Log.e("RestaurantScreen", "UID or Place ID is null")
                                    }
                                },
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = "")
                            }
                            Text(
                                text = "Add to Fav",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                onClick = {
                                    navController.navigate(Map(place?.latLng!!.latitude, place?.latLng!!.longitude))
                                },
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = "")
                            }
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class RestaurantScreen(val placeId: String)