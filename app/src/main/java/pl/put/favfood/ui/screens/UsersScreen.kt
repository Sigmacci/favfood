package pl.put.favfood.ui.screens

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable
import pl.put.favfood.utils.auth.AuthState
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel

@Composable
fun UsersScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userListViewModel: UserListViewModel
) {
    val scrollState = rememberScrollState()
    val authState = authViewModel.authState.observeAsState()
    val userList = userListViewModel.filteredUserList.observeAsState()
    var searchQuery by remember { mutableStateOf("") }

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
                    selected = true,
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
                modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(scrollState),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Users",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            userListViewModel.getUsersByName(searchQuery)
                        },
                        label = { Text("Search for users...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (userList.value!!.isEmpty()) {
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
                } else {
                    for (user in userList.value) {
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
                                    Icon(Icons.Default.Person, contentDescription = "", modifier = Modifier.size(52.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = user.username.take(20) + if (user.username.length > 20) "..." else "",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.fillMaxHeight(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    IconButton(onClick = {
                                        userListViewModel.getUserByEmail(user.email) { uid ->
                                            if (uid != null) {
                                                navController.navigate(Home(uid))
                                            } else {
                                                Log.e("NOUID", "No uid here")
                                            }
                                        }
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
}

@Serializable
object Users