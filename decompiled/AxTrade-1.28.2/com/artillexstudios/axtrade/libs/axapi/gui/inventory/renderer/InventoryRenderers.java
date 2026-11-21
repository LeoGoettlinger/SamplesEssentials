package com.artillexstudios.axtrade.libs.axapi.gui.inventory.renderer;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class InventoryRenderers {
   private static final ConcurrentHashMap<UUID, InventoryRenderer> renderers = new ConcurrentHashMap<>();
   private static final Collection<InventoryRenderer> rendererView = Collections.unmodifiableCollection(renderers.values());

   public static InventoryRenderer getRenderer(Player player) {
      return renderers.computeIfAbsent(player.getUniqueId(), uuid -> new InventoryRenderer(player));
   }

   public static void disconnect(UUID uuid) {
      renderers.remove(uuid);
   }

   public static Collection<InventoryRenderer> getCurrentRenderers() {
      return rendererView;
   }
}
