package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ext;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.Annotated;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedParameter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ExceptionUtil;

public abstract class Java7Support {
   private static final Java7Support IMPL;

   public static Java7Support instance() {
      return IMPL;
   }

   public abstract Boolean findTransient(Annotated var1);

   public abstract Boolean hasCreatorAnnotation(Annotated var1);

   public abstract PropertyName findConstructorName(AnnotatedParameter var1);

   static {
      Java7Support impl = null;

      try {
         Class<?> cls = Class.forName("org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ext.Java7SupportImpl");
         impl = ClassUtil.createInstance((Class<Java7Support>)cls, false);
      } catch (Throwable var2) {
         ExceptionUtil.rethrowIfFatal(var2);
      }

      IMPL = impl;
   }
}
