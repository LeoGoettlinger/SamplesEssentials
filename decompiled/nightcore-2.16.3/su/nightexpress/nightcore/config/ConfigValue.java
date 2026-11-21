package su.nightexpress.nightcore.config;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.TriFunction;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.wrapper.UniFormatter;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

@NullMarked
@Deprecated(
   forRemoval = true
)
public class ConfigValue<T> {
   private final String path;
   private final T defaultValue;
   private final String[] description;
   private final ConfigValue.Loader<T> reader;
   private final ConfigValue.Writer<T> writer;
   @Nullable
   private T value;
   @Nullable
   private UnaryOperator<T> onRead;

   public ConfigValue(String path, ConfigValue.Loader<T> reader, ConfigValue.Writer<T> writer, T defaultValue, @Nullable String... description) {
      this.path = path;
      this.description = description == null ? new String[0] : description;
      this.reader = reader;
      this.writer = writer;
      this.defaultValue = defaultValue;
   }

   @Deprecated
   public ConfigValue(String path, ConfigValue.Reader<T> reader, ConfigValue.Writer<T> writer, T defaultValue, String... description) {
      this(path, reader.update(), writer, defaultValue, description);
   }

   private static <T> ConfigValue<T> create(String path, ConfigValue.Loader<T> reader, ConfigValue.Writer<T> writer, T defaultValue, String... description) {
      return new ConfigValue<>(path, reader, writer, defaultValue, description);
   }

   public static <T> ConfigValue<T> create(
      String path, ConfigValue.Loader<T> reader, ConfigValue.Writer<T> writer, Supplier<T> defaultValue, String... description
   ) {
      return create(path, reader, writer, defaultValue.get(), description);
   }

   @Deprecated
   private static <T> ConfigValue<T> create(String path, ConfigValue.Reader<T> reader, ConfigValue.Writer<T> writer, T defaultValue, String... description) {
      return new ConfigValue<>(path, reader.update(), writer, defaultValue, description);
   }

   @Deprecated
   public static <T> ConfigValue<T> create(
      String path, ConfigValue.Reader<T> reader, ConfigValue.Writer<T> writer, Supplier<T> defaultValue, String... description
   ) {
      return create(path, reader, writer, defaultValue.get(), description);
   }

   @Deprecated
   public static <T> ConfigValue<T> create(String path, ConfigValue.Reader<T> reader, T defaultValue, String... description) {
      return create(path, reader.update(), FileConfig::set, defaultValue, description);
   }

   @Deprecated
   public static <T> ConfigValue<T> create(String path, ConfigValue.Reader<T> reader, Supplier<T> defaultValue, String... description) {
      return create(path, reader.update(), FileConfig::set, defaultValue, description);
   }

   public static <T> ConfigValue<T> create(String path, ConfigValue.Loader<T> reader, T defaultValue, String... description) {
      return create(path, reader, FileConfig::set, defaultValue, description);
   }

   public static <T> ConfigValue<T> create(String path, ConfigValue.Loader<T> reader, Supplier<T> defaultValue, String... description) {
      return create(path, reader, FileConfig::set, defaultValue, description);
   }

   public static ConfigValue<Boolean> create(String path, boolean defaultValue, String... description) {
      return create(path, MemorySection::getBoolean, defaultValue, description);
   }

   public static ConfigValue<Integer> create(String path, int defaultValue, String... description) {
      return create(path, MemorySection::getInt, defaultValue, description);
   }

   public static ConfigValue<int[]> create(String path, int[] defaultValue, String... description) {
      return create(path, FileConfig::getIntArray, FileConfig::setIntArray, defaultValue, description);
   }

   public static ConfigValue<Double> create(String path, double defaultValue, String... description) {
      return create(path, MemorySection::getDouble, defaultValue, description);
   }

   public static ConfigValue<Long> create(String path, long defaultValue, String... description) {
      return create(path, MemorySection::getLong, defaultValue, description);
   }

