package ar.com.cablevision.attv.dogs.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import ar.com.cablevision.attv.dogs.model.DogBreed
import ar.com.cablevision.attv.dogs.model.DogDatabase
import ar.com.cablevision.attv.dogs.model.DogsApiService
import ar.com.cablevision.attv.dogs.utils.NotificationsHelper
import ar.com.cablevision.attv.dogs.utils.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.lang.NumberFormatException


class ListViewModel(application: Application): BaseViewModel(application) {


    private val dogsService = DogsApiService()
    private val disposable = CompositeDisposable()

    val dogs = MutableLiveData<List<DogBreed>>()
    val dogsLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()
    val prefer = SharedPreferencesHelper(getApplication())
    private var refreshTime: Long = 5 * 60 * 1000 * 1000 * 1000L

    fun refresh() {
        checkCacheDuration()
        val updateTime = prefer.getUpdateTime()
        if(updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        } else {
            fetchFromRemote()
        }
    }

    private fun checkCacheDuration() {
        val cachePreference = prefer.getCacheDuration()
        try {
            val cachePreferenceInt = cachePreference?.toInt() ?: 5*60
            refreshTime = cachePreferenceInt.times(1000 * 1000 * 1000L)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }
    fun refreshBypassCache() {
        fetchFromRemote()
    }

    private fun fetchFromDatabase() {
        loading.value = true
        launch {
            val dogs = DogDatabase(getApplication()).dogDao().getAllDogs()
            dogsRetrieved(dogs)
            Toast.makeText(getApplication(),"Dogs retrieved from database", Toast.LENGTH_SHORT).show()
            NotificationsHelper(getApplication()).createNotification()
        }
    }

    private fun fetchFromRemote(){
        loading.value = true
        disposable.add(
            dogsService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<DogBreed>>(){
                    override fun onSuccess(dogList: List<DogBreed>) {
                        storeDogsLocally(dogList)
                    }

                    override fun onError(e: Throwable) {
                        dogsLoadError.value = true
                        loading.value = false
                        e.printStackTrace()
                    }

                })
        )
    }

    private fun storeDogsLocally(dogList: List<DogBreed>) {
        launch {
            val dao = DogDatabase(getApplication()).dogDao()
            dao.deleteAll()
            val result = dao.insertAll(*dogList.toTypedArray())
            var i = 0
            while (i < dogList.size) {
                dogList[i].uuid = result[i].toInt()
                ++i
            }
            dogsRetrieved(dogList)
        }
        prefer.saveUpdateTime(System.nanoTime())
    }

    private fun dogsRetrieved(dogList: List<DogBreed>?) {
        dogs.value = dogList
        dogsLoadError.value = false
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}