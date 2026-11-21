package com.viaversion.viaversion.bukkit.listeners.v1_20_5to1_21;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.ProtocolStorables1_21;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PlayerChangeItemListener extends ViaBukkitListener {
   protected final Enchantment efficiency = this.getByName("efficiency", "DIG_SPEED");
   protected final Enchantment aquaAffinity = this.getByName("aqua_affinity", "WATER_WORKER");
   protected final Enchantment depthStrider = this.getByName("depth_strider", "DEPTH_STRIDER");
   protected final Enchantment soulSpeed = this.getByName("soul_speed", "SOUL_SPEED");
   protected final Enchantment swiftSneak = this.getByName("swift_sneak", "SWIFT_SNEAK");

   public PlayerChangeItemListener(ViaVersionPlugin plugin) {
      super(plugin, Protocol1_20_5To1_21.class);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerItemHeld(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      ItemStack item = player.getInventory().getItem(event.getNewSlot());
      this.sendAttributeUpdate(player, item, PlayerChangeItemListener.Slot.HAND);
   }

   protected EfficiencyAttributeStorage getEfficiencyStorage(UserConnection connection) {
      if (connection != null && connection.isActive()) {
         ProtocolStorables1_21 storables = connection.storables(Protocol1_20_5To1_21.class);
         return storables.efficiencyAttributes();
      } else {
         return null;
      }
   }

   void sendAttributeUpdate(Player player, @Nullable ItemStack item, PlayerChangeItemListener.Slot slot) {
      UserConnection connection = this.getUserConnection(player);
      EfficiencyAttributeStorage storage = this.getEfficiencyStorage(connection);
      if (storage != null) {
         EfficiencyAttributeStorage.ActiveEnchants enchants = storage.activeEnchants();

         enchants = switch (slot) {
            case HAND -> enchants.efficiency(item != null ? item.getEnchantmentLevel(this.efficiency) : 0);
            case HELMET -> enchants.aquaAffinity(item != null ? item.getEnchantmentLevel(this.aquaAffinity) : 0);
            case LEGGINGS -> enchants.swiftSneak(item != null && this.swiftSneak != null ? item.getEnchantmentLevel(this.swiftSneak) : 0);
            case BOOTS -> enchants.depthStrider(item != null && this.depthStrider != null ? item.getEnchantmentLevel(this.depthStrider) : 0);
         };
         storage.setEnchants(player.getEntityId(), connection, enchants);
      }
   }

   private Enchantment getByName(String newName, String oldName) {
      Enchantment enchantment = Enchantment.getByName(newName);
      return enchantment == null ? Enchantment.getByName(oldName) : enchantment;
   }

   static enum Slot {
      HAND,
      BOOTS,
      LEGGINGS,
      HELMET;
   }
}
