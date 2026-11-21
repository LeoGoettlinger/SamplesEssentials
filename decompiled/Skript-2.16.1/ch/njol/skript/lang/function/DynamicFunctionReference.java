package ch.njol.skript.lang.function;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Utils;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Executable;
import org.skriptlang.skript.util.Validated;

public class DynamicFunctionReference<Result> implements Contract, Executable<Event, Result[]>, Validated, AnyNamed {
   @NotNull
   private final String name;
   @Nullable
   private final Script source;
   private final Reference<Function<? extends Result>> function;
   @UnknownNullability
   private final Signature<? extends Result> signature;
   private final Validated validator = Validated.validator();
   private final Map<DynamicFunctionReference.Input, Expression<?>> checkedInputs = new HashMap<>();
   private final boolean resolved;

   public DynamicFunctionReference(Function<? extends Result> function) {
      this.resolved = true;
      this.function = new WeakReference<>(function);
      this.name = function.getName();
      this.signature = function.getSignature();
      File file = ScriptLoader.getScriptFromName(this.signature.namespace());
      this.source = file != null ? ScriptLoader.getScript(file) : null;
   }

   public DynamicFunctionReference(@NotNull String name) {
      this(name, null);
   }

   public DynamicFunctionReference(@NotNull String name, @Nullable Script source) {
      this.name = name;
      Function<? extends Result> function;
      if (source != null) {
         function = (Function<? extends Result>)Functions.getFunction(name, source.getConfig().getFileName());
      } else {
         function = (Function<? extends Result>)Functions.getFunction(name, null);
      }

      this.resolved = function != null;
      this.function = new WeakReference<>(function);
      if (this.resolved) {
         this.signature = function.getSignature();
         File file = ScriptLoader.getScriptFromName(this.signature.namespace());
         this.source = file != null ? ScriptLoader.getScript(file) : null;
      } else {
         this.signature = null;
         this.source = null;
      }
   }

   @Nullable
   public Script source() {
      return this.source;
   }

   @NotNull
   @Override
   public String name() {
      return this.name;
   }

   @Override
   public boolean isSingle(Expression<?>... arguments) {
      if (!this.resolved) {
         return true;
      } else {
         return this.signature.getContract() != null ? this.signature.getContract().isSingle(arguments) : this.signature.isSingle();
      }
   }

   @Nullable
   @Override
   public Class<?> getReturnType(Expression<?>... arguments) {
      if (!this.resolved) {
         return Object.class;
      } else if (this.signature.getContract() != null) {
         return this.signature.getContract().getReturnType(arguments);
      } else {
         Function<? extends Result> function = this.function.get();
         return function != null && function.getReturnType() != null ? function.getReturnType().getC() : null;
      }
   }

   @Nullable
   public Result[] execute(Event event, Object... arguments) {
      if (!this.valid()) {
         return null;
      } else {
         Function<? extends Result> function = this.function.get();
         if (function == null) {
            return null;
         } else {
            Object[][] consigned = FunctionReference.consign(arguments);

            Object[] var5;
            try {
               var5 = function.execute(consigned);
            } finally {
               function.resetReturnValue();
            }

            return (Result[])var5;
         }
      }
   }

   @Override
   public void invalidate() {
      this.validator.invalidate();
   }

   @Override
   public boolean valid() {
      return this.resolved && this.validator.valid() && this.function.get() != null && (this.source == null || this.source.valid());
   }

   @Override
   public String toString() {
      return this.source != null ? this.name + "() from " + Classes.toString(this.source) : this.name + "()";
   }

   @Nullable
   public Expression<?> validate(Expression<?>[] parameters) {
      DynamicFunctionReference.Input input = new DynamicFunctionReference.Input(parameters);
      return this.validate(input);
   }

   @Nullable
   public Expression<?> validate(DynamicFunctionReference.Input input) {
      if (this.checkedInputs.containsKey(input)) {
         return this.checkedInputs.get(input);
      } else {
         this.checkedInputs.put(input, null);
         if (this.signature == null) {
            return null;
         } else {
            boolean varArgs = this.signature.getMaxParameters() == 1 && !this.signature.parameters().getFirst().isSingle();
            Expression<?>[] inputParameters = input.parameters();
            if (inputParameters.length > this.signature.getMaxParameters() && !varArgs) {
               return null;
            } else if (inputParameters.length < this.signature.getMinParameters()) {
               return null;
            } else {
               Expression<?>[] checkedInputParameters = new Expression[inputParameters.length];

               for (int i = 0; i < inputParameters.length; i++) {
                  org.skriptlang.skript.common.function.Parameter<?> parameter = this.signature.parameters().all()[varArgs ? 0 : i];
                  Class<?> target = Utils.getComponentType(parameter.type());
                  Expression<?> expression = inputParameters[i].getConvertedExpression(target);
                  if (expression == null) {
                     return null;
                  }

                  if (parameter.isSingle() && !expression.isSingle()) {
                     return null;
                  }

                  checkedInputParameters[i] = expression;
               }

               ExpressionList<?> result = new ExpressionList<>(checkedInputParameters, Object.class, true);
               this.checkedInputs.put(input, result);
               return result;
            }
         }
      }
   }

   @Nullable
   public static DynamicFunctionReference<?> parseFunction(String name) {
      if (name.contains(") from ")) {
         String source = name.substring(name.lastIndexOf(" from ") + 6).trim();
         Script script = getScript(source);
         return resolveFunction(name.substring(0, name.lastIndexOf(" from ")).trim(), script);
      } else {
         return resolveFunction(name, null);
      }
   }

   @Nullable
   public static DynamicFunctionReference<?> resolveFunction(String name, @Nullable Script script) {
      if (name.contains("(") && name.contains(")")) {
         name = name.replaceAll("\\(.*\\).*", "").trim();
      }

      DynamicFunctionReference<Object> reference = new DynamicFunctionReference<>(name, script);
      return !reference.valid() ? null : reference;
   }

   @Nullable
   private static Script getScript(@Nullable String source) {
      if (source != null && !source.isEmpty()) {
         File file = ScriptLoader.getScriptFromName(source);
         return file != null && !file.isDirectory() ? ScriptLoader.getScript(file) : null;
      } else {
         return null;
      }
   }

   public static class Input {
      private final Class<?>[] types;
      private final transient Expression<?>[] parameters;

      public Input(Expression<?>... types) {
         Class<?>[] classes = new Class[types.length];

         for (int i = 0; i < types.length; i++) {
            classes[i] = types[i].getReturnType();
         }

         this.parameters = types;
         this.types = classes;
      }

      private Expression<?>[] parameters() {
         return this.parameters;
      }

      @Override
      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else {
            return !(object instanceof DynamicFunctionReference.Input input)
               ? false
               : Arrays.equals((Object[])this.parameters, (Object[])input.parameters) && Objects.deepEquals(this.types, input.types);
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode((Object[])this.types) ^ Arrays.hashCode((Object[])this.parameters);
      }
   }
}
