# https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#retrofit
-keepattributes Signature
-keep class kotlin.coroutines.Continuation

-keep,allowobfuscation,allowshrinking class at.connyduck.calladapter.networkresult.NetworkResult
