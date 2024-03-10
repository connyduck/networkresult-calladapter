package at.connyduck.calladapter.networkresult

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkResultCallTest {
    private val backingCall = TestCall<String>()
    private val networkNetworkResultCall = NetworkResultCall(backingCall)

    @Test
    fun `should throw an error when invoking 'execute'`() {
        assertThrows<UnsupportedOperationException> {
            networkNetworkResultCall.execute()
        }
    }

    @Test
    fun `should delegate properties to backing call`() {
        with(networkNetworkResultCall) {
            assertEquals(isExecuted, backingCall.isExecuted)
            assertEquals(isCanceled, backingCall.isCanceled)
            assertEquals(request(), backingCall.request())
        }
    }

    @Test
    fun `should return new instance when cloned`() {
        val clonedCall = networkNetworkResultCall.clone()
        assert(clonedCall == networkNetworkResultCall)
    }

    @Test
    fun `should cancel backing call as well when cancelled`() {
        networkNetworkResultCall.cancel()
        assert(backingCall.isCanceled)
    }

    @Test
    fun `should parse successful call as NetworkResult-success`() {
        val body = "Test body"
        networkNetworkResultCall.enqueue(
            object : Callback<NetworkResult<String>> {
                override fun onResponse(
                    call: Call<NetworkResult<String>>,
                    response: Response<NetworkResult<String>>,
                ) {
                    assertTrue(response.isSuccessful)
                    assertEquals(
                        response.body(),
                        NetworkResult.success(body),
                    )
                }

                override fun onFailure(
                    call: Call<NetworkResult<String>>,
                    t: Throwable,
                ) {
                    throw IllegalStateException()
                }
            },
        )
        backingCall.complete(body)
    }

    @Test
    fun `should parse call with 404 error code as NetworkResult-failure`() {
        val errorCode = 404
        val errorBody = "not found"
        networkNetworkResultCall.enqueue(
            object : Callback<NetworkResult<String>> {
                override fun onResponse(
                    call: Call<NetworkResult<String>>,
                    response: Response<NetworkResult<String>>,
                ) {
                    assertEquals(
                        NetworkResult.failure<String>(
                            object : HttpException(Response.error<String>(errorCode, errorBody.toResponseBody())) {
                                override fun equals(other: Any?): Boolean {
                                    return (other is HttpException) && other.code() == code() && other.message() == message()
                                }
                            },
                        ),
                        response.body(),
                    )
                }

                override fun onFailure(
                    call: Call<NetworkResult<String>>,
                    t: Throwable,
                ) {
                    throw IllegalStateException()
                }
            },
        )

        backingCall.complete(Response.error(errorCode, errorBody.toResponseBody()))
    }

    @Test
    fun `should parse call with IOException as NetworkResult-failure`() {
        val exception = IOException()
        networkNetworkResultCall.enqueue(
            object : Callback<NetworkResult<String>> {
                override fun onResponse(
                    call: Call<NetworkResult<String>>,
                    response: Response<NetworkResult<String>>,
                ) {
                    assertEquals(
                        response.body(),
                        NetworkResult.failure<String>(exception),
                    )
                }

                override fun onFailure(
                    call: Call<NetworkResult<String>>,
                    t: Throwable,
                ) {
                    throw IllegalStateException()
                }
            },
        )

        backingCall.completeWithException(exception)
    }
}
