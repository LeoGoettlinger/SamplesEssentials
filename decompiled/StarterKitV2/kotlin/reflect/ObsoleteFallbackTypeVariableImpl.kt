package kotlin.reflect

import java.lang.reflect.GenericDeclaration
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.util.ArrayList
import kotlin.jvm.internal.SourceDebugExtension

@ExperimentalStdlibApi
@SourceDebugExtension(["SMAP\nTypesJVM.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TypesJVM.kt\nkotlin/reflect/ObsoleteFallbackTypeVariableImpl\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 ArraysJVM.kt\nkotlin/collections/ArraysKt__ArraysJVMKt\n*L\n1#1,244:1\n1586#2:245\n1661#2,3:246\n37#3,2:249\n*S KotlinDebug\n*F\n+ 1 TypesJVM.kt\nkotlin/reflect/ObsoleteFallbackTypeVariableImpl\n*L\n130#1:245\n130#1:246,3\n130#1:249,2\n*E\n"])
private class ObsoleteFallbackTypeVariableImpl(typeParameter: KTypeParameter) : TypeVariable<GenericDeclaration>, TypeImpl {
   private final val typeParameter: KTypeParameter

   init {
      this.typeParameter = typeParameter;
   }

   public override fun getName(): String {
      return this.typeParameter.getName();
   }

   public override fun getGenericDeclaration(): GenericDeclaration {
      throw new UnsupportedOperationException(
         "getGenericDeclaration() is not supported for type variables created from KType: ${this.typeParameter}.\nUpdate kotlin-reflect dependency to 2.3.20+."
      );
   }

   public override fun getBounds(): Array<Type> {
      val `$this$toTypedArray$iv`: java.lang.Iterable = this.typeParameter.getUpperBounds();
      val `destination$iv$iv`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$toTypedArray$iv`, 10));

      for (Object item$iv$iv : $this$map$iv) {
         `destination$iv$iv`.add(TypesJVMKt.access$computeJavaType(`item$iv$iv` as KType, true));
      }

      return (`destination$iv$iv` as java.util.List).toArray(new Type[0]);
   }

   public override fun getTypeName(): String {
      return this.getName();
   }

   public override operator fun equals(other: Any?): Boolean {
      return other is TypeVariable
         && this.getName() == (other as TypeVariable).getName()
         && this.getGenericDeclaration() == (other as TypeVariable).getGenericDeclaration();
   }

   public override fun hashCode(): Int {
      return this.getName().hashCode() xor this.getGenericDeclaration().hashCode();
   }

   public override fun toString(): String {
      return this.getTypeName();
   }

   public override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
      return null;
   }

   public override fun getAnnotations(): Array<Annotation> {
      return new java.lang.annotation.Annotation[0];
   }

   public override fun getDeclaredAnnotations(): Array<Annotation> {
      return new java.lang.annotation.Annotation[0];
   }
}
