package org.enginehub.piston.converter;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.util.CaseHelper;

public class ArgumentConverters {
   private static final ArgumentConverter<String> STRING_ARGUMENT_CONVERTER = SimpleArgumentConverter.from(
      (s, c) -> SuccessfulConversion.fromSingle(s), "any text"
   );
   private static final String CONVERTER_CONVERT = "convert";
   private static final MethodType HANDLE_TO_CONVERTER = MethodType.methodType(Converter.class, MethodHandle.class);
   private static final MethodType CONVERTER_SIG = MethodType.methodType(ConversionResult.class, String.class, InjectedValueAccess.class);
   private static final MethodHandle HANDLE_TO_CONVERTER_CONVERTER;
   private static final MethodHandle SUCCESSFUL_CONVERSION_FROM;
   private static final MethodHandle FAILED_CONVERSION_FROM;
   private static final List<ArgumentConverters.ACProvider<Object>> PROVIDERS;

   public static ArgumentConverter<String> forString() {
      return STRING_ARGUMENT_CONVERTER;
   }

   private static <T> Optional<ArgumentConverter<T>> valueOfConverters(TypeToken<T> type) {
      Class<?> c = type.wrap().getRawType();

      MethodHandle handle;
      try {
         handle = MethodHandles.publicLookup().findStatic(c, "valueOf", MethodType.methodType(c, String.class));
      } catch (IllegalAccessException | NoSuchMethodException var4) {
         return Optional.empty();
      }

      handle = noContextConverter(handle);
      return Optional.of(converterForHandle(handle, c));
   }

   private static <T> Optional<ArgumentConverter<T>> constructorConverters(TypeToken<T> type) {
      Class<?> c = type.wrap().getRawType();

      MethodHandle handle;
      try {
         handle = MethodHandles.publicLookup().findConstructor(c, MethodType.methodType(void.class, String.class));
      } catch (IllegalAccessException | NoSuchMethodException var4) {
         return Optional.empty();
      }

      handle = noContextConverter(handle);
      return Optional.of(converterForHandle(handle, c));
   }

   private static MethodHandle noContextConverter(MethodHandle noContextHandle) {
      return MethodHandles.dropArguments(noContextHandle, 1, InjectedValueAccess.class);
   }

   private static <T> SimpleArgumentConverter<T> converterForHandle(MethodHandle handle, Class<?> type) {
      MethodType mType = handle.type();
      Preconditions.checkArgument(
         mType.parameterType(0).isAssignableFrom(String.class)
            && mType.parameterType(1).isAssignableFrom(InjectedValueAccess.class)
            && type.isAssignableFrom(mType.returnType()),
         "Incorrect signature: %s",
         handle
      );
      handle = augmentHandleForConversionResult(handle);

      Converter<T> converter;
      try {
         converter = (Converter)HANDLE_TO_CONVERTER_CONVERTER.invokeExact((MethodHandle)handle);
      } catch (Throwable var5) {
         Throwables.throwIfUnchecked(var5);
         throw new RuntimeException(var5);
      }

      return SimpleArgumentConverter.from(converter, "any " + CaseHelper.titleToSpacedLower(type.getSimpleName()));
   }

   private static MethodHandle augmentHandleForConversionResult(MethodHandle delegate) {
      MethodHandle result = MethodHandles.filterReturnValue(
         delegate, SUCCESSFUL_CONVERSION_FROM.asType(MethodType.methodType(ConversionResult.class, delegate.type().returnType()))
      );
      return MethodHandles.catchException(
         result, Throwable.class, FAILED_CONVERSION_FROM.asType(MethodType.methodType(ConversionResult.class, Throwable.class))
      );
   }

   public static <T> ArgumentConverter<T> get(TypeToken<T> type) {
      return (ArgumentConverter<T>)(type.getRawType().equals(String.class)
         ? forString()
         : (ArgumentConverter)PROVIDERS.stream()
            .map(x -> x.provideAc(type))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No built-in converters for " + type)));
   }

   private ArgumentConverters() {
   }

   static {
      MethodHandle handleInvoker = MethodHandles.invoker(CONVERTER_SIG);

      try {
         HANDLE_TO_CONVERTER_CONVERTER = LambdaMetafactory.metafactory(
               MethodHandles.lookup(), "convert", HANDLE_TO_CONVERTER, CONVERTER_SIG, handleInvoker, CONVERTER_SIG
            )
            .dynamicInvoker();
      } catch (LambdaConversionException var3) {
         throw new IllegalStateException("Failed to load ArgumentConverter MetaFactory", var3);
      }

      try {
         SUCCESSFUL_CONVERSION_FROM = MethodHandles.lookup()
            .findStatic(SuccessfulConversion.class, "fromSingle", MethodType.methodType(SuccessfulConversion.class, Object.class));
         FAILED_CONVERSION_FROM = MethodHandles.lookup()
            .findStatic(FailedConversion.class, "from", MethodType.methodType(FailedConversion.class, Throwable.class));
      } catch (IllegalAccessException | NoSuchMethodException var2) {
         throw new IllegalStateException(var2);
      }

      PROVIDERS = ImmutableList.of(
         ArgumentConverters::valueOfConverters,
         ArgumentConverters::constructorConverters,
         (ArgumentConverters.ACProvider<>)type -> Objects.equals(type.wrap().getRawType(), Character.class)
            ? Optional.of(SimpleArgumentConverter.from((s, c) -> SuccessfulConversion.fromSingle(s.charAt(0)), "any character"))
            : Optional.empty()
      );
   }

   private interface ACProvider<T> {
      Optional<ArgumentConverter<T>> provideAc(TypeToken<T> var1);
   }
}
