package revxrsal.commands.core.reflect;

import java.lang.reflect.Method;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.ktx.call.KotlinConstants;
import revxrsal.commands.ktx.call.KotlinFunction;
import revxrsal.commands.util.Collections;

final class KotlinMethodCallerFactory implements MethodCallerFactory {
   public static final KotlinMethodCallerFactory INSTANCE = new KotlinMethodCallerFactory();

   @NotNull
   @Override
   public MethodCaller createFor(@NotNull Method method) {
      KotlinFunction function = KotlinFunction.wrap(method);
      return (instance, arguments) -> {
         List<Object> list = Collections.listOf(arguments);
         list.replaceAll(v -> v == KotlinConstants.ABSENT_VALUE ? null : v);
         return function.call(instance, list, parameter -> parameter.isAnnotationPresent(Optional.class));
      };
   }
}
