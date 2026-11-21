package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;

public abstract class ClassIntrospector {
   protected ClassIntrospector() {
   }

   public abstract ClassIntrospector copy();

   public abstract BeanDescription forSerialization(SerializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract BeanDescription forDeserialization(DeserializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract BeanDescription forDeserializationWithBuilder(
      DeserializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3, BeanDescription var4
   );

   @Deprecated
   public abstract BeanDescription forDeserializationWithBuilder(DeserializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract BeanDescription forCreation(DeserializationConfig var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract BeanDescription forClassAnnotations(MapperConfig<?> var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public abstract BeanDescription forDirectClassAnnotations(MapperConfig<?> var1, JavaType var2, ClassIntrospector.MixInResolver var3);

   public interface MixInResolver {
      Class<?> findMixInClassFor(Class<?> var1);

      ClassIntrospector.MixInResolver copy();
   }
}
