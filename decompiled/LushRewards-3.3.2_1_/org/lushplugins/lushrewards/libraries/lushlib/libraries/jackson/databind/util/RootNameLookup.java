package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util;

import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.AnnotationIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ClassKey;

public class RootNameLookup implements Serializable {
   private static final long serialVersionUID = 1L;
   protected transient LRUMap<ClassKey, PropertyName> _rootNames = new LRUMap<>(20, 200);

   public PropertyName findRootName(JavaType rootType, MapperConfig<?> config) {
      return this.findRootName(rootType.getRawClass(), config);
   }

   public PropertyName findRootName(Class<?> rootType, MapperConfig<?> config) {
      ClassKey key = new ClassKey(rootType);
      PropertyName name = this._rootNames.get(key);
      if (name != null) {
         return name;
      } else {
         BeanDescription beanDesc = config.introspectClassAnnotations(rootType);
         AnnotationIntrospector intr = config.getAnnotationIntrospector();
         AnnotatedClass ac = beanDesc.getClassInfo();
         name = intr.findRootName(ac);
         if (name == null || !name.hasSimpleName()) {
            name = PropertyName.construct(rootType.getSimpleName());
         }

         this._rootNames.put(key, name);
         return name;
      }
   }

   protected Object readResolve() {
      return new RootNameLookup();
   }
}
