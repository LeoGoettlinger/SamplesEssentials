package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect;

import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Version;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.AnnotationIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.PackageVersion;

public abstract class NopAnnotationIntrospector extends AnnotationIntrospector implements Serializable {
   private static final long serialVersionUID = 1L;
   public static final NopAnnotationIntrospector instance = new NopAnnotationIntrospector() {
      private static final long serialVersionUID = 1L;

      @Override
      public Version version() {
         return PackageVersion.VERSION;
      }
   };

   @Override
   public Version version() {
      return Version.unknownVersion();
   }
}
