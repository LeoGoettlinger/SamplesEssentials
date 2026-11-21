package kotlin.coroutines.jvm.internal

import kotlin.coroutines.Continuation

@PublishedApi
internal fun <T> wrapContinuation(declaringClass: String, methodName: String, fileName: String, lineNumber: Int, spilledVariables: Array<Any?>, continuation: T): T
   where T : Continuation<Any?>
   
   T : CoroutineStackFrame
{
   return (T)continuation;
}

internal fun <T> wrapContinuationReal(
   declaringClass: String,
   methodName: String,
   fileName: String,
   lineNumber: Int,
   spilledVariables: Array<Any?>,
   continuation: T
): T where T : Continuation<Any?>, T : CoroutineStackFrame {
   return (T)((new TailCallBaseContinuationImpl(declaringClass, methodName, fileName, lineNumber, spilledVariables, continuation)) as Continuation);
}
