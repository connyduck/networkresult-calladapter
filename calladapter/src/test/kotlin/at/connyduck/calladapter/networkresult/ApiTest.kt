package at.connyduck.calladapter.networkresult

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

class ApiTest {
    private var mockWebServer = MockWebServer()

    private lateinit var api: TestApi

    @BeforeEach
    fun setup() {
        mockWebServer.start()

        val moshi =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        api =
            Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(OkHttpClient())
                .build()
                .create(TestApi::class.java)
    }

    @AfterEach
    fun shutdown() {
        mockWebServer.shutdown()
    }

    private fun mockResponse(
        responseCode: Int,
        body: String = "",
    ) = MockResponse()
        .setResponseCode(responseCode)
        .setBody(body)

    @Test
    fun `suspending call - should return the correct test object`() {
        val response =
            mockResponse(
                200,
                """
                {
                    "lets": "not",
                    "test": 1
                }
            """,
            )

        mockWebServer.enqueue(response)

        val responseObject =
            runBlocking {
                api.testEndpointAsync()
            }

        assertEquals(
            NetworkResult.success(TestResponseClass("not", 1)),
            responseObject,
        )
    }

    @Test
    fun `blocking call - should return the correct test object`() {
        val response =
            mockResponse(
                200,
                """
                {
                    "lets": "not",
                    "test": 1
                }
            """,
            )

        mockWebServer.enqueue(response)

        val responseObject = api.testEndpointSync()

        assertEquals(
            NetworkResult.success(TestResponseClass("not", 1)),
            responseObject,
        )
    }

    @Test
    fun `suspending call - should return a ApiError failure when the server returns error 500`() {
        val errorCode = 500
        val response = mockResponse(errorCode)

        mockWebServer.enqueue(response)

        val responseObject =
            runBlocking {
                api.testEndpointAsync()
            }

        assertEquals(500, (responseObject.exceptionOrNull() as HttpException).code())
        assertEquals("Server Error", (responseObject.exceptionOrNull() as HttpException).message())
    }

    @Test
    fun `blocking call - should return a ApiError failure when the server returns error 500`() {
        val errorCode = 500
        val response = mockResponse(errorCode)

        mockWebServer.enqueue(response)

        val responseObject = api.testEndpointSync()

        assertEquals(500, (responseObject.exceptionOrNull() as HttpException).code())
        assertEquals("Server Error", (responseObject.exceptionOrNull() as HttpException).message())
    }

    @Test
    fun `suspending call - should return a NetworkError failure when the network fails`() {
        mockWebServer.enqueue(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
        val responseObject =
            runBlocking {
                api.testEndpointAsync()
            }

        assertEquals(
            NetworkResult.failure<TestResponseClass>(
                object : IOException() {
                    override fun equals(other: Any?): Boolean {
                        return (other is IOException)
                    }
                },
            ),
            responseObject,
        )
    }

    @Test
    fun `blocking call - should return a NetworkError failure when the network fails`() {
        mockWebServer.enqueue(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
        val responseObject = api.testEndpointSync()

        assertEquals(
            NetworkResult.failure<TestResponseClass>(
                object : IOException() {
                    override fun equals(other: Any?): Boolean {
                        return (other is IOException)
                    }
                },
            ),
            responseObject,
        )
    }
}
