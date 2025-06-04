package pl.put.favfood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn
import kotlinx.serialization.Serializable
import pl.put.favfood.R
import pl.put.favfood.utils.auth.AuthState
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userListViewModel: UserListViewModel
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val userList = userListViewModel.userList.observeAsState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val currentUser = FirebaseAuth.getInstance().currentUser

                userListViewModel.userExists(currentUser?.uid!!) { exists ->
                    if (!exists) userListViewModel.addUser(currentUser.uid.toString(), currentUser.displayName!!, currentUser.email!!)
                }
                navController.navigate(Home())
            }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher),
                        contentDescription = "",
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(24.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text(text = "Email") }
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text(text = "Password") }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { authViewModel.signIn(emailState.value, passwordState.value) },
                    enabled = authState.value != AuthState.Loading
                ) {
                    Text(text = "Sign In")
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { authViewModel.signInWithGoogle(context).launchIn(coroutineScope) }
                ) {
                    Image(
                        modifier = Modifier.width(24.dp),
                        painter = painterResource(id = R.drawable.goog),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Continue with Google")
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { navController.navigate(Register) }
                ) {
                    Text(text = "Don't have an account? Sign up")
                }
            }
        }
    }
}

@Serializable
object Login