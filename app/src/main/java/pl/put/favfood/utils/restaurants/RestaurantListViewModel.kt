package pl.put.favfood.utils.restaurants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place

class RestaurantListViewModel : ViewModel() {
    private var _restaurantList = MutableLiveData<List<Place>>(emptyList())
    var restaurantList : LiveData<List<Place>> = _restaurantList

    fun addPlace(place: Place) {
        _restaurantList.value = _restaurantList.value?.plus(place)
    }

    fun clear() {
        _restaurantList.value = emptyList()
    }
}