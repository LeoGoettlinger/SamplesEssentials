package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.items.GearRestrictionHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinition;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.BossBarUtil;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerItem {
   public PlayerItem.EquipmentSlot equipmentSlot;
   public Player player;
   public ItemStack itemStack = null;
   public int itemTier = 0;
   public ArrayList<ElitePotionEffect> continuousPotionEffects = new ArrayList<>();
   public ArrayList<ElitePotionEffect> onHitPotionEffects = new ArrayList<>();
   public int damageArthropodsLevel = 0;
   public int damageUndeadLevel = 0;
   public int thornsLevel = 0;
   private double critChance = 0.0;
   private double hunterChance = 0.0;
   private double eliteDamageReduction = 0.0;
   private double protectionProjectile = 0.0;
   private double eliteDamage = 0.0;
   private double eliteEnchantmentDamage = 0.0;
   private double blastProtection = 0.0;
   private double loudStrikesBonus = 0.0;
   private boolean displayingAsBroken = false;
   private PlayerItem.ItemRuntimeContext itemRuntimeContext = null;

   public PlayerItem(ItemStack itemStack, PlayerItem.EquipmentSlot equipmentSlot, Player player) {
      this.equipmentSlot = equipmentSlot;
      this.player = player;
      this.fullUpdate(itemStack);
   }

   private boolean fullUpdate(ItemStack itemStack) {
      boolean itemIsEmpty = itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0;
      boolean cachedItemIsEmpty = this.itemStack == null || this.itemStack.getType().isAir();
      if (itemIsEmpty) {
         if (this.displayingAsBroken) {
            BossBarUtil.HideBrokenItemBossBar(this.equipmentSlot, this.player);
            this.displayingAsBroken = false;
         }

         return cachedItemIsEmpty ? false : this.fillNullItem();
      } else {
         PlayerItem.ItemRuntimeContext currentRuntimeContext = PlayerItem.ItemRuntimeContext.capture(this.player, itemStack);
         if (!cachedItemIsEmpty && itemStack.isSimilar(this.itemStack) && currentRuntimeContext.equals(this.itemRuntimeContext)) {
            return false;
         } else if (EliteItemManager.isOnLastDamage(itemStack)) {
            if (!this.displayingAsBroken) {
               BossBarUtil.DisplayBrokenItemBossBar(
                  this.equipmentSlot,
                  this.player,
                  ItemSettingsConfig.getNoItemDurabilityMessage()
                     .replace("$item", !itemStack.getItemMeta().hasDisplayName() ? itemStack.getType().toString() : itemStack.getItemMeta().getDisplayName())
               );
               this.displayingAsBroken = true;
            }

            return this.fillNullItem();
         } else {
            if (this.displayingAsBroken) {
               BossBarUtil.HideBrokenItemBossBar(this.equipmentSlot, this.player);
               this.displayingAsBroken = false;
            }

            if (currentRuntimeContext.soulbindEnabled() && !SoulbindEnchantment.isValidSoulbindUser(itemStack.getItemMeta(), this.player)) {
               this.player.getWorld().dropItem(this.player.getLocation(), itemStack);
               itemStack.setAmount(0);
               return this.fillNullItem();
            } else if (!GearRestrictionHandler.canEquip(this.player, itemStack)) {
               GearRestrictionHandler.sendRestrictionMessage(this.player, itemStack);
               this.player.getWorld().dropItem(this.player.getLocation(), itemStack);
               itemStack.setAmount(0);
               return this.fillNullItem();
            } else {
               this.eliteDamage = 0.0;
               this.eliteEnchantmentDamage = 0.0;
               if (this.equipmentSlot.equals(PlayerItem.EquipmentSlot.MAINHAND)) {
                  this.itemTier = rawWeaponTier(itemStack);
                  this.eliteDamage = EliteItemManager.getEliteDamageFromEliteAttributes(itemStack);
               } else {
                  this.itemTier = (int)Math.round(EliteItemManager.getArmorLevel(itemStack));
               }

               if (currentRuntimeContext.dungeonLevelSync() > 0 && this.itemTier > currentRuntimeContext.dungeonLevelSync()) {
                  this.itemTier = currentRuntimeContext.dungeonLevelSync();
                  if (this.equipmentSlot.equals(PlayerItem.EquipmentSlot.MAINHAND)) {
                     this.eliteDamage = EliteItemManager.calculateEliteBonus(itemStack, this.itemTier);
                  }
               }

               this.continuousPotionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.continuousPotionEffectKey);
               this.onHitPotionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.onHitPotionEffectKey);
               this.eliteDamageReduction = EliteItemManager.getEliteDefense(itemStack) + EliteItemManager.getBonusEliteDefense(itemStack);
               this.protectionProjectile = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.PROJECTILE_PROTECTION.getKey());
               this.blastProtection = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.BLAST_PROTECTION.getKey());
               this.damageArthropodsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.BANE_OF_ARTHROPODS.getKey());
               this.damageUndeadLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.SMITE.getKey());
               this.critChance = EliteEnchantmentCatalog.contribution(
                  itemStack, EliteEnchantmentCatalog.CRITICAL_CHANCE, this.player.getUniqueId(), this.sharedSlot()
               );
               this.hunterChance = EliteEnchantmentCatalog.contribution(
                  itemStack, EliteEnchantmentCatalog.HUNTER_BONUS, this.player.getUniqueId(), this.sharedSlot()
               );
               this.thornsLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.THORNS.getKey());
               this.loudStrikesBonus = readLoudStrikesBonus(itemStack, this.sharedSlot(), this.player.getUniqueId());
               this.eliteEnchantmentDamage = EliteItemManager.getEliteDamageFromEnchantments(itemStack);
               this.itemStack = itemStack.clone();
               this.itemRuntimeContext = currentRuntimeContext;
               return true;
            }
         }
      }
   }

   private static int rawWeaponTier(ItemStack item) {
      return (int)Math.round(EliteItemManager.getWeaponLevel(item));
   }

   public static int readWeaponTier(Player player, ItemStack item) {
      int tier = rawWeaponTier(item);
      int sync = PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon ? dungeon.getLevelSync() : 0;
      return sync > 0 ? Math.min(tier, sync) : tier;
   }

   private boolean fillNullItem() {
      this.itemStack = null;
      this.itemTier = 0;
      this.continuousPotionEffects = new ArrayList<>();
      this.onHitPotionEffects = new ArrayList<>();
      this.damageArthropodsLevel = 0;
      this.damageUndeadLevel = 0;
      this.thornsLevel = 0;
      this.critChance = 0.0;
      this.hunterChance = 0.0;
      this.eliteDamageReduction = 0.0;
      this.protectionProjectile = 0.0;
      this.eliteDamage = 0.0;
      this.eliteEnchantmentDamage = 0.0;
      this.blastProtection = 0.0;
      this.loudStrikesBonus = 0.0;
      this.itemRuntimeContext = null;
      return true;
   }

   public int getTier(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.itemTier;
   }

   public double getEliteDamage(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.eliteDamage;
   }

   public double getEliteEnchantmentDamage(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.eliteEnchantmentDamage;
   }

   public double getEliteDefense(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.eliteDamageReduction;
   }

   public double getProtectionProjectile(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.protectionProjectile;
   }

   public double getBlastProtection(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.blastProtection;
   }

   public ArrayList<ElitePotionEffect> getContinuousPotionEffects(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.continuousPotionEffects;
   }

   public ArrayList<ElitePotionEffect> getOnHitPotionEffects(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.onHitPotionEffects;
   }

   public int getDamageArthropodsLevel(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.damageArthropodsLevel;
   }

   public int getDamageUndeadLevel(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.damageUndeadLevel;
   }

   public double getCritChance(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.critChance;
   }

   public double getHunterChance(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.hunterChance;
   }

   public double getLoudStrikesBonus(ItemStack itemStack, boolean update) {
      if (update) {
         this.fullUpdate(itemStack);
      }

      return this.loudStrikesBonus;
   }

   public static double readLoudStrikesBonus(ItemStack item, EnchantmentDefinition.Slot slot, UUID actor) {
      return item != null && !item.getType().isAir() && item.hasItemMeta() && !EliteItemManager.isOnLastDamage(item)
         ? EliteEnchantmentCatalog.contribution(item, EliteEnchantmentCatalog.THREAT_BONUS, actor, slot)
         : 0.0;
   }

   private EnchantmentDefinition.Slot sharedSlot() {
      return EnchantmentDefinition.Slot.valueOf(switch (this.equipmentSlot) {
         case HELMET -> "HEAD";
         case CHESTPLATE -> "CHEST";
         case LEGGINGS -> "LEGS";
         case BOOTS -> "FEET";
         default -> this.equipmentSlot.name();
      });
   }

   public static enum EquipmentSlot {
      HELMET,
      CHESTPLATE,
      LEGGINGS,
      BOOTS,
      MAINHAND,
      OFFHAND;
   }

   private record ItemRuntimeContext(
      GearRestrictionHandler.RestrictionContext restrictionContext,
      boolean soulbindEnabled,
      int dungeonLevelSync,
      long enchantmentRevision,
      long potionRevision
   ) {
      private static PlayerItem.ItemRuntimeContext capture(Player player, ItemStack itemStack) {
         int dungeonLevelSync = PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeonInstance ? dungeonInstance.getLevelSync() : 0;
         return new PlayerItem.ItemRuntimeContext(
            GearRestrictionHandler.getRestrictionContext(player, itemStack),
            EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled(),
            dungeonLevelSync,
            EliteEnchantmentCatalog.revision(),
            PotionEffectsConfig.revision()
         );
      }
   }
}
