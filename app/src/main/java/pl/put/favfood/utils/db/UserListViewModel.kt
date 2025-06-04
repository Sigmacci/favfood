package pl.put.favfood.utils.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserListViewModel : ViewModel() {
    private var db = Firebase.firestore

    private var _userList = MutableLiveData<List<User>>(emptyList())
    var userList : LiveData<List<User>> = _userList

    private var _filteredUserList = MutableLiveData<List<User>>(emptyList())
    var filteredUserList : LiveData<List<User>> = _filteredUserList

    init {
        getUsers()
        viewModelScope.launch {
            _userList.observeForever { list ->
                _filteredUserList.value = list
            }
        }
    }

    fun getUsers() {
        db.collection("user_details")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    _userList.value = value.toObjects()
                }
            }

    }

    fun getUserByUid(uid: String, onComplete: (User?) -> Unit) {
        db.collection("user_details")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onComplete(document.toObject(User::class.java))
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun getUserByEmail(email: String, onComplete: (String?) -> Unit) {
        db.collection("user_details")
            .get()
            .addOnSuccessListener { result ->
                val match = result.documents.firstOrNull { doc ->
                    val user = doc.toObject(User::class.java)
                    user?.email == email
                }

                onComplete(match?.id)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun getUsersByName(username: String) {
        if (username == "") {
            _filteredUserList.value = userList.value
        } else {
            _filteredUserList.value = _userList.value!!.filter { it.username.contains(username) }
        }
    }

    fun userExists(uid: String, onResult: (Boolean) -> Unit) {
        db.collection("user_details")
            .document(uid)
            .get()
            .addOnSuccessListener { document -> onResult(document.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    fun addUser(uid: String, username: String, email: String) {
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "restaurants" to emptyList<Restaurant>()
        )

        db.collection("user_details")
            .document(uid)
            .set(user)
            .addOnSuccessListener { Log.d("Firestore", "User added successfully") }
            .addOnFailureListener { Log.e("Firestore", "User was not added") }
    }

    fun addOrRemoveFavByUid(uid: String, restaurantId: String) {
        getUserByUid(uid) { user ->
            if (user == null) return@getUserByUid

            val restaurants = user.restaurants.toMutableList()

            if (restaurants.contains(restaurantId)) {
                restaurants.remove(restaurantId)
            } else {
                restaurants.add(restaurantId)
            }

            db.collection("user_details")
                .document(uid)
                .set(user.copy(restaurants = restaurants))
                .addOnSuccessListener {
                    Log.d("Firestore", "Data updated successfully")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Data was not updated")
                }
        }
    }

}