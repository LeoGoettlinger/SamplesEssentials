package revxrsal.commands.ktx

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull
import revxrsal.commands.CommandHandler
import revxrsal.commands.CommandHandlerVisitor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.process.ContextResolver
import revxrsal.commands.process.ContextResolver.ContextResolverContext

public object SuspendFunctionsSupport : CommandHandlerVisitor {
   public override fun visit(handler: CommandHandler) {
      handler.registerContextResolverFactory(SuspendFunctionsSupport::visit$lambda-0);
   }

   @JvmStatic
   fun `visit$lambda-0`(it: CommandParameter): ContextResolver {
      return if (it.isLastInMethod() && it.getType().isAssignableFrom(Continuation.class)) SuspendFunctionsSupport.ContinuationResolver.INSTANCE else null;
   }

   private class BasicContinuation<T>(handleException: (Throwable) -> Unit) : Continuation<T> {
      public open val context: EmptyCoroutineContext
         public open get() {
            return EmptyCoroutineContext.INSTANCE;
         }


      private final val handleException: (Throwable) -> Unit

      init {
         this.handleException = handleException;
      }

      public open fun resumeWith(result: Result<Any>) {
         val var10000: java.lang.Throwable = Result.exceptionOrNull-impl(result);
         if (var10000 != null) {
            this.handleException.invoke(var10000);
         }
      }
   }

   private object ContinuationResolver : ContextResolver<Continuation<? super Object>> {
      public open fun resolve(context: ContextResolverContext): Continuation<Any> {
         return new SuspendFunctionsSupport.BasicContinuation<>((new Function1<java.lang.Throwable, Unit>(context) {
            {
               super(1);
               this.$context = `$context`;
            }

            public final void invoke(@NotNull java.lang.Throwable it) {
               val var10000: CommandHandler = this.$context.commandHandler();
               var10000.getExceptionHandler().handleException(it, this.$context.actor());
            }
         }) as (java.lang.Throwable?) -> Unit);
      }
   }
}
