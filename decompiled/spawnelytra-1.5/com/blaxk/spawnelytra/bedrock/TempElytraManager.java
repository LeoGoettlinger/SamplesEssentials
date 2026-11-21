package com.blaxk.spawnelytra.bedrock;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.integration.BedrockSupport;
import com.blaxk.spawnelytra.listener.SpawnElytra;
import com.blaxk.spawnelytra.util.MessageUtil;
import com.blaxk.spawnelytra.util.SchedulerUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class TempElytraManager implements Listener {
   private final Main plugin;
   private final NamespacedKey tempElytraKey;
   private final NamespacedKey storedChestplateKey;
   private final Map<UUID, ItemStack> disguisedChestplates = new HashMap<>();

   public TempElytraManager(Main plugin) {
      this.plugin = plugin;
      this.tempElytraKey = new NamespacedKey(plugin, "temp_elytra");
      this.storedChestplateKey = new NamespacedKey(plugin, "stored_chestplate");
   }

   public boolean isTempElytra(ItemStack item) {
      if (item != null && Material.ELYTRA == item.getType() && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && meta.getPersistentDataContainer().has(this.tempElytraKey, PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   public boolean hasTempElytraEquipped(Player player) {
      return this.isTempElytra(player.getInventory().getChestplate());
   }

   public void ensureEquipped(Player player) {
      this.removeRogueTempElytras(player);
      ItemStack chestplate = player.getInventory().getChestplate();
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      if (this.isTempElytra(chestplate)) {
         if (!this.disguisedChestplates.containsKey(player.getUniqueId())) {
            this.setDisguise(player, this.deserializeChestplate((String)pdc.get(this.storedChestplateKey, PersistentDataType.STRING)));
         }
      } else {
         String existingBackup = (String)pdc.get(this.storedChestplateKey, PersistentDataType.STRING);
         if (existingBackup != null) {
            if (chestplate == null || Material.AIR == chestplate.getType()) {
               this.setDisguise(player, this.deserializeChestplate(existingBackup));
               player.getInventory().setChestplate(this.createTempElytra());
               return;
            }

            ItemStack previousOriginal = this.deserializeChestplate(existingBackup);
            pdc.remove(this.storedChestplateKey);
            if (previousOriginal != null) {
               this.giveOrDrop(player, previousOriginal);
            }
         }

         pdc.set(this.storedChestplateKey, PersistentDataType.STRING, this.serializeChestplate(chestplate));
         this.setDisguise(player, chestplate);
         player.getInventory().setChestplate(this.createTempElytra());
      }
   }

   public void ensureRestored(Player player) {
      this.disguisedChestplates.remove(player.getUniqueId());
      this.removeRogueTempElytras(player);
      ItemStack chestplate = player.getInventory().getChestplate();
      boolean hadTemp = this.isTempElytra(chestplate);
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      String backup = (String)pdc.get(this.storedChestplateKey, PersistentDataType.STRING);
      if (hadTemp || backup != null) {
         if (hadTemp) {
            player.getInventory().setChestplate(null);
         }

         if (backup != null) {
            pdc.remove(this.storedChestplateKey);
            ItemStack original = this.deserializeChestplate(backup);
            if (original != null) {
               ItemStack currentSlot = player.getInventory().getChestplate();
               if (currentSlot != null && Material.AIR != currentSlot.getType()) {
                  this.giveOrDrop(player, original);
               } else {
                  player.getInventory().setChestplate(original);
               }
            }
         }
      }
   }

   public void restoreAll() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (!this.keepEquippedWhileAirborne(player)) {
            this.ensureRestored(player);
         }
      }
   }

   public boolean hasDisguise(Player wearer) {
      return this.disguisedChestplates.containsKey(wearer.getUniqueId());
   }

   public void sendDisguise(Player wearer, Player viewer) {
      ItemStack shown = this.disguisedChestplates.get(wearer.getUniqueId());
      if (shown != null && !viewer.equals(wearer)) {
         try {
            viewer.sendEquipmentChange(wearer, EquipmentSlot.CHEST, shown);
         } catch (Throwable var5) {
         }
      }
   }

   private void setDisguise(Player player, ItemStack shownItem) {
      ItemStack shown = shownItem != null && Material.AIR != shownItem.getType() ? shownItem.clone() : new ItemStack(Material.AIR);
      this.disguisedChestplates.put(player.getUniqueId(), shown);
      SchedulerUtil.runAtEntityLater(this.plugin, player, 2L, () -> this.broadcastDisguise(player));
   }

   private void broadcastDisguise(Player wearer) {
      if (wearer.isOnline() && this.hasDisguise(wearer)) {
         for (Player viewer : this.viewersOf(wearer)) {
            this.sendDisguise(wearer, viewer);
         }
      }
   }

   private Collection<? extends Player> viewersOf(Player wearer) {
      try {
         return wearer.getTrackedPlayers();
      } catch (Throwable var3) {
         return wearer.getWorld().getPlayers();
      }
   }

   @EventHandler
   public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
      NamespacedKey key = event.getAdvancement().getKey();
      if ("minecraft".equals(key.getNamespace()) && "end/elytra".equals(key.getKey())) {
         Player player = event.getPlayer();
         if (this.hasTempElytraEquipped(player)) {
            try {
               event.message(null);
            } catch (Throwable var5) {
            }
         }
      }
   }

   private ItemStack createTempElytra() {
      ItemStack elytra = new ItemStack(Material.ELYTRA);
      ItemMeta meta = elytra.getItemMeta();
      if (meta != null) {
         meta.getPersistentDataContainer().set(this.tempElytraKey, PersistentDataType.BYTE, (byte)1);
         meta.setUnbreakable(true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
         Component lore = MessageUtil.component("temp_elytra_lore").decoration(TextDecoration.ITALIC, false);

         try {
            meta.lore(List.of(lore));
         } catch (Throwable var5) {
            meta.setLore(List.of(LegacyComponentSerializer.legacySection().serialize(lore)));
         }

         elytra.setItemMeta(meta);
      }

      return elytra;
   }

   private String serializeChestplate(ItemStack chestplate) {
      if (chestplate != null && Material.AIR != chestplate.getType()) {
         YamlConfiguration yaml = new YamlConfiguration();
         yaml.set("item", chestplate);
         return yaml.saveToString();
      } else {
         return "";
      }
   }

   private ItemStack deserializeChestplate(String data) {
      if (data != null && !data.isEmpty()) {
         try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(data);
            return yaml.getItemStack("item");
         } catch (Exception var3) {
            this.plugin.getLogger().warning("Failed to restore a stored chestplate: " + var3.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }

   private void giveOrDrop(Player player, ItemStack item) {
      Map<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack[]{item});

      for (ItemStack leftover : overflow.values()) {
         player.getWorld().dropItemNaturally(player.getLocation(), leftover);
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (this.hasTempElytraEquipped(player) && BedrockSupport.isManaged(player)) {
         SpawnElytra instance = this.plugin.getSpawnElytraInstance(player.getWorld().getName());
         if (instance != null && instance.isValid()) {
            this.ensureEquipped(player);
            instance.resumeBedrockFlight(player);
            return;
         }
      }

      this.ensureRestored(player);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (!this.keepEquippedWhileAirborne(player)) {
         this.ensureRestored(player);
      }
   }

   private boolean keepEquippedWhileAirborne(Player player) {
      if (this.hasTempElytraEquipped(player) && !player.isOnGround()) {
         this.disguisedChestplates.remove(player.getUniqueId());
         return true;
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      boolean removedFromDrops = event.getDrops().removeIf(this::isTempElytra);
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      String backup = (String)pdc.get(this.storedChestplateKey, PersistentDataType.STRING);
      if (backup != null) {
         if (removedFromDrops) {
            pdc.remove(this.storedChestplateKey);
            ItemStack original = this.deserializeChestplate(backup);
            if (original != null) {
               event.getDrops().add(original);
            }
         }
      }
   }

   @EventHandler
   public void onRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      SchedulerUtil.runAtEntityLater(this.plugin, player, 1L, () -> {
         if (player.isOnline()) {
            this.ensureRestored(player);
         }
      });
   }

   @EventHandler
   public void onChangedWorld(PlayerChangedWorldEvent event) {
      this.ensureRestored(event.getPlayer());
   }

   @EventHandler
   public void onGameModeChange(PlayerGameModeChangeEvent event) {
      GameMode newMode = event.getNewGameMode();
      if (GameMode.CREATIVE == newMode || GameMode.SPECTATOR == newMode) {
         this.ensureRestored(event.getPlayer());
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (!this.isTempElytra(event.getCurrentItem()) && !this.isTempElytra(event.getCursor())) {
         if (event.getWhoClicked() instanceof Player player
            && this.hasTempElytraEquipped(player)
            && ClickType.RIGHT == event.getClick()
            && this.isChestEquippable(event.getCurrentItem())) {
            event.setCancelled(true);
            this.resyncInventory(player);
         }
      } else {
         event.setCancelled(true);
         this.resyncInventory(event.getWhoClicked());
      }
   }

   private boolean isChestEquippable(ItemStack item) {
      if (item == null) {
         return false;
      } else {
         return switch (item.getType()) {
            case ELYTRA, LEATHER_CHESTPLATE, CHAINMAIL_CHESTPLATE, IRON_CHESTPLATE, GOLDEN_CHESTPLATE, DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE -> true;
            default -> false;
         };
      }
   }

   @EventHandler
   public void onRightClickArmor(PlayerInteractEvent event) {
      if (Action.RIGHT_CLICK_AIR == event.getAction() || Action.RIGHT_CLICK_BLOCK == event.getAction()) {
         Player player = event.getPlayer();
         if (this.isChestEquippable(event.getItem()) && this.hasTempElytraEquipped(player)) {
            event.setUseItemInHand(Result.DENY);
            this.resyncInventory(player);
         }
      }
   }

   private void removeRogueTempElytras(Player player) {
      ItemStack[] storage = player.getInventory().getStorageContents();
      boolean changed = false;

      for (int i = 0; i < storage.length; i++) {
         if (this.isTempElytra(storage[i])) {
            storage[i] = null;
            changed = true;
         }
      }

      if (changed) {
         player.getInventory().setStorageContents(storage);
      }

      if (this.isTempElytra(player.getInventory().getItemInOffHand())) {
         player.getInventory().setItemInOffHand(null);
      }
   }

   private void resyncInventory(HumanEntity human) {
      if (human instanceof Player player) {
         SchedulerUtil.runAtEntityLater(this.plugin, player, 1L, () -> {
            if (player.isOnline()) {
               player.updateInventory();
            }
         });
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      if (this.isTempElytra(event.getOldCursor())) {
         event.setCancelled(true);
      } else {
         for (ItemStack item : event.getNewItems().values()) {
            if (this.isTempElytra(item)) {
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onItemDrop(PlayerDropItemEvent event) {
      if (this.isTempElytra(event.getItemDrop().getItemStack())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
      if (this.isTempElytra(event.getPlayerItem()) || this.isTempElytra(event.getArmorStandItem())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onItemDamage(PlayerItemDamageEvent event) {
      if (this.isTempElytra(event.getItem())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onItemSpawn(ItemSpawnEvent event) {
      if (this.isTempElytra(event.getEntity().getItemStack())) {
         event.setCancelled(true);
      }
   }
}
