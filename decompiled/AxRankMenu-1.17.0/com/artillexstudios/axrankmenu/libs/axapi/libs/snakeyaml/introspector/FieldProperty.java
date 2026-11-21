package com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.introspector;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.YAMLException;
import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.util.ArrayUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

public class FieldProperty extends GenericProperty {
   private final Field field;

   public FieldProperty(Field field) {
      super(field.getName(), field.getType(), field.getGenericType());
      this.field = field;
      field.setAccessible(true);
   }

   @Override
   public void set(Object object, Object value) throws Exception {
      this.field.set(object, value);
   }

   @Override
   public Object get(Object object) {
      try {
         return this.field.get(object);
      } catch (Exception var3) {
         throw new YAMLException("Unable to access field " + this.field.getName() + " on object " + object + " : " + var3);
      }
   }

   @Override
   public List<Annotation> getAnnotations() {
      return ArrayUtils.toUnmodifiableList(this.field.getAnnotations());
   }

   @Override
   public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return this.field.getAnnotation(annotationType);
   }
}