   public static ConfigValue<String> create(String path, String defaultValue, String... description) {
      return create(path, FileConfig::getString, defaultValue, description);
   }

   public static ConfigValue<String[]> create(String path, String[] defaultValue, String... description) {
      return create(path, FileConfig::getStringArray, FileConfig::setStringArray, defaultValue, description);
   }

   public static ConfigValue<List<String>> create(String path, List<String> defaultValue, String... description) {
      return create(path, MemorySection::getStringList, defaultValue, description);
   }

   public static ConfigValue<Set<String>> create(String path, Set<String> defaultValue, String... description) {
      return create(path, FileConfig::getStringSet, defaultValue, description);
   }

   @Deprecated
   public static ConfigValue<ItemStack> create(String path, ItemStack defaultValue, String... description) {
      return create(path, FileConfig::getItem, FileConfig::setItem, defaultValue, description);
   }

   public static ConfigValue<NightItem> create(String path, NightItem defaultValue, String... description) {
      return create(path, FileConfig::getCosmeticItem, FileConfig::set, defaultValue, description);
   }

   @Deprecated
   public static ConfigValue<UniSound> create(String path, UniSound defaultValue, String... description) {
      ConfigValue.Writer<UniSound> writer = (cfg, path1, obj) -> obj.write(cfg, path1);
      return create(path, UniSound::read, writer, defaultValue, description);
   }

   public static ConfigValue<NightSound> create(String path, NightSound defaultValue, String... description) {
      return create(path, FileConfig::getSound, FileConfig::set, defaultValue, description);
   }

   public static ConfigValue<su.nightexpress.nightcore.bridge.wrap.NightSound> create(
      String path, su.nightexpress.nightcore.bridge.wrap.NightSound defaultValue, String... description
   ) {
      return create(path, (config, path2) -> config.readSound(path2), FileConfig::set, defaultValue, description);
   }

   public static ConfigValue<UniParticle> create(String path, UniParticle defaultValue, String... description) {
      ConfigValue.Loader<UniParticle> reader = UniParticle::read;
      ConfigValue.Writer<UniParticle> writer = (cfg, path1, obj) -> obj.write(cfg, path1);
      return create(path, reader, writer, defaultValue, description);
   }

   @Deprecated
   public static ConfigValue<UniFormatter> create(String path, UniFormatter defaultValue, String... description) {
      return create(path, UniFormatter::read, defaultValue, description);
   }

   public static <E extends Enum<E>> ConfigValue<E> create(String path, Class<E> clazz, E defaultValue, String... description) {
      ConfigValue.Loader<E> reader = (cfg, path1) -> cfg.getEnum(path1, clazz);
      ConfigValue.Writer<E> writer = (cfg, path1, obj) -> cfg.set(path1, obj.name());
      return create(path, reader, writer, defaultValue, description);
   }

   public static <V> ConfigValue<Set<V>> forSet(
      String path, Function<String, V> reader, ConfigValue.Writer<Set<V>> writer, Supplier<Set<V>> defaultValue, String... description
   ) {
      return forSet(path, reader, writer, defaultValue.get(), description);
   }

   public static <V> ConfigValue<Set<V>> forSet(
      String path, Function<String, V> valFun, ConfigValue.Writer<Set<V>> writer, Set<V> defaultValue, String... description
   ) {
      ConfigValue.Loader<Set<V>> reader = (cfg, path1) -> cfg.getStringSet(path1)
         .stream()
         .map(valFun)
         .filter(Objects::nonNull)
         .collect(Collectors.toCollection(HashSet::new));
      return create(path, reader, writer, defaultValue, description);
   }

   @Deprecated
   public static <K, V> ConfigValue<Map<K, V>> forMap(
      String path,
      Function<String, K> keyFun,
      TriFunction<FileConfig, String, String, V> valFun,
      ConfigValue.Writer<Map<K, V>> writer,
      Supplier<Map<K, V>> defaultValue,
      String... description
   ) {
      return forMap(path, keyFun, valFun, writer, defaultValue.get(), description);
   }

