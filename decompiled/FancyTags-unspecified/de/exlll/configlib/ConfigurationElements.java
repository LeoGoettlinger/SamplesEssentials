package de.exlll.configlib;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;

final class ConfigurationElements {
   private ConfigurationElements() {
   }

   record FieldElement(Field element) implements ConfigurationElement<Field> {
      @Override
      public String name() {
         return this.element.getName();
      }

      @Override
      public Class<?> type() {
         return this.element.getType();
      }

      @Override
      public AnnotatedType annotatedType() {
         return this.element.getAnnotatedType();
      }

      @Override
      public Object value(Object elementHolder) {
         return Reflect.getValue(this.element, elementHolder);
      }

      @Override
      public Class<?> declaringType() {
         return this.element.getDeclaringClass();
      }
   }

   record RecordComponentElement(RecordComponent element) implements ConfigurationElement<RecordComponent> {
      @Override
      public String name() {
         return this.element.getName();
      }

      @Override
      public Class<?> type() {
         return this.element.getType();
      }

      @Override
      public AnnotatedType annotatedType() {
         return this.element.getAnnotatedType();
      }

      @Override
      public Object value(Object elementHolder) {
         return Reflect.getValue(this.element, elementHolder);
      }

      @Override
      public Class<?> declaringType() {
         return this.element.getDeclaringRecord();
      }
   }
}
