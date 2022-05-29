package at.connyduck.calladapter.networkresult

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InterruptedIOException

public class TestCall<T> : Call<T> {
    private var executed = false
    private var canceled = false
    private var callback: Callback<T>? = null
    private var request = Request.Builder().url("http://example.com").build()

    public fun completeWithException(t: Throwable) {
        synchronized(this) {
            callback?.onFailure(this, t)
        }
    }

    public fun complete(body: T): Unit = complete(Response.success(body))

    public fun complete(response: Response<T>) {
        synchronized(this) {
            callback?.onResponse(this, response)
        }
    }

    override fun enqueue(callback: Callback<T>) {
        synchronized(this) {
            this.callback = callback
        }
    }

    public override fun isExecuted(): Boolean = synchronized(this) { executed }
    public override fun isCanceled(): Boolean = synchronized(this) { canceled }
    public override fun clone(): TestCall<T> = TestCall()

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

    public override fun request(): Request = request
    public override fun timeout(): Timeout = Timeout()
}
