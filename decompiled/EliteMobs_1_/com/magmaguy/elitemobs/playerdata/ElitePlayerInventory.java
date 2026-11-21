package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.elitemobs.utils.BossBarUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ElitePlayerInventory {
   public static HashMap<UUID, ElitePlayerInventory> playerInventories = new HashMap<>();
   public final PlayerItem helmet;
   public final PlayerItem chestplate;
   public final PlayerItem leggings;
   public final PlayerItem boots;
   public final PlayerItem mainhand;
   public final PlayerItem offhand;
   private final Player player;
   private final HashSet<String> customMetadata = new HashSet<>();

   public ElitePlayerInventory(Player player) {
      this.player = player;
      this.helmet = new PlayerItem(player.getInventory().getHelmet(), PlayerItem.EquipmentSlot.HELMET, player);
      this.chestplate = new PlayerItem(player.getInventory().getChestplate(), PlayerItem.EquipmentSlot.CHESTPLATE, player);
      this.leggings = new PlayerItem(player.getInventory().getLeggings(), PlayerItem.EquipmentSlot.LEGGINGS, player);
      this.boots = new PlayerItem(player.getInventory().getBoots(), PlayerItem.EquipmentSlot.BOOTS, player);
      this.mainhand = new PlayerItem(player.getInventory().getItemInMainHand(), PlayerItem.EquipmentSlot.MAINHAND, player);
      this.offhand = new PlayerItem(player.getInventory().getItemInOffHand(), PlayerItem.EquipmentSlot.OFFHAND, player);
      playerInventories.put(player.getUniqueId(), this);
   }

   public static ElitePlayerInventory getPlayer(Player player) {
      if (player != null && player.isOnline() && !player.hasMetadata("NPC")) {
         ElitePlayerInventory elitePlayerInventory = playerInventories.get(player.getUniqueId());
         return elitePlayerInventory != null && elitePlayerInventory.player == player ? elitePlayerInventory : new ElitePlayerInventory(player);
      } else {
         return null;
      }
   }

   public static void shutdown() {
      playerInventories.clear();
   }

   public static void initialize() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         getPlayer(player);
      }
   }

   public static ItemStack[] getHeldAndEquippedItems(PlayerInventory inventory) {
      return new ItemStack[]{
         inventory.getItemInMainHand(),
         inventory.getItemInOffHand(),
         inventory.getHelmet(),
         inventory.getChestplate(),
         inventory.getLeggings(),
         inventory.getBoots()
      };
   }

   public double getEliteDamage(boolean update) {
      return this.helmet.getEliteDamage(this.player.getInventory().getHelmet(), update)
         + this.chestplate.getEliteDamage(this.player.getInventory().getChestplate(), update)
         + this.leggings.getEliteDamage(this.player.getInventory().getLeggings(), update)
         + this.boots.getEliteDamage(this.player.getInventory().getBoots(), update)
         + this.mainhand.getEliteDamage(this.player.getInventory().getItemInMainHand(), update)
         + this.offhand.getEliteDamage(this.player.getInventory().getItemInOffHand(), update);
   }

   public double getEliteEnchantmentDamage(boolean update) {
      return this.helmet.getEliteEnchantmentDamage(this.player.getInventory().getHelmet(), update)
         + this.chestplate.getEliteEnchantmentDamage(this.player.getInventory().getChestplate(), update)
         + this.leggings.getEliteEnchantmentDamage(this.player.getInventory().getLeggings(), update)
         + this.boots.getEliteEnchantmentDamage(this.player.getInventory().getBoots(), update)
         + this.mainhand.getEliteEnchantmentDamage(this.player.getInventory().getItemInMainHand(), update)
         + this.offhand.getEliteEnchantmentDamage(this.player.getInventory().getItemInOffHand(), update);
   }

   public double getEliteDefense(boolean update) {
      return this.helmet.getEliteDefense(this.player.getInventory().getHelmet(), update)
         + this.chestplate.getEliteDefense(this.player.getInventory().getChestplate(), update)
         + this.leggings.getEliteDefense(this.player.getInventory().getLeggings(), update)
         + this.boots.getEliteDefense(this.player.getInventory().getBoots(), update)
         + this.mainhand.getEliteDefense(this.player.getInventory().getItemInMainHand(), update)
         + this.offhand.getEliteDefense(this.player.getInventory().getItemInOffHand(), update);
   }

   public double getEliteProjectileProtection(boolean update) {
      return (
            this.helmet.getProtectionProjectile(this.player.getInventory().getHelmet(), update)
               + this.chestplate.getProtectionProjectile(this.player.getInventory().getChestplate(), update)
               + this.leggings.getProtectionProjectile(this.player.getInventory().getLeggings(), update)
               + this.boots.getProtectionProjectile(this.player.getInventory().getBoots(), update)
               + this.mainhand.getProtectionProjectile(this.player.getInventory().getItemInMainHand(), update)
               + this.offhand.getProtectionProjectile(this.player.getInventory().getItemInOffHand(), update)
         )
         / 6.0;
   }

   public double getEliteBlastProtection(boolean update) {
      return (
            this.helmet.getBlastProtection(this.player.getInventory().getHelmet(), update)
               + this.chestplate.getBlastProtection(this.player.getInventory().getChestplate(), update)
               + this.leggings.getBlastProtection(this.player.getInventory().getLeggings(), update)
               + this.boots.getBlastProtection(this.player.getInventory().getBoots(), update)
               + this.mainhand.getBlastProtection(this.player.getInventory().getItemInMainHand(), update)
               + this.offhand.getBlastProtection(this.player.getInventory().getItemInOffHand(), update)
         )
         / 6.0;
   }

   public int getWeaponLevel(boolean update) {
      return this.mainhand.getTier(this.player.getInventory().getItemInMainHand(), update);
   }

   public int getFullPlayerTier(boolean update) {
      return CombatLevelCalculator.calculateCombatLevel(this.player.getUniqueId());
   }

   public int getNaturalMobSpawnLevel(boolean update) {
      return this.player.getGameMode().equals(GameMode.SPECTATOR) ? 0 : CombatLevelCalculator.calculateCombatLevel(this.player.getUniqueId());
   }

   public ArrayList<ElitePotionEffect> getContinuousPotionEffects(boolean update) {
      ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
      elitePotionEffects.addAll(this.helmet.getContinuousPotionEffects(this.player.getInventory().getHelmet(), update));
      elitePotionEffects.addAll(this.chestplate.getContinuousPotionEffects(this.player.getInventory().getChestplate(), update));
      elitePotionEffects.addAll(this.leggings.getContinuousPotionEffects(this.player.getInventory().getLeggings(), update));
      elitePotionEffects.addAll(this.boots.getContinuousPotionEffects(this.player.getInventory().getBoots(), update));
      elitePotionEffects.addAll(this.mainhand.getContinuousPotionEffects(this.player.getInventory().getItemInMainHand(), update));
      elitePotionEffects.addAll(this.offhand.getContinuousPotionEffects(this.player.getInventory().getItemInOffHand(), update));
      return elitePotionEffects;
   }

   public ArrayList<ElitePotionEffect> getOnHitPotionEffects(boolean update) {
      ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();
      elitePotionEffects.addAll(this.helmet.getOnHitPotionEffects(this.player.getInventory().getHelmet(), update));
      elitePotionEffects.addAll(this.chestplate.getOnHitPotionEffects(this.player.getInventory().getChestplate(), update));
      elitePotionEffects.addAll(this.leggings.getOnHitPotionEffects(this.player.getInventory().getLeggings(), update));
      elitePotionEffects.addAll(this.boots.getOnHitPotionEffects(this.player.getInventory().getBoots(), update));
      elitePotionEffects.addAll(this.mainhand.getOnHitPotionEffects(this.player.getInventory().getItemInMainHand(), update));
      elitePotionEffects.addAll(this.offhand.getOnHitPotionEffects(this.player.getInventory().getItemInOffHand(), update));
      return elitePotionEffects;
   }

   public double getCritChance(boolean update) {
      ItemStack[] activeItems = getHeldAndEquippedItems(this.player.getInventory());
      double critChance = this.mainhand.getCritChance(activeItems[0], update)
         + this.offhand.getCritChance(activeItems[1], update)
         + this.helmet.getCritChance(activeItems[2], update)
         + this.chestplate.getCritChance(activeItems[3], update)
         + this.leggings.getCritChance(activeItems[4], update)
         + this.boots.getCritChance(activeItems[5], update);
      double cap = Math.max(0.0, Math.min(1.0, MobCombatSettingsConfig.getMaximumCriticalStrikeChance()));
      return Math.min(cap, critChance);
   }

   public double getHunterChance(boolean update) {
      return this.helmet.getHunterChance(this.player.getInventory().getHelmet(), update)
         + this.chestplate.getHunterChance(this.player.getInventory().getChestplate(), update)
         + this.leggings.getHunterChance(this.player.getInventory().getLeggings(), update)
         + this.boots.getHunterChance(this.player.getInventory().getBoots(), update)
         + this.mainhand.getHunterChance(this.player.getInventory().getItemInMainHand(), update)
         + this.offhand.getHunterChance(this.player.getInventory().getItemInOffHand(), update);
   }

   public double baseDamage() {
      return this.getWeaponLevel(true) == 0 ? 1.0 : this.getWeaponLevel(false);
   }

   public double getLoudStrikesBonusMultiplier(boolean update) {
      return this.helmet.getLoudStrikesBonus(this.player.getInventory().getHelmet(), update)
         + this.chestplate.getLoudStrikesBonus(this.player.getInventory().getChestplate(), update)
         + this.leggings.getLoudStrikesBonus(this.player.getInventory().getLeggings(), update)
         + this.boots.getLoudStrikesBonus(this.player.getInventory().getBoots(), update)
         + this.mainhand.getLoudStrikesBonus(this.player.getInventory().getItemInMainHand(), update)
         + this.offhand.getLoudStrikesBonus(this.player.getInventory().getItemInOffHand(), update);
   }

   public boolean hasTag(String string) {
      return this.customMetadata.contains(string);
   }

   public HashSet<String> getTags() {
      return this.customMetadata;
   }

   public void addTags(List<String> string) {
      this.customMetadata.addAll(string);
   }

   public void removeTags(List<String> string) {
      this.customMetadata.removeAll(string);
   }

   public static class ElitePlayerInventoryEvents implements Listener {
      @EventHandler
      public void onPlayerJoin(PlayerJoinEvent event) {
         ElitePlayerInventory.getPlayer(event.getPlayer());
      }

      @EventHandler
      public void onPlayerLogout(PlayerQuitEvent event) {
         ElitePlayerInventory.playerInventories
            .computeIfPresent(
               event.getPlayer().getUniqueId(), (id, inventory) -> (ElitePlayerInventory)(inventory.player == event.getPlayer() ? null : inventory)
            );
         BossBarUtil.clearPlayer(event.getPlayer().getUniqueId());
      }
   }
}
