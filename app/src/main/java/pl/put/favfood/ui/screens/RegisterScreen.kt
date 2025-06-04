package pl.put.favfood.ui.screens

import android.util.Log
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.serialization.Serializable
import pl.put.favfood.R
import pl.put.favfood.utils.auth.AuthState
import pl.put.favfood.utils.auth.AuthViewModel
import pl.put.favfood.utils.db.UserListViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userListViewModel: UserListViewModel
) {
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val userList = userListViewModel.userList.observeAsState()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Created -> {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val profileUpdate = userProfileChangeRequest {
                    displayName = nameState.value
                }
                currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        userListViewModel.addUser(currentUser.uid.toString(), nameState.value, emailState.value)
                        authViewModel.createdToAuthenticated()
                    } else {
                        Log.e("Profile", "Couldn't update profile info")
                    }
                }
            }
            is AuthState.Authenticated -> navController.navigate(Home())
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
                        text = "Sign Up",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text(text = "Username") }
                )
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
                    onClick = { authViewModel.signUp(emailState.value, passwordState.value) },
                    enabled = authState.value != AuthState.Loading
                ) {
                    Text(text = "Sign Up")
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { navController.navigate(Login) }
                ) {
                    Text(text = "Have an account? Sign in")
                }
            }
        }
    }
}

@Serializable
object Register