   @Deprecated
   public static <K, V> ConfigValue<Map<K, V>> forMap(
      String path,
      Function<String, K> keyFun,
      TriFunction<FileConfig, String, String, V> valFun,
      ConfigValue.Writer<Map<K, V>> writer,
      Map<K, V> defaultValue,
      String... description
   ) {
      return forMap(path, keyFun, valFun, LinkedHashMap::new, writer, defaultValue, description);
   }

   @Deprecated
   public static <K, V> ConfigValue<TreeMap<K, V>> forTreeMap(
      String path,
      Function<String, K> keyFun,
      TriFunction<FileConfig, String, String, V> valFun,
      ConfigValue.Writer<TreeMap<K, V>> writer,
      Supplier<TreeMap<K, V>> defaultValue,
      String... description
   ) {
      return forTreeMap(path, keyFun, valFun, writer, defaultValue.get(), description);
   }

   @Deprecated
   public static <K, V> ConfigValue<TreeMap<K, V>> forTreeMap(
      String path,
      Function<String, K> keyFun,
      TriFunction<FileConfig, String, String, V> valFun,
      ConfigValue.Writer<TreeMap<K, V>> writer,
      TreeMap<K, V> defaultValue,
      String... description
   ) {
      return forMap(path, keyFun, valFun, TreeMap::new, writer, defaultValue, description);
   }

   @Deprecated
   public static <K, V, M extends Map<K, V>> ConfigValue<M> forMap(
      String path,
      Function<String, K> keyFun,
      TriFunction<FileConfig, String, String, V> valFun,
      Supplier<M> mapSupplier,
      ConfigValue.Writer<M> writer,
      M defaultValue,
      String... description
   ) {
      ConfigValue.Loader<M> reader = (cfg, path1) -> {
         M map = mapSupplier.get();

         for (String id : cfg.getSection(path1)) {
            K key = keyFun.apply(id);
            V val = valFun.apply(cfg, path1, id);
            if (key != null && val != null) {
               map.put(key, val);
            }
         }

         return map;
      };
      return create(path, reader, writer, defaultValue, description);
   }

   public static <K, V> ConfigValue<Map<K, V>> forMap(
      String path,
      Function<String, K> keyReadFun,
      Function<K, String> keyWriteFun,
      ReadFunction<V> valReadFun,
      Consumer<Map<K, V>> defaultValue,
      String... description
   ) {
      ConfigValue.Loader<Map<K, V>> reader = (config, readPath) -> {
         LinkedHashMap<K, V> map = new LinkedHashMap<>();

         for (String keyRaw : config.getSection(readPath)) {
            K key = keyReadFun.apply(keyRaw);
            V val = valReadFun.read(config, readPath + "." + keyRaw, keyRaw);
            if (key != null && val != null) {
               map.put(key, val);
            }
         }

         return map;
      };
      ConfigValue.Writer<Map<K, V>> writer = (config, writePath, map) -> map.forEach((key, value) -> {
         String keyRaw = keyWriteFun.apply((K)key);
         config.set(writePath + "." + keyRaw, value);
      });
      Map<K, V> defaultMap = new LinkedHashMap<>();
      defaultValue.accept(defaultMap);
      return create(path, reader, writer, defaultMap, description);
   }

   public static <K, V extends Writeable> ConfigValue<Map<K, V>> forMap(
      String path,
      Function<String, K> keyReadFun,
      Function<K, String> keyWriteFun,
      BiFunction<FileConfig, String, V> valReadFun,
      Consumer<Map<K, V>> defaultValue,
      String... description
   ) {
      return forMap(path, keyReadFun, keyWriteFun, (cfg, path2, id) -> valReadFun.apply(cfg, path2), defaultValue, description);
   }

