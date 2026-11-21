package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

@ApiStatus.Internal
public final class Accessors {
   private static final Lookup ROOT_LOOKUP = MethodHandles.lookup();

   private Accessors() {
   }

   @NotNull
   public static Object build(@NotNull Class<?> ownerType, @NotNull String accessorName, @NotNull Class<?> fieldType) {
      TypeTag tag = TypeTag.of(fieldType);

      try {
         MethodHandle accessor = ROOT_LOOKUP.findVirtual(ownerType, accessorName, MethodType.methodType(fieldType));

         return switch (tag) {
            case INT -> buildPrimitive(ownerType, accessor, ToIntFunction.class, "applyAsInt", int.class);
            case LONG -> buildPrimitive(ownerType, accessor, ToLongFunction.class, "applyAsLong", long.class);
            case DOUBLE -> buildPrimitive(ownerType, accessor, ToDoubleFunction.class, "applyAsDouble", double.class);
            case FLOAT -> buildFloatAsDouble(ownerType, accessor);
            case BOOLEAN -> buildReference(ownerType, accessor, Boolean.class);
            case STRING -> buildReference(ownerType, accessor, String.class);
            case BYTES -> buildReference(ownerType, accessor, byte[].class);
            case UUID -> buildReference(ownerType, accessor, UUID.class);
            case FLOAT_ARRAY -> buildReference(ownerType, accessor, float[].class);
            case ENUM -> buildReference(ownerType, accessor, fieldType);
            case NESTED_SEALED -> buildReference(ownerType, accessor, fieldType);
         };
      } catch (Throwable var5) {
         throw new IllegalStateException("could not build accessor for " + ownerType.getName() + "." + accessorName, var5);
      }
   }

   @NotNull
   private static Object buildPrimitive(
      @NotNull Class<?> ownerType,
      @NotNull MethodHandle accessor,
      @NotNull Class<?> functionalInterface,
      @NotNull String methodName,
      @NotNull Class<?> primitiveReturn
   ) throws Throwable {
      MethodType invokedType = MethodType.methodType(functionalInterface);
      MethodType samMethod = MethodType.methodType(primitiveReturn, Object.class);
      MethodType implType = MethodType.methodType(primitiveReturn, ownerType);
      CallSite cs = LambdaMetafactory.metafactory(ROOT_LOOKUP, methodName, invokedType, samMethod, accessor, implType);
      return (Object)cs.getTarget().invoke();
   }

   @NotNull
   private static ToDoubleFunction<?> buildFloatAsDouble(@NotNull Class<?> ownerType, @NotNull MethodHandle accessor) throws Throwable {
      MethodType invokedType = MethodType.methodType(ToDoubleFunction.class);
      MethodType samMethod = MethodType.methodType(double.class, Object.class);
      MethodType implType = MethodType.methodType(float.class, ownerType);
      CallSite cs = LambdaMetafactory.metafactory(ROOT_LOOKUP, "applyAsDouble", invokedType, samMethod, accessor, implType);
      return (ToDoubleFunction)cs.getTarget().invoke();
   }

   @NotNull
   private static Function<?, ?> buildReference(@NotNull Class<?> ownerType, @NotNull MethodHandle accessor, @NotNull Class<?> returnType) throws Throwable {
      MethodType invokedType = MethodType.methodType(Function.class);
      MethodType samMethod = MethodType.methodType(Object.class, Object.class);
      MethodType implType = MethodType.methodType(returnType, ownerType);
      CallSite cs = LambdaMetafactory.metafactory(ROOT_LOOKUP, "apply", invokedType, samMethod, accessor, implType);
      return (Function)cs.getTarget().invoke();
   }
}
