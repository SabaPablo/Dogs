package ar.com.cablevision.attv.dogs.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DogDao {

    @Insert
    suspend fun insertAll(vararg dogs: DogBreed): List<Long>

    @Query("SELECT * FROM dogbreed")
    suspend fun getAllDogs(): List<DogBreed>

    @Query("SELECT * FROM dogbreed WHERE uuid = :dogId")
    fun getDog(dogId:Int): LiveData<DogBreed>

    @Query("DELETE FROM dogbreed")
    suspend fun deleteAll()
}