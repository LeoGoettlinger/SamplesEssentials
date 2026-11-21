package kotlin.jvm.internal

import java.lang.reflect.GenericDeclaration

public interface KotlinGenericDeclaration {
   public abstract fun findJavaDeclaration(): GenericDeclaration? {
   }
}
