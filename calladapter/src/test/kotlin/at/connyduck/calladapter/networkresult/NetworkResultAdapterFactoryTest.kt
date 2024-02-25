package at.connyduck.calladapter.networkresult

import com.squareup.moshi.Types
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import retrofit2.Call
import retrofit2.Retrofit

class NetworkResultAdapterFactoryTest {
    private val retrofit = Retrofit.Builder().baseUrl("http://example.com").build()

    @Test
    fun `should return a NetworkResultCallAdapter`() {
        val responseType =
            Types.newParameterizedType(NetworkResult::class.java, TestResponseClass::class.java)
        val callType = Types.newParameterizedType(Call::class.java, responseType)

        val adapter = NetworkResultCallAdapterFactory().get(callType, arrayOf(), retrofit)

        assertInstanceOf(NetworkResultCallAdapter::class.java, adapter)
        assertEquals(TestResponseClass::class.java, adapter?.responseType())
    }

    @Test
    fun `should return a SyncNetworkResultCallAdapter`() {
        val responseType =
            Types.newParameterizedType(NetworkResult::class.java, TestResponseClass::class.java)

        val adapter = NetworkResultCallAdapterFactory().get(responseType, arrayOf(), retrofit)

        assertInstanceOf(SyncNetworkResultCallAdapter::class.java, adapter)
        assertEquals(TestResponseClass::class.java, adapter?.responseType())
    }

    @Test
    fun `should throw error if the type is not parameterized`() {
        assertThrows(
            IllegalStateException::class.java,
        ) { NetworkResultCallAdapterFactory().get(NetworkResult::class.java, arrayOf(), retrofit) }
    }

    @Test
    fun `should return null if the type is not supported`() {
        val adapter = NetworkResultCallAdapterFactory().get(TestResponseClass::class.java, arrayOf(), retrofit)

        assertNull(adapter)
    }
}
