package at.connyduck.calladapter.networkresult

import retrofit2.http.GET

interface TestApi {

    @GET("testpath")
    suspend fun testEndpointAsync(): NetworkResult<TestResponseClass>

    @GET("testpath")
    fun testEndpointSync(): NetworkResult<TestResponseClass>
}
