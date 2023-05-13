package at.connyduck.calladapter.networkresult

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InterruptedIOException

class TestCall<T> : Call<T> {
    private var executed = false
    private var canceled = false
    private var callback: Callback<T>? = null
    private var request = Request.Builder().url("http://example.com").build()

    fun completeWithException(t: Throwable) {
        synchronized(this) {
            callback?.onFailure(this, t)
        }
    }

    fun complete(body: T): Unit = complete(Response.success(body))

    fun complete(response: Response<T>) {
        synchronized(this) {
            callback?.onResponse(this, response)
        }
    }

    override fun enqueue(callback: Callback<T>) {
        synchronized(this) {
            this.callback = callback
        }
    }

    override fun isExecuted(): Boolean = synchronized(this) { executed }
    override fun isCanceled(): Boolean = synchronized(this) { canceled }
    override fun clone(): TestCall<T> = TestCall()

    override fun cancel() {
        synchronized(this) {
            if (canceled) return
            canceled = true

            val exception = InterruptedIOException("canceled")
            callback?.onFailure(this, exception)
        }
    }

    override fun execute(): Response<T> {
        throw UnsupportedOperationException("Network call does not support synchronous execution")
    }

    override fun request(): Request = request
    override fun timeout(): Timeout = Timeout()
}
