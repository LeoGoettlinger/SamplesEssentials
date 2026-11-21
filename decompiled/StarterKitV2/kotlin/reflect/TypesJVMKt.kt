package kotlin.reflect

import java.lang.reflect.GenericDeclaration
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.util.ArrayList
import java.util.NoSuchElementException
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.jvm.internal.KTypeBase
import kotlin.jvm.internal.KTypeParameterBase

@SinceKotlin(
   version = "1.4"
)
@ExperimentalStdlibApi
@LowPriorityInOverloadResolution
public final val javaType: Type
   public final get() {
      if (`$this$javaType` is KTypeBase) {
         val var1: Type = (`$this$javaType` as KTypeBase).getJavaType();
         if (var1 != null) {
            return var1;
         }
      }

      return computeJavaType$default(`$this$javaType`, false, 1, null);
   }


@ExperimentalStdlibApi
private final val javaType: Type
   private final get() {
      val var10000: KVariance = `$this$javaType`.getVariance();
      if (var10000 == null) {
         return WildcardTypeImpl.Companion.getSTAR();
      } else {
         val var3: KType = `$this$javaType`.getType();
         var var4: Type;
         switch (TypesJVMKt.WhenMappings.$EnumSwitchMapping$0[var10000.ordinal()]) {
            case 1:
               var4 = new WildcardTypeImpl(null, computeJavaType(var3, true));
               break;
            case 2:
               var4 = computeJavaType(var3, true);
               break;
            case 3:
               var4 = new WildcardTypeImpl(computeJavaType(var3, true), null);
               break;
            default:
               throw new NoWhenBranchMatchedException();
         }

         return var4;
      }
   }


@ExperimentalStdlibApi
private fun KType.computeJavaType(forceWrapper: Boolean = false): Type {
   val classifier: KClassifier = `$this$computeJavaType`.getClassifier();
   if (classifier is KTypeParameter) {
      if (classifier !is KTypeParameterBase) {
         return new ObsoleteFallbackTypeVariableImpl(classifier as KTypeParameter);
      } else {
         val var20: GenericDeclaration = (classifier as KTypeParameterBase).getJavaContainingDeclaration$kotlin_stdlib();
         if (var20 == null) {
            throw new UnsupportedOperationException("javaType is not supported for this type: $`$this$computeJavaType`");
         } else {
            val var21: Array<TypeVariable> = var20.getTypeParameters();
            val var15: Array<Any> = var21;
            var var17: Any = null;
            var `found$iv`: Boolean = false;

            for (Object element$iv : $this$single$iv) {
               if ((`element$iv` as TypeVariable).getName() == (classifier as KTypeParameterBase).getName()) {
                  if (`found$iv`) {
                     throw new IllegalArgumentException("Array contains more than one matching element.");
                  }

                  var17 = `element$iv`;
                  `found$iv` = true;
               }
            }

            if (!`found$iv`) {
               throw new NoSuchElementException("Array contains no element matching the predicate.");
            } else {
               return var17 as Type;
            }
         }
      }
   } else if (classifier is KClass) {
      val jClass: Class = if (forceWrapper) JvmClassMappingKt.getJavaObjectType(classifier as KClass) else JvmClassMappingKt.getJavaClass(classifier as KClass);
      val arguments: java.util.List = `$this$computeJavaType`.getArguments();
      if (arguments.isEmpty()) {
         return jClass;
      } else if (jClass.isArray()) {
         if (jClass.getComponentType().isPrimitive()) {
            return jClass;
         } else {
            val var10000: KTypeProjection = CollectionsKt.singleOrNull(arguments);
            if (var10000 == null) {
               throw new IllegalArgumentException("kotlin.Array must have exactly one type argument: $`$this$computeJavaType`");
            } else {
               val variance: KVariance = var10000.component1();
               val elementType: KType = var10000.component2();
               var var19: Type;
               switch (variance == null ? -1 : TypesJVMKt.WhenMappings.$EnumSwitchMapping$0[variance.ordinal()]) {
                  case -1:
                  case 1:
                     var19 = jClass;
                     break;
                  case 0:
                  default:
                     throw new NoWhenBranchMatchedException();
                  case 2:
                  case 3:
                     val javaElementType: Type = computeJavaType$default(elementType, false, 1, null);
                     var19 = if (javaElementType is Class) jClass else new GenericArrayTypeImpl(javaElementType);
               }

               return var19;
            }
         }
      } else {
         return createPossiblyInnerType(jClass, arguments);
      }
   } else {
      throw new UnsupportedOperationException("Unsupported type classifier: $`$this$computeJavaType`");
   }
}

