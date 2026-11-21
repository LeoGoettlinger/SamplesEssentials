package ac.grim.grimac.shaded.configuralize;

import ac.grim.grimac.shaded.configuralize.mapping.MappingFunction;
import ac.grim.grimac.shaded.configuralize.mapping.Option;
import ac.grim.grimac.shaded.json.simple.parser.JSONParser;
import ac.grim.grimac.shaded.maps.weak.Dynamic;
import ac.grim.grimac.shaded.maps.weak.Weak;
import ac.grim.grimac.shaded.snakeyaml.Yaml;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class DynamicConfig {
   private final Map<Source, Provider> sources = new LinkedHashMap<>();
   private final Map<String, Object> runtimeValues = new HashMap<>();
   private Language language;
   private JSONParser jsonParser = null;
   private Yaml yamlParser = null;

   public DynamicConfig() {
      this(Language.EN);
   }

   public DynamicConfig(Language language) {
      this.language = language;
   }

   public boolean isLanguageAvailable() {
      return this.isLanguageAvailable(this.language);
   }

   public boolean isLanguageAvailable(Language language) {
      return this.sources.keySet().stream().allMatch(source -> source.isLanguageAvailable(language));
   }

   public boolean addSource(Source source) {
      return this.sources.put(source, new Provider(this, source)) == null;
   }

   public boolean addSource(Class<?> clazz, String resource, File file) {
      Source source = new Source(this, clazz, resource, file);
      return this.sources.put(source, new Provider(this, source)) == null;
   }

   public boolean removeSource(Source source) {
      return this.sources.remove(source) != null;
   }

   public void saveAllDefaults() throws IOException {
      this.saveAllDefaults(false);
   }

   public void saveAllDefaults(boolean overwrite) throws IOException {
      for (Entry<Source, Provider> source : this.sources.entrySet()) {
         source.getValue().saveDefaults(overwrite);
      }
   }

   public void loadAll() throws IOException, ParseException {
      for (Entry<Source, Provider> source : this.sources.entrySet()) {
         source.getValue().load();
      }
   }

   public void map(MappingFunction<?>... mappingFunctions) {
      this.map(this.getClass(), mappingFunctions);
   }

   public void map(List<MappingFunction<?>> mappingFunctions) {
      this.map(this.getClass(), mappingFunctions);
   }

   public void map(Class<?> targetClass, MappingFunction<?>... mappings) {
      this.map(targetClass, Arrays.asList(mappings));
   }

   public void map(Class<?> targetClass, List<MappingFunction<?>> mappings) {
      this.mapFields(targetClass, mappings);

      for (Class<?> declared : targetClass.getDeclaredClasses()) {
         this.map(declared, mappings);
      }
   }

   private void mapFields(Class<?> clazz, List<MappingFunction<?>> mappings) {
      for (Field field : clazz.getDeclaredFields()) {
         if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Option.class)) {
            Option fieldAnnotation = field.getAnnotation(Option.class);
            String key = fieldAnnotation.key();
            if (!field.isAccessible()) {
               field.setAccessible(true);
            }

            boolean valueExists = Modifier.isFinal(field.getModifiers());
            if (valueExists) {
               try {
                  Field modifiersField = Field.class.getDeclaredField("modifiers");
                  modifiersField.setAccessible(true);
                  modifiersField.setInt(field, field.getModifiers() & -17);
               } catch (NoSuchFieldException | IllegalAccessException var15) {
                  throw new RuntimeException("Failed to reflectively set field " + field + " to non-final", var15);
               }
            }

            try {
               Dynamic dynamic = this.dgetSilent(key);
               MappingFunction<?> mappingFunction = mappings != null ? mappings.stream().filter(mf -> mf.getKey().equals(key)).findFirst().orElse(null) : null;
               Object value;
               if (mappingFunction != null) {
                  value = mappingFunction.getFunction().apply(dynamic);
               } else {
                  String var13 = field.getType().getName().toLowerCase();
                  switch (var13) {
                     case "int":
                     case "integer":
                        value = dynamic.convert().intoInteger();
                        break;
                     case "double":
                        value = dynamic.convert().intoDouble();
                        break;
                     default:
                        value = dynamic.asObject();
                  }
               }

               field.set(null, value);
            } catch (IllegalAccessException var16) {
               throw new RuntimeException("Field " + field + " is not accessible");
            } catch (Throwable var17) {
               throw new RuntimeException("Failed to map key " + key, var17);
            }
         }
      }
   }

   public Dynamic dget(String key) throws IllegalArgumentException {
      return this.runtimeValues.containsKey(key)
         ? Dynamic.from(this.runtimeValues.get(key))
         : this.sources
            .values()
            .stream()
            .filter(Objects::nonNull)
            .filter(provider -> provider.getValues() != null)
            .map(provider -> provider.getValues().dget(key))
            .filter(Objects::nonNull)
            .filter(Weak::isPresent)
            .findFirst()
            .orElseGet(
               () -> this.sources
                  .values()
                  .stream()
                  .map(provider -> provider.getDefaults().dget(key))
                  .filter(Weak::isPresent)
                  .findFirst()
                  .orElseThrow(() -> new IllegalArgumentException("Invalid key: " + key))
            );
   }

   public Dynamic dgetSilent(String key) {
      try {
         return this.dget(key);
      } catch (IllegalArgumentException var3) {
         return Dynamic.from(null);
      }
   }

   public <T> T get(String key) throws RuntimeException {
      return (T)this.dget(key).asObject();
   }

   public <T> Optional<T> getOptional(String key) {
      try {
         return Optional.ofNullable(this.get(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public <T> T getElse(String key, T otherwise) {
      try {
         return this.get(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public <K, V> Map<K, V> getMap(String key) throws RuntimeException {
      return this.dget(key).convert().intoMap();
   }

   public <K, V> Optional<Map<K, V>> getOptionalMap(String key) {
      try {
         return Optional.ofNullable(this.getMap(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public <K, V> Map<K, V> getMapElse(String key, Map<K, V> otherwise) {
      try {
         return this.getMap(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public <T> List<T> getList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public <T> Optional<List<T>> getOptionalList(String key) {
      try {
         return Optional.ofNullable(this.getList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public <T> List<T> getListElse(String key, List<T> otherwise) {
      try {
         return this.getList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public String getString(String key) throws RuntimeException {
      return this.dget(key).convert().intoString();
   }

   public Optional<String> getOptionalString(String key) {
      try {
         return Optional.ofNullable(this.getString(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public String getStringElse(String key, String otherwise) {
      try {
         return this.getString(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public List<String> getStringList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public Optional<List<String>> getOptionalStringList(String key) {
      try {
         return Optional.ofNullable(this.getStringList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public List<String> getStringListElse(String key, List<String> otherwise) {
      try {
         return this.getStringList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public boolean getBoolean(String key) throws RuntimeException {
      String value = this.dget(key).convert().intoString();
      String var3 = value.toLowerCase();
      switch (var3) {
         case "true":
         case "yes":
         case "on":
         case "1":
            return true;
         case "false":
         case "no":
         case "off":
         case "0":
            return false;
         default:
            throw new RuntimeException("Can't convert key " + key + " value \"" + value + "\" to boolean");
      }
   }

   public Optional<Boolean> getOptionalBoolean(String key) {
      try {
         return Optional.of(this.getBoolean(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public boolean getBooleanElse(String key, boolean otherwise) {
      try {
         return this.getBoolean(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public List<Boolean> getBooleanList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public Optional<List<Boolean>> getOptionalBooleanList(String key) {
      try {
         return Optional.ofNullable(this.getBooleanList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public List<Boolean> getBooleanListElse(String key, List<Boolean> otherwise) {
      try {
         return this.getBooleanList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public int getInt(String key) throws RuntimeException {
      return this.dget(key).convert().intoInteger();
   }

   public Optional<Integer> getOptionalInt(String key) {
      try {
         return Optional.of(this.getInt(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public int getIntElse(String key, int otherwise) {
      try {
         return this.getInt(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public List<Integer> getIntList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public Optional<List<Integer>> getOptionalIntList(String key) {
      try {
         return Optional.ofNullable(this.getIntList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public List<Integer> getIntListElse(String key, List<Integer> otherwise) {
      try {
         return this.getIntList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public long getLong(String key) throws RuntimeException {
      return this.dget(key).convert().intoLong();
   }

   public Optional<Long> getOptionalLong(String key) {
      try {
         return Optional.of(this.getLong(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public long getLongElse(String key, long otherwise) {
      try {
         return this.getLong(key);
      } catch (Exception var5) {
         return otherwise;
      }
   }

   public List<Long> getLongList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public Optional<List<Long>> getOptionalLongList(String key) {
      try {
         return Optional.ofNullable(this.getLongList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public List<Long> getLongListElse(String key, List<Long> otherwise) {
      try {
         return this.getLongList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public double getDouble(String key) throws RuntimeException {
      return this.dget(key).convert().intoDouble();
   }

   public Optional<Double> getOptionalDouble(String key) {
      try {
         return Optional.of(this.getDouble(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public double getDoubleElse(String key, double otherwise) {
      try {
         return this.getDouble(key);
      } catch (Exception var5) {
         return otherwise;
      }
   }

   public List<Double> getDoubleList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public Optional<List<Double>> getOptionalDoubleList(String key) {
      try {
         return Optional.ofNullable(this.getDoubleList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public List<Double> getDoubleListElse(String key, List<Double> otherwise) {
      try {
         return this.getDoubleList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public BigDecimal getDecimal(String key) throws RuntimeException {
      return this.dget(key).convert().intoDecimal();
   }

   public Optional<BigDecimal> getOptionalDecimal(String key) {
      try {
         return Optional.ofNullable(this.getDecimal(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public BigDecimal getDecimalElse(String key, BigDecimal otherwise) {
      try {
         return this.getDecimal(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public List<BigDecimal> getDecimalList(String key) throws RuntimeException {
      return this.dget(key).convert().intoList();
   }

   public Optional<List<BigDecimal>> getOptionalDecimalList(String key) {
      try {
         return Optional.ofNullable(this.getDecimalList(key));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public List<BigDecimal> getDecimalListElse(String key, List<BigDecimal> otherwise) {
      try {
         return this.getDecimalList(key);
      } catch (Exception var4) {
         return otherwise;
      }
   }

   public <T> T getSilent(String key) {
      try {
         return (T)this.dget(key).asObject();
      } catch (IllegalArgumentException var3) {
         return null;
      }
   }

   public void getSilent(String key, Consumer<Dynamic> success) {
      this.getSilent(key, success, null);
   }

   public void getSilent(String key, Consumer<Dynamic> success, Runnable failure) {
      try {
         Dynamic dynamic = this.dget(key);
         if (success != null) {
            success.accept(dynamic);
         }
      } catch (IllegalArgumentException var5) {
         if (failure != null) {
            failure.run();
         }
      }
   }

   public void setRuntimeValue(String key, Object value) {
      this.runtimeValues.put(key, value);
   }

   JSONParser getJsonParser() {
      return this.jsonParser != null ? this.jsonParser : (this.jsonParser = new JSONParser());
   }

   Yaml getYamlParser() {
      return this.yamlParser != null ? this.yamlParser : (this.yamlParser = new Yaml());
   }

   public Language getLanguage() {
      return this.language;
   }

   public void setLanguage(Language language) {
      this.language = language;
   }

   public Map<Source, Provider> getSources() {
      return this.sources;
   }

   public Provider getProvider(String resource) {
      return this.sources
         .entrySet()
         .stream()
         .filter(entry -> entry.getKey().getResourceName().equals(resource))
         .map(Entry::getValue)
         .findFirst()
         .orElseThrow(() -> new IllegalArgumentException("Invalid resource " + resource));
   }
}
