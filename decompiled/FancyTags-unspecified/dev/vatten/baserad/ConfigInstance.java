package dev.vatten.baserad;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurationStore;
import java.nio.file.Path;
import lombok.Generated;

public class ConfigInstance<T> {
   private static final YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();
   protected final VattenPlugin plugin;
   protected final String name;
   protected final Class<T> clazz;
   protected final YamlConfigurationStore<T> configStore;
   protected final Path file;
   protected T data;

   public ConfigInstance(VattenPlugin plugin, String name, Class<T> clazz) {
      this.plugin = plugin;
      this.name = name;
      this.clazz = clazz;
      this.configStore = new YamlConfigurationStore<>(clazz, properties);
      this.file = plugin.getPath().resolve(name + ".yml");
   }

   void load() {
      this.data = this.configStore.update(this.file);
   }

   void save() {
      this.configStore.save(this.data, this.file);
   }

   @Generated
   public T getData() {
      return this.data;
   }
}