   public static <V> ConfigValue<Map<String, V>> forMapById(
      String path, BiFunction<FileConfig, String, V> valReadFun, Consumer<Map<String, V>> defaultValue, String... description
   ) {
      return forMapById(path, (cfg, path2, id) -> valReadFun.apply(cfg, path2), defaultValue, description);
   }

   public static <V> ConfigValue<Map<String, V>> forMapById(
      String path, ReadFunction<V> valReadFun, Consumer<Map<String, V>> defaultValue, String... description
   ) {
      return forMap(path, String::toLowerCase, key -> key, valReadFun, defaultValue, description);
   }

   public static <E extends Enum<E>, V> ConfigValue<Map<E, V>> forMapByEnum(
      String path, Class<E> clazz, BiFunction<FileConfig, String, V> valReadFun, Consumer<Map<E, V>> defaultValue, String... description
   ) {
      return forMapByEnum(path, clazz, (cfg, path2, id) -> valReadFun.apply(cfg, path2), defaultValue, description);
   }

   public static <E extends Enum<E>, V> ConfigValue<Map<E, V>> forMapByEnum(
      String path, Class<E> clazz, ReadFunction<V> valReadFun, Consumer<Map<E, V>> defaultValue, String... description
   ) {
      return forMap(path, str -> StringUtil.getEnum(str, clazz).orElse(null), Enum::name, valReadFun, defaultValue, description);
   }

   @Deprecated
   public static <V> ConfigValue<Map<String, V>> forMap(
      String path,
      TriFunction<FileConfig, String, String, V> function,
      ConfigValue.Writer<Map<String, V>> writer,
      Supplier<Map<String, V>> defaultValue,
      String... description
   ) {
      return forMap(path, String::toLowerCase, function, writer, defaultValue.get(), description);
   }

   @Deprecated
   public static <V> ConfigValue<Map<String, V>> forMap(
      String path,
      TriFunction<FileConfig, String, String, V> function,
      ConfigValue.Writer<Map<String, V>> writer,
      Map<String, V> defaultValue,
      String... description
   ) {
      return forMap(path, String::toLowerCase, function, writer, defaultValue, description);
   }

   public ConfigValue<T> onRead(UnaryOperator<T> onRead) {
      this.onRead = onRead;
      return this;
   }

   public ConfigValue<T> whenRead(Consumer<T> onRead) {
      this.onRead(object -> {
         onRead.accept((T)object);
         return object;
      });
      return this;
   }

   public T read(FileConfig config) {
      if (!config.contains(this.path)) {
         this.write(config);
      }

      if (this.description.length > 0 && !this.description[0].isEmpty()) {
         config.setComments(this.path, this.description);
      }

      UnaryOperator<T> postRead = this.onRead == null ? value -> value : this.onRead;
      T read = this.reader.read(config, this.path);
      if (read == null) {
         read = this.defaultValue;
      }

      return this.value = postRead.apply(read);
   }

   public void write(FileConfig config) {
      this.getWriter().write(config, this.getPath(), this.get());
   }

   public boolean remove(FileConfig config) {
      return config.remove(this.getPath());
   }

   public T get() {
      return this.value == null ? this.defaultValue : this.value;
   }

   public void set(T value) {
      this.value = value;
   }

   public String getPath() {
      return this.path;
   }

   public String[] getDescription() {
      return this.description;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }

   public ConfigValue.Loader<T> getReader() {
      return this.reader;
   }

   public ConfigValue.Writer<T> getWriter() {
      return this.writer;
   }

   public interface Loader<T> {
      @Nullable
      T read(FileConfig var1, String var2);
   }

   @Deprecated
   public interface Reader<T> {
      T read(FileConfig var1, String var2, T var3);

      default ConfigValue.Loader<T> update() {
         return (cfg, path) -> this.read(cfg, path, null);
      }
   }

   public interface Writer<T> {
      void write(FileConfig var1, String var2, T var3);
   }
}
