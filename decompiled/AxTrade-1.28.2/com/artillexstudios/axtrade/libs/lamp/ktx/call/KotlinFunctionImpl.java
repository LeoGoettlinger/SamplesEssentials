package com.artillexstudios.axtrade.libs.lamp.ktx.call;

import com.artillexstudios.axtrade.libs.lamp.core.reflect.MethodCaller;
import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import com.artillexstudios.axtrade.libs.lamp.util.Suppliers;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

final class KotlinFunctionImpl implements KotlinFunction {
   private final Supplier<Map<String, Parameter>> byName = Suppliers.lazy(() -> {
      Map<String, Parameter> byName = new HashMap<>();

      for (Parameter parameter : this.getParameters()) {
         byName.put(parameter.getName(), parameter);
      }

      return Collections.unmodifiableMap(byName);
   });
   private final CallableMethod mainMethod;
   private final Supplier<CallableMethod> defaultMethod;
   @Unmodifiable
   private final List<Parameter> parameters;

   public KotlinFunctionImpl(Method mainMethod) {
      MethodCaller mainCaller = KotlinSingletons.getCallerForNonDefault(mainMethod);
      this.parameters = Arrays.asList(mainMethod.getParameters());
      this.mainMethod = CallableMethod.of(mainMethod, mainCaller);
      this.defaultMethod = Suppliers.lazy(() -> DefaultFunctionFinder.findDefaultFunction(mainMethod));
   }

   @NotNull
   @Override
   public CallableMethod getMethod() {
      return this.mainMethod;
   }

   @Nullable
   @Override
   public CallableMethod getDefaultSyntheticMethod() {
      return this.defaultMethod.get();
   }

   @Override
   public boolean isSuspend() {
      Parameter lastParameter = com.artillexstudios.axtrade.libs.lamp.util.Collections.getOrNull(this.parameters, this.parameters.size() - 1);
      return lastParameter != null && lastParameter.getType() == KotlinConstants.continuation();
   }

   @Override
   public <T> T call(@Nullable Object instance, @NotNull List<Object> arguments, @NotNull Function<Parameter, Boolean> isOptional) {
      Map<Parameter, Object> callArgs = this.mapArgsToParams(i -> com.artillexstudios.axtrade.libs.lamp.util.Collections.getOrNull(arguments, i));
      return this.callByParameters(instance, callArgs, isOptional);
   }

   @Override
   public <T> T callByIndices(@Nullable Object instance, @NotNull Map<Integer, Object> arguments, @NotNull Function<Parameter, Boolean> isOptional) {
      Map<Parameter, Object> callArgs = this.mapArgsToParams(arguments::get);
      return this.callByParameters(instance, callArgs, isOptional);
   }

   @Override
   public <T> T callByNames(@Nullable Object instance, @NotNull Map<String, Object> arguments, @NotNull Function<Parameter, Boolean> isOptional) {
      return this.callByParameters(instance, com.artillexstudios.axtrade.libs.lamp.util.Collections.mapKeys(arguments, this::getParameter), isOptional);
   }

   @Override
   public <T> T callByParameters(@Nullable Object instance, @NotNull Map<Parameter, Object> arguments, @NotNull Function<Parameter, Boolean> isOptional) {
      Preconditions.checkCallableStatic(instance, this.mainMethod.getMethod());
      List<Object> args = new ArrayList<>();
      int mask = 0;
      List<Integer> masks = new ArrayList<>(1);
      int index = 0;
      boolean anyOptional = false;

      for (Parameter parameter : this.parameters) {
         if (index != 0 && index % 32 == 0) {
            masks.add(mask);
            mask = 0;
         }

         Object providedArg = arguments.get(parameter);
         if (providedArg != null) {
            args.add(providedArg);
         } else if (isOptional.apply(parameter)) {
            mask |= 1 << index % 32;
            args.add(KotlinConstants.defaultPrimitiveValue(parameter.getType()));
            anyOptional = true;
         } else {
            if (!parameter.isVarArgs()) {
               throw new IllegalArgumentException("No argument provided for a required parameter: " + parameter + ".");
            }

            args.add(Array.newInstance(parameter.getType(), 0));
         }

         index++;
      }

      if (!anyOptional) {
         return (T)this.mainMethod.getCaller().call(instance, args.toArray());
      } else {
         CallableMethod defaultMethod = this.defaultMethod.get();
         if (defaultMethod == null) {
            throw new IllegalArgumentException(
               "Unable to invoke function with default parameters. \nThis may happen because you have an @Optional non-null primitive type (e.g. Int) with no default value using @Default or a Kotlin-default value.\nEither mark it as nullable, add a default value (@Optional param: Type = ...), or use @Default"
            );
         } else {
            masks.add(mask);
            args.addAll(masks);
            args.add(null);
            return (T)defaultMethod.getCaller().call(instance, args.toArray());
         }
      }
   }

   @NotNull
   private Map<Parameter, Object> mapArgsToParams(@NotNull Function<Integer, Object> map) {
      Map<Parameter, Object> callArgs = new HashMap<>();

      for (int i = 0; i < this.parameters.size(); i++) {
         Parameter parameter = this.parameters.get(i);
         callArgs.put(parameter, map.apply(i));
      }

      return callArgs;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<Parameter> getParameters() {
      return this.parameters;
   }

   @NotNull
   @Unmodifiable
   @Override
   public Map<String, Parameter> getParametersByName() {
      return this.byName.get();
   }

   @NotNull
   @Override
   public Parameter getParameter(@NotNull String name) {
      return this.getParametersByName().get(name);
   }

   @NotNull
   @Override
   public Parameter getParameter(int index) {
      return this.parameters.get(index);
   }
}