@JvmSynthetic
fun `computeJavaType$default`(var0: KType, var1: Boolean, var2: Int, var3: Any): Type {
   if ((var2 and 1) != 0) {
      var1 = false;
   }

   return computeJavaType(var0, var1);
}

@ExperimentalStdlibApi
private fun createPossiblyInnerType(jClass: Class<*>, arguments: List<KTypeProjection>): Type {
   val var10000: Class = jClass.getDeclaringClass();
   if (var10000 == null) {
      val var27: java.lang.Iterable = arguments;
      val var30: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(arguments, 10));

      for (Object item$iv$iv : var27) {
         var30.add(getJavaType(var36 as KTypeProjection));
      }

      return new ParameterizedTypeImpl(jClass, null, var30 as MutableList<Type>);
   } else if (Modifier.isStatic(jClass.getModifiers())) {
      val var44: Type = var10000;
      val var25: java.lang.Iterable = arguments;
      val `$this$mapTo$iv$iv`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(arguments, 10));

      for (Object item$iv$iv : var25) {
         `$this$mapTo$iv$iv`.add(getJavaType(var33 as KTypeProjection));
      }

      return new ParameterizedTypeImpl(jClass, var44, `$this$mapTo$iv$iv` as MutableList<Type>);
   } else {
      val n: Int = jClass.getTypeParameters().length;
      val var10001: Type = createPossiblyInnerType(var10000, arguments.subList(n, arguments.size()));
      val `$this$map$iv`: java.lang.Iterable = arguments.subList(0, n);
      val `destination$iv$iv`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$map$iv`, 10));

      for (Object item$iv$iv : $this$map$iv) {
         `destination$iv$iv`.add(getJavaType(`item$iv$iv` as KTypeProjection));
      }

      return new ParameterizedTypeImpl(jClass, var10001, `destination$iv$iv` as MutableList<Type>);
   }
}

private fun typeToString(type: Type): String {
   val var10000: java.lang.String;
   if (type is Class) {
      if ((type as Class).isArray()) {
         val unwrap: Sequence = SequencesKt.generateSequence(type, <unrepresentable>.INSTANCE);
         var10000 = "${SequencesKt.<Class>last(unwrap).getName()}${StringsKt.repeat("[]", SequencesKt.count(unwrap))}";
      } else {
         var10000 = (type as Class).getName();
      }
   } else {
      var10000 = type.toString();
   }

   return var10000;
}

@JvmSynthetic
fun `access$computeJavaType`(`$receiver`: KType, forceWrapper: Boolean): Type {
   return computeJavaType(`$receiver`, forceWrapper);
}

@JvmSynthetic
fun `access$typeToString`(type: Type): java.lang.String {
   return typeToString(type);
}
// $VF: Class flags could not be determined
@JvmSynthetic
internal class WhenMappings {
   @JvmStatic
   fun {
      val var0: IntArray = new int[KVariance.values().length];

      try {
         var0[KVariance.IN.ordinal()] = 1;
      } catch (var4: NoSuchFieldError) {
      }

      try {
         var0[KVariance.INVARIANT.ordinal()] = 2;
      } catch (var3: NoSuchFieldError) {
      }

      try {
         var0[KVariance.OUT.ordinal()] = 3;
      } catch (var2: NoSuchFieldError) {
      }

      $EnumSwitchMapping$0 = var0;
   }
}
