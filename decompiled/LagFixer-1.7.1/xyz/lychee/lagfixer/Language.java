package xyz.lychee.lagfixer;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TextComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.MiniMessage;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class Language {
   private static final YamlConfiguration yaml = new YamlConfiguration();
   private static final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
      .character('&')
      .hexCharacter('#')
      .hexColors()
      .useUnusualXRepeatedCharacterHexFormat()
      .build();
   private static final Map<String, String> mainValues = new HashMap<>();
   private final AbstractModule module;
   private final Map<String, String> values = new HashMap<>();

   public Language(AbstractModule module) {
      this.module = module;
   }

   public static Component getMainValue(String key, boolean prefix, TagResolver.Single... placeholders) {
      return !mainValues.containsKey(key) ? null : createComponent(mainValues.get(key), prefix, placeholders);
   }

   public static Component createComponent(String message, boolean prefix, TagResolver.Single... placeholders) {
      Component component = MiniMessage.miniMessage().deserialize(message, placeholders);
      return prefix ? ((TextComponent)Component.empty().append(ConfigManager.getInstance().getPrefix())).append(component) : component;
   }

   public void loadMessages() {
      ConfigurationSection section = yaml.getConfigurationSection("messages." + this.module.getName());
      if (section != null) {
         this.values.clear();
         section.getValues(true).forEach((key, value) -> {
            if (value instanceof String) {
               this.values.put(key, (String)value);
            }
         });
      }
   }

   public boolean hasTranslation(String key) {
      return this.values.containsKey(key);
   }

   public Component getComponent(String key, boolean prefix, TagResolver.Single... placeholders) {
      return (Component)(!this.values.containsKey(key) ? Component.text("Unknown value - " + key) : createComponent(this.values.get(key), prefix, placeholders));
   }

   public String getString(String key, boolean prefix, TagResolver.Single... placeholders) {
      Component component = this.getComponent(key, prefix, placeholders);
      return component == null ? "Unknown value - " + key : MessageUtils.fixColors(null, getSerializer().serialize(component));
   }

   @Generated
   public AbstractModule getModule() {
      return this.module;
   }

   @Generated
   public Map<String, String> getValues() {
      return this.values;
   }

   @Generated
   public static YamlConfiguration getYaml() {
      return yaml;
   }

   @Generated
   public static LegacyComponentSerializer getSerializer() {
      return serializer;
   }

   @Generated
   public static Map<String, String> getMainValues() {
      return mainValues;
   }
}
