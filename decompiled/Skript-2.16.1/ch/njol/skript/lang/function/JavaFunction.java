package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Documentable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Contract;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.common.function.FunctionArguments;
import org.skriptlang.skript.common.function.Parameters;

@Deprecated(
   since = "2.13",
   forRemoval = true
)
public abstract class JavaFunction<T> extends Function<T> implements Documentable {
   @NotNull
   @Nullable
   private String[] returnedKeys;
   @Nullable
   private String[] description = null;
   @Nullable
   private String[] examples = null;
   @Nullable
   private String[] keywords;
   @Nullable
   private String since = null;

   public JavaFunction(Signature<T> sign) {
      super(sign);
   }

   public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
      this(name, parameters, returnType, single, null);
   }

   @Internal
   JavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
      this(script, name, parameters, returnType, single, true, null);
   }

   public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, @Nullable Contract contract) {
      this(null, name, parameters, returnType, single, false, contract);
   }

   @Internal
   JavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, boolean local, @Nullable Contract contract) {
      this(new Signature<>(script, name, parameters, local, returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));
   }

   @Nullable
   @Override
   public abstract T[] execute(FunctionEvent<?> var1, Object[][] var2);

   @Override
   public final T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments) {
      Parameters parameters = this.getSignature().parameters();
      Object[][] params = new Object[parameters.size()][];

      for (int i = 0; i < parameters.size(); i++) {
         Parameter<?> parameter = (Parameter<?>)parameters.get(i);
         Object object = arguments.get(parameter.name());
         if (object != null && object.getClass().isArray()) {
            params[i] = (Object[])object;
         } else if (object == null) {
            Expression<?> defaultExpression = parameter.getDefaultExpression();
            if (defaultExpression == null) {
               return null;
            }

            if (parameter.isSingle()) {
               params[i] = new Object[]{defaultExpression.getSingle(event)};
            } else {
               params[i] = defaultExpression.getArray(event);
            }
         } else {
            params[i] = new Object[]{object};
         }
      }

      T[] execute = this.execute(event, params);
      if (execute == null || execute.length == 0) {
         return null;
      } else {
         return (T)(execute.length == 1 ? execute[0] : execute);
      }
   }

   @NotNull
   @Nullable
   @Override
   public String[] returnedKeys() {
      return this.returnedKeys;
   }

   public void setReturnedKeys(@NotNull @Nullable String[] keys) {
      if (this.isSingle()) {
         throw new IllegalStateException("Cannot return keys for a single return function");
      } else {
         assert this.returnedKeys == null;

         this.returnedKeys = keys;
      }
   }

   public JavaFunction<T> description(String... description) {
      assert this.description == null;

      this.description = description;
      return this;
   }

   public JavaFunction<T> examples(String... examples) {
      assert this.examples == null;

      this.examples = examples;
      return this;
   }

   public JavaFunction<T> keywords(String... keywords) {
      assert this.keywords == null;

      this.keywords = keywords;
      return this;
   }

   public JavaFunction<T> since(String since) {
      assert this.since == null;

      this.since = since;
      return this;
   }

   @Nullable
   public String[] getDescription() {
      return this.description;
   }

   @Nullable
   public String[] getExamples() {
      return this.examples;
   }

   @Nullable
   public String[] getKeywords() {
      return this.keywords;
   }

   @Nullable
   public String getSince() {
      return this.since;
   }

   @Override
   public boolean resetReturnValue() {
      this.returnedKeys = null;
      return true;
   }

   @NotNull
   @Override
   public String name() {
      return this.getName();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> description() {
      return this.description != null ? List.of(this.description) : Collections.emptyList();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> since() {
      return this.since != null ? List.of(this.since) : Collections.emptyList();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> examples() {
      return this.examples != null ? List.of(this.examples) : Collections.emptyList();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> keywords() {
      return this.keywords != null ? List.of(this.keywords) : Collections.emptyList();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> requires() {
      return Collections.emptyList();
   }
}
