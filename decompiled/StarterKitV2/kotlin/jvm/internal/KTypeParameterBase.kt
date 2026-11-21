package kotlin.jvm.internal

import java.lang.reflect.GenericDeclaration
import kotlin.reflect.KTypeParameter

public abstract class KTypeParameterBase : KTypeParameter {
   internal final val container: Any

   internal final val javaContainingDeclaration: GenericDeclaration?
      internal final get() {
         return this.javaContainingDeclaration$delegate.getValue() as GenericDeclaration;
      }


   open fun KTypeParameterBase(container: Any) {
      this.container = container;
      this.javaContainingDeclaration$delegate = LazyKt.lazy(LazyThreadSafetyMode.PUBLICATION, KTypeParameterBase::javaContainingDeclaration_delegate$lambda$0);
   }

   public override operator fun equals(other: Any?): Boolean {
      return other is KTypeParameterBase
         && this.getName() == (other as KTypeParameterBase).getName()
         && this.container == (other as KTypeParameterBase).container;
   }

   public override fun hashCode(): Int {
      return this.container.hashCode() * 31 + this.getName().hashCode();
   }

   public override fun toString(): String {
      return TypeParameterReference.Companion.toString(this);
   }

   @JvmStatic
   fun `javaContainingDeclaration_delegate$lambda$0`(`this$0`: KTypeParameterBase): GenericDeclaration {
      val var1: Any = `this$0`.container;
      return if ((`this$0`.container as? KotlinGenericDeclaration) != null) (`this$0`.container as? KotlinGenericDeclaration).findJavaDeclaration() else null;
   }
}
