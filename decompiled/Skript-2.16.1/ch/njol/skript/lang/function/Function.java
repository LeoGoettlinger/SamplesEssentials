package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameters;
import org.skriptlang.skript.common.function.ScriptParameter;

public abstract class Function<T> implements org.skriptlang.skript.common.function.Function<T> {
   public static boolean executeWithNulls = SkriptConfig.executeFunctionsWithMissingParams.value();
   private final Signature<T> sign;

   public Function(Signature<T> sign) {
      this.sign = sign;
   }

   public Signature<T> getSignature() {
      return this.sign;
   }

   @NotNull
   @Override
   public org.skriptlang.skript.common.function.Signature<T> signature() {
      return this.sign;
   }

   public String getName() {
      return this.sign.getName();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   public Parameter<?>[] getParameters() {
      return Arrays.stream(this.sign.getParameters()).map(Signature::toOldParameter).toArray(Parameter[]::new);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   public Parameter<?> getParameter(int index) {
      return Signature.toOldParameter(this.sign.getParameter(index));
   }

   public boolean isSingle() {
      return this.sign.isSingle();
   }

   @Nullable
   public ClassInfo<T> getReturnType() {
      return this.sign.getReturnType();
   }

   public Class<T> type() {
      return this.sign.returnType();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   @Nullable
   public final T[] execute(Object[][] params) {
      FunctionEvent<? extends T> event = new FunctionEvent<>(this);
      if (Functions.callFunctionEvents) {
         Bukkit.getPluginManager().callEvent(event);
      }

      Parameters parameters = this.sign.parameters();
      if (params.length > parameters.size()) {
         assert false : params.length;

         return null;
      } else {
         Object[][] parameterValues = params.length < parameters.size() ? Arrays.copyOf(params, parameters.size()) : params;
         int i = 0;

         for (org.skriptlang.skript.common.function.Parameter<?> parameter : parameters.all()) {
            Object[] parameterValue = (Object[])(parameter.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED)
               ? this.convertToKeyed(parameterValues[i])
               : parameterValues[i]);
            Expression<?> defaultValueExpr;
            if (parameter instanceof Parameter<?> script) {
               defaultValueExpr = script.def;
            } else if (parameter instanceof ScriptParameter<?> script) {
               defaultValueExpr = script.defaultValue();
            } else {
               defaultValueExpr = null;
            }

            if ((parameterValues[i] == null || parameterValues[i].length == 0)
               && parameter.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED)
               && defaultValueExpr != null) {
               Object[] defaultValue = defaultValueExpr.getArray(event);
               if (defaultValue.length == 1) {
                  parameterValue = KeyedValue.zip(defaultValue, null);
               } else {
                  parameterValue = defaultValue;
               }
            } else if (parameterValue == null && !(this instanceof DefaultFunction)) {
               assert defaultValueExpr != null;

               parameterValue = parameter.evaluate(defaultValueExpr, event);
            }

            if (!(this instanceof DefaultFunction) && !executeWithNulls && parameterValue != null && parameterValue.length == 0) {
               return null;
            }

            parameterValues[i] = parameterValue;
            i++;
         }

         T[] r = this.execute(event, parameterValues);

         assert this.sign.getReturnType() == null
            ? r == null
            : r == null
               || (r.length <= 1 || !this.sign.isSingle())
                  && !CollectionUtils.contains(r, null)
                  && this.sign.getReturnType().getC().isAssignableFrom(r.getClass().getComponentType()) : this + "; " + Arrays.toString(r);

         return r != null && r.length <= 0 ? null : r;
      }
   }

   @Nullable
   private KeyedValue<Object>[] convertToKeyed(Object[] values) {
      if (values == null) {
         return null;
      } else if (values.length == 0) {
         return new KeyedValue[0];
      } else {
         return values instanceof KeyedValue[] ? (KeyedValue[])values : KeyedValue.zip(values, null);
      }
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Nullable
   public abstract T[] execute(FunctionEvent<?> var1, Object[][] var2);

   @NotNull
   @Nullable
   @Override
   public String[] returnedKeys() {
      return null;
   }

   @Override
   public abstract boolean resetReturnValue();

   @Override
   public String toString() {
      return (this.sign.isLocal() ? "local " : "") + "function " + this.sign.getName();
   }
}
