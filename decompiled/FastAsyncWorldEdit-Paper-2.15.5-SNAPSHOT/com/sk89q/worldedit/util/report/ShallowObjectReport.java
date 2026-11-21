package com.sk89q.worldedit.util.report;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.apache.logging.log4j.Logger;

public class ShallowObjectReport extends DataReport {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   public ShallowObjectReport(String title, Object object) {
      super(title);
      Preconditions.checkNotNull(object, "object");
      this.scanClass(object, object.getClass());
   }

   void scanClass(Object object, Class<?> type) {
      for (Field field : type.getDeclaredFields()) {
         if (!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(Unreported.class) == null) {
            field.setAccessible(true);

            try {
               Object value = field.get(object);
               this.append(field.getName(), String.valueOf(value));
            } catch (IllegalAccessException var8) {
               LOGGER.warn("Failed to get value of '" + field.getName() + "' on " + type);
            }
         }
      }
   }
}
