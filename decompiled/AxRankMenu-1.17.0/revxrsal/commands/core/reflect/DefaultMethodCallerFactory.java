package revxrsal.commands.core.reflect;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.ktx.call.KotlinConstants;

final class DefaultMethodCallerFactory implements MethodCallerFactory {
   public static final DefaultMethodCallerFactory INSTANCE = new DefaultMethodCallerFactory();

   @NotNull
   @Override
   public MethodCaller createFor(@NotNull Method method) throws Throwable {
      return KotlinConstants.isKotlinClass(method.getDeclaringClass())
         ? MethodCallerFactory.kotlinFunctions().createFor(method)
         : MethodCallerFactory.methodHandles().createFor(method);
   }
}
