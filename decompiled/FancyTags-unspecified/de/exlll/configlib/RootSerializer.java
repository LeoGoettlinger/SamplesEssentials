package de.exlll.configlib;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;

final class RootSerializer<T> implements Serializer<T, Map<?, ?>> {
   private final TypeSerializer<T, ?> serializer;
   private final ConfigurationProperties properties;
   private final Environment environment;

   RootSerializer(Class<T> type, ConfigurationProperties properties, Environment environment) {
      Validator.requireConfigurationType(type);
      this.properties = Validator.requireNonNull(properties, "configuration properties");
      this.environment = Validator.requireNonNull(environment, "environment");
      this.serializer = TypeSerializer.newSerializerFor(type, properties);
   }

   public Map<?, ?> serialize(T rootConfiguration) {
      return this.serializer.serialize(rootConfiguration);
   }

   public T deserialize(Map<?, ?> serializedConfiguration) {
      RootSerializer.EnvironmentVariablePreprocessor envVarPreprocessor = new RootSerializer.EnvironmentVariablePreprocessor(
         this.environment, this.properties.getEnvVarResolutionConfiguration()
      );
      envVarPreprocessor.preprocess(serializedConfiguration);
      return this.serializer.deserialize(serializedConfiguration);
   }

   T newDefaultInstance() {
      return this.serializer.newDefaultInstance();
   }

   private static final class EnvironmentVariablePreprocessor {
      private final Environment environment;
      private final ConfigurationProperties.EnvVarResolutionConfiguration envVarConfig;

      private EnvironmentVariablePreprocessor(Environment environment, ConfigurationProperties.EnvVarResolutionConfiguration envVarConfig) {
         this.environment = environment;
         this.envVarConfig = envVarConfig;
      }

      public void preprocess(Map<?, ?> serializedConfig) {
         if (this.envVarConfig.resolveEnvVars()) {
            String initialPrefix = this.envVarConfig.prefix();
            this.preprocessEnvVarMap(initialPrefix, (Map<Object, Object>)serializedConfig);
         }
      }

      private void preprocessEnvVarMap(String prefix, Map<Object, Object> map) {
         for (Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (key != null) {
               Object val = entry.getValue();
               String envVar = prefix.isEmpty() ? key.toString() : prefix + "_" + key;
               envVar = this.envVarConfig.caseSensitive() ? envVar : envVar.toUpperCase();
               String envVarVal = this.environment.getValue(envVar);
               this.preprocessRecursively(val, envVar, envVarVal, entry::setValue);
            }
         }
      }

      private void preprocessEnvVarList(String prefix, List<Object> list) {
         for (int i = 0; i < list.size(); i++) {
            Object val = list.get(i);
            String envVar = prefix.isEmpty() ? String.valueOf(i) : prefix + "_" + i;
            envVar = this.envVarConfig.caseSensitive() ? envVar : envVar.toUpperCase();
            String envVarVal = this.environment.getValue(envVar);
            int index = i;
            this.preprocessRecursively(val, envVar, envVarVal, o -> list.set(index, o));
         }
      }

      private void preprocessRecursively(Object value, String envVar, String envVarVal, Consumer<Object> updater) {
         if (value instanceof Map<?, ?> map) {
            requireEnvVarNull(envVar, envVarVal, "map");
            this.preprocessEnvVarMap(envVar, (Map<Object, Object>)map);
         } else if (value instanceof List<?> list) {
            requireEnvVarNull(envVar, envVarVal, "list");
            this.preprocessEnvVarList(envVar, (List<Object>)list);
         } else if (value instanceof Set) {
            requireEnvVarNull(envVar, envVarVal, "set");
         } else if (envVarVal != null) {
            Object updatedValue = tryConvertEnvVarValueToTypeOfCurrentValue(envVar, envVarVal, value);
            updater.accept(updatedValue);
         }
      }

      private static void requireEnvVarNull(String envVar, String envVarVal, String collectionType) {
         if (envVarVal != null) {
            String msg = "Environment variables cannot be used to replace collections, but environment variable '%s' with value '%s' tries to replace a %s."
               .formatted(envVar, envVarVal, collectionType);
            throw new ConfigurationException(msg);
         }
      }

      private static Object tryConvertEnvVarValueToTypeOfCurrentValue(String envVar, String envVarValue, Object currentValue) {
         if (currentValue == null) {
            String msg = "Value '%s' of environment variable '%s' cannot be converted because the value that is supposed to be replaced is null."
               .formatted(envVarValue, envVar);
            throw new ConfigurationException(msg);
         } else if (currentValue instanceof String) {
            return envVarValue;
         } else {
            try {
               if (currentValue instanceof Boolean) {
                  return parseBoolean(envVarValue);
               }

               if (currentValue instanceof Long) {
                  return Long.valueOf(envVarValue);
               }

               if (currentValue instanceof Double) {
                  return Double.valueOf(envVarValue);
               }
            } catch (RuntimeException var6) {
               String simpleName = currentValue.getClass().getSimpleName();
               String msg = "Value '%s' of environment variable '%s' cannot be converted to type '%s'. The value the environment variable is supposed to replace is '%s'."
                  .formatted(envVarValue, envVar, simpleName, currentValue);
               throw new ConfigurationException(msg, var6);
            }

            String msg = "Unexpected value '%s' of type '%s'".formatted(currentValue, currentValue.getClass());
            throw new ConfigurationException(msg);
         }
      }

      private static Boolean parseBoolean(String val) {
         if (!"true".equalsIgnoreCase(val) && !"false".equalsIgnoreCase(val)) {
            String msg = "The string '" + val + "' is not a valid boolean value. Only the values 'true' and 'false' (ignoring their case) are supported.";
            throw new RuntimeException(msg);
         } else {
            return Boolean.valueOf(val);
         }
      }
   }
}
