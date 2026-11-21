package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.MetadataHandler;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class ClassMinionIdentity {
   private static final String KEY_NAME = "class_minion";
   private final NamespacedKey key;

   ClassMinionIdentity(Plugin plugin) {
      this.key = new NamespacedKey(Objects.requireNonNull(plugin, "plugin"), "class_minion");
   }

   void mark(Entity entity) {
      Objects.requireNonNull(entity, "entity").getPersistentDataContainer().set(this.key, PersistentDataType.BYTE, (byte)1);
   }

   boolean matches(Entity entity) {
      return entity != null && entity.getPersistentDataContainer().has(this.key, PersistentDataType.BYTE);
   }

   public static boolean isMinion(Entity entity) {
      Plugin plugin = MetadataHandler.PLUGIN;
      return plugin != null && entity != null && entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "class_minion"), PersistentDataType.BYTE);
   }
}
