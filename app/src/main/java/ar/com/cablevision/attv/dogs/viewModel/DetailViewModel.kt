package ar.com.cablevision.attv.dogs.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ar.com.cablevision.attv.dogs.model.DogBreed
import ar.com.cablevision.attv.dogs.model.DogDatabase
import kotlinx.coroutines.launch

class DetailViewModel(application: Application): BaseViewModel(application)  {

    var dogLiveData: LiveData<DogBreed>? = null
    var dogUUID = 0

    fun fetch() {
        dogLiveData =DogDatabase(getApplication()).dogDao().getDog(dogUUID)
    }
}