package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Deprecated(
   since = "2.13",
   forRemoval = true
)
public abstract class SimpleJavaFunction<T> extends JavaFunction<T> {
   public SimpleJavaFunction(Signature<T> sign) {
      super(sign);
   }

   public SimpleJavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
      super(name, parameters, returnType, single);
   }

   @Internal
   SimpleJavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
      super(script, name, parameters, returnType, single);
   }

   public SimpleJavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, Contract contract) {
      super(name, parameters, returnType, single, contract);
   }

   @Nullable
   @Override
   public final T[] execute(FunctionEvent<?> event, Object[][] params) {
      for (Object[] param : params) {
         if (param == null || param.length == 0 || param[0] == null) {
            return null;
         }
      }

      return this.executeSimple(params);
   }

   @Nullable
   public abstract T[] executeSimple(Object[][] var1);
}
