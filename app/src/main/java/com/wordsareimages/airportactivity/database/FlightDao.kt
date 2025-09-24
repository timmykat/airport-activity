import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.wordsareimages.airportactivity.models.FlightInfo
import java.time.ZonedDateTime

@Dao
interface FlightDao {

    @Query("SELECT * FROM flights WHERE operation = :operation ORDER BY timestamp ASC")
    fun getFlights(operation: String): Flow<List<FlightInfo>>

    @Query("SELECT * FROM flights ORDER BY timestamp ASC")
    fun getAllFlights(): Flow<List<FlightInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlights(flights: List<FlightInfo>)

    @Query("DELETE FROM flights WHERE timestamp < :cutoff")
    suspend fun deleteOldFlights(cutoff: ZonedDateTime)

    @Query("DELETE FROM flights")
    suspend fun clearAllFlights()
}
