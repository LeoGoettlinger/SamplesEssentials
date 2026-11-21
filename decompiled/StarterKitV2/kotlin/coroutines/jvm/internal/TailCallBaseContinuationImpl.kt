package kotlin.coroutines.jvm.internal

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal class TailCallBaseContinuationImpl(declaringClass: String,
   methodName: String,
   fileName: String,
   lineNumber: Int,
   vararg spilledVariables: Any,
   continuation: Continuation<Any?>
) : BaseContinuationImpl(continuation) {
   public final val declaringClass: String
   public final val methodName: String
   public final val fileName: String
   public final val lineNumber: Int
   public final val spilledVariables: Array<Any?>
   private final val continuation: Continuation<Any?>

   public open val context: CoroutineContext
      public open get() {
         return this.continuation.getContext();
      }


   init {
      this.declaringClass = declaringClass;
      this.methodName = methodName;
      this.fileName = fileName;
      this.lineNumber = lineNumber;
      this.spilledVariables = spilledVariables;
      this.continuation = continuation;
   }

   protected override fun invokeSuspend(result: Result<Any?>): Any? {
      ResultKt.throwOnFailure(result);
      return result;
   }

   public override fun getStackTraceElement(): StackTraceElement {
      val moduleName: java.lang.String = ModuleNameRetriever.INSTANCE.getModuleName(this);
      return new StackTraceElement(
         if (moduleName == null) this.declaringClass else "$moduleName/${this.declaringClass}", this.methodName, this.fileName, this.lineNumber
      );
   }
}
