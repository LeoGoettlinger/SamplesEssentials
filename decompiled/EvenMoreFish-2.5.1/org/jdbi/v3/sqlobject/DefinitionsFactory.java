package org.jdbi.v3.sqlobject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jdbi.v3.core.extension.ExtensionMetadata;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.sqlobject.customizer.Definition;

class DefinitionsFactory {
   private final Map<String, Object> definitions = new HashMap<>();

   private DefinitionsFactory() {
   }

   static void configureDefinitions(Class<?> extensionType, ExtensionMetadata.Builder builder) {
      new DefinitionsFactory().configure(extensionType, builder);
   }

   void configure(Class<?> extensionType, ExtensionMetadata.Builder builder) {
      this.configureTypeDefinitions(extensionType);
      if (!this.definitions.isEmpty()) {
         builder.addInstanceConfigCustomizer(config -> config.get(SqlStatements.class).defineMap(this.definitions));
      }
   }

   private void configureTypeDefinitions(Class<?> type) {
      Arrays.asList(type.getInterfaces()).forEach(this::configureTypeDefinitions);
      Optional.ofNullable(type.getSuperclass()).ifPresent(this::configureTypeDefinitions);

      for (Definition typeDefinition : type.getAnnotationsByType(Definition.class)) {
         if (notDefined(typeDefinition.key())) {
            throw new UnableToCreateSqlObjectException(String.format("Type level @Definition on %s must have specific key", type));
         }

         if (notDefined(typeDefinition.value())) {
            throw new UnableToCreateSqlObjectException(String.format("Type level @Definition on %s must have specific value", type));
         }

         this.definitions.put(typeDefinition.key(), typeDefinition.value());
      }

      this.configureFieldDefinitions(type);
      this.configureMethodDefinitions(type);
   }

   private void configureFieldDefinitions(Class<?> type) {
      for (Field field : type.getDeclaredFields()) {
         for (Definition fieldDefinition : field.getAnnotationsByType(Definition.class)) {
            if (defined(fieldDefinition.value())) {
               throw new UnableToCreateSqlObjectException(String.format("Field %s @Definition on %s may not specify value", field.getName(), type));
            }

            try {
               this.definitions.put(defaultValue(fieldDefinition.key(), field.getName()), field.get(null));
            } catch (IllegalAccessException | IllegalArgumentException var11) {
               throw new UnableToCreateSqlObjectException(String.format("Could not read field %s of %s", field.getName(), type), var11);
            }
         }
      }
   }

   private void configureMethodDefinitions(Class<?> type) {
      for (Method method : type.getDeclaredMethods()) {
         for (Definition methodDefinition : method.getAnnotationsByType(Definition.class)) {
            if (method.getParameterCount() > 0) {
               throw new UnableToCreateSqlObjectException(
                  String.format("@Definition annotated method %s may not have any parameters (on %s)", method.getName(), type)
               );
            }

            if (defined(methodDefinition.value())) {
               throw new UnableToCreateSqlObjectException(String.format("Method %s @Definition on %s may not specify value", method.getName(), type));
            }

            try {
               this.definitions.put(defaultValue(methodDefinition.key(), method.getName()), method.invoke(null));
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException var11) {
               throw new UnableToCreateSqlObjectException(String.format("Could not invoke method %s of %s", method.getName(), type), var11);
            }
         }
      }
   }

   private static boolean defined(String v) {
      return !notDefined(v);
   }

   private static boolean notDefined(String v) {
      return "__definition_undefined__".equals(v);
   }

   private static String defaultValue(String value, String dfl) {
      return notDefined(value) ? dfl : value;
   }
}
