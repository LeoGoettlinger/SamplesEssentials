package kotlin.jvm.internal

import java.lang.reflect.GenericDeclaration
import java.lang.reflect.Method
import kotlin.reflect.KDeclarationContainer

public fun KDeclarationContainer?.findMethodBySignature(signature: String): GenericDeclaration? {
   if (`$this$findMethodBySignature` !is ClassBasedDeclarationContainer) {
      return null;
   } else {
      val jvmName: java.lang.String = StringsKt.substringBefore$default(signature, '(', null, 2, null);
      if (jvmName == "<init>") {
         throw new UnsupportedOperationException("Generic Java constructors are not supported: $`$this$findMethodBySignature`/$signature");
      } else {
         var var10000: Array<Method> = (`$this$findMethodBySignature` as ClassBasedDeclarationContainer).getJClass().getDeclaredMethods();
         val var3: Array<Any> = var10000;
         var var4: Int = 0;
         val var5: Int = var3.length;

         while (true) {
            if (var4 >= var5) {
               var10000 = null;
               break;
            }

            var var6: Any;
            label30: {
               var6 = var3[var4];
               val it: Method = var3[var4] as Method;
               if ((var3[var4] as Method).getName() == jvmName) {
                  if (computeMethodSignature(it) == signature) {
                     var9 = true;
                     break label30;
                  }
               }

               var9 = false;
            }

            if (var9) {
               var10000 = (Method[])var6;
               break;
            }

            var4++;
         }

         return var10000 as GenericDeclaration;
      }
   }
}

private fun Method.computeMethodSignature(): String {
   val var1: StringBuilder = new StringBuilder();
   val `$this$computeMethodSignature_u24lambda_u240`: StringBuilder = var1;
   var1.append(`$this$computeMethodSignature`.getName());
   var1.append("(");
   val var10000: Array<Class> = `$this$computeMethodSignature`.getParameterTypes();

   for (Class type : var10000) {
      val var8: Appendable = `$this$computeMethodSignature_u24lambda_u240`;
      appendClass(var8, type);
   }

   `$this$computeMethodSignature_u24lambda_u240`.append(")");
   val var9: Appendable = `$this$computeMethodSignature_u24lambda_u240`;
   val var10001: Class = `$this$computeMethodSignature`.getReturnType();
   appendClass(var9, var10001);
   return var1.toString();
}

private fun Appendable.appendClass(start: Class<*>) {
   var klass: Class = start;

   while (klass.isArray()) {
      `$this$appendClass`.append("[");
      val var10000: Class = klass.getComponentType();
      klass = var10000;
   }

   if (klass == Void::class.javaPrimitiveType) {
      `$this$appendClass`.append("V");
   } else if (klass == Int::class.javaPrimitiveType) {
      `$this$appendClass`.append("I");
   } else if (klass == java.lang.Long::class.javaPrimitiveType) {
      `$this$appendClass`.append("J");
   } else if (klass == java.lang.Short::class.javaPrimitiveType) {
      `$this$appendClass`.append("S");
   } else if (klass == java.lang.Byte::class.javaPrimitiveType) {
      `$this$appendClass`.append("B");
   } else if (klass == java.lang.Boolean::class.javaPrimitiveType) {
      `$this$appendClass`.append("Z");
   } else if (klass == Character::class.javaPrimitiveType) {
      `$this$appendClass`.append("C");
   } else if (klass == java.lang.Float::class.javaPrimitiveType) {
      `$this$appendClass`.append("F");
   } else if (klass == java.lang.Double::class.javaPrimitiveType) {
      `$this$appendClass`.append("D");
   } else {
      `$this$appendClass`.append("L");
      val var10001: java.lang.String = klass.getName();
      `$this$appendClass`.append(StringsKt.replace$default(var10001, '.', '/', false, 4, null));
      `$this$appendClass`.append(";");
   }
}
