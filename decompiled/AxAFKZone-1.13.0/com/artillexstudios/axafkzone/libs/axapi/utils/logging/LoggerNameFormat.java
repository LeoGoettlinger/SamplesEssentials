package com.artillexstudios.axafkzone.libs.axapi.utils.logging;

import com.artillexstudios.axafkzone.libs.axapi.utils.Nameable;

public enum LoggerNameFormat {
   FULL {
      @Override
      public String getName(Class<?> clazz) {
         return clazz.getName();
      }
   },
   COMPACT {
      @Override
      public String getName(Class<?> clazz) {
         return clazz.getSimpleName();
      }
   },
   NAMEABLE {
      @Override
      public String getName(Class<?> clazz) {
         return Nameable.getInstance().getName();
      }
   };

   public abstract String getName(Class<?> clazz);
}
