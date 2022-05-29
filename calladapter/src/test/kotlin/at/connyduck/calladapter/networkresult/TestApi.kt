package at.connyduck.calladapter.networkresult

import retrofit2.http.GET

public interface TestApi {

    @GET("testpath")
    public suspend fun testEndpointAsync(): NetworkResult<TestResponseClass>

    @GET("testpath")
    public fun testEndpointSync(): NetworkResult<TestResponseClass>
}
