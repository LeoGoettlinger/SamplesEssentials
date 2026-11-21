package net.ess3.api;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.plugin.Plugin;

public final class PluginKey {
   private final Plugin plugin;
   private final String key;

   private PluginKey(final Plugin plugin, final String key) {
      this.plugin = plugin;
      this.key = key;
   }

   public static PluginKey random(final Plugin plugin) {
      return new PluginKey(plugin, UUID.randomUUID().toString());
   }

   public static PluginKey fromKey(final Plugin plugin, final String key) {
      return new PluginKey(plugin, key);
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public String getKey() {
      return this.key;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.plugin, this.key);
   }

   @Override
   public String toString() {
      return this.plugin.getName().toLowerCase() + ":" + this.key;
   }

   @Override
   public boolean equals(final Object o) {
      return !(o instanceof PluginKey) && !o.getClass().getName().equals("org.bukkit.NamespacedKey")
         ? false
         : this == o || this.toString().equals(o.toString());
   }
}
