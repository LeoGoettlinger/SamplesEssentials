package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

final class CombatTestEquipment {
   private PlayerInventory inventory;
   private final UUID playerId;
   private final Map<Integer, ItemStack> originals = new LinkedHashMap<>();
   private final Set<Integer> borrowed = new LinkedHashSet<>();
   private final String owner = UUID.randomUUID().toString();
   private static final NamespacedKey OWNER = new NamespacedKey("elitemobs", "diagnostic_equipment");
   private int heldSlot;
   private boolean captured;

   CombatTestEquipment(Player player) {
      this.inventory = player.getInventory();
      this.playerId = player.getUniqueId();
   }

   void capture() {
      if (!this.captured) {
         this.heldSlot = this.inventory.getHeldItemSlot();
         this.originals.put(this.heldSlot, cloneItem(this.inventory.getItem(this.heldSlot)));

         for (int slot = 36; slot <= 40; slot++) {
            this.originals.put(slot, cloneItem(this.inventory.getItem(slot)));
         }

         this.captured = true;

         for (Entry<Integer, ItemStack> entry : this.originals.entrySet()) {
            this.write(entry.getKey(), cloneItem(entry.getValue()));
         }
      }
   }

   void restore() {
      if (this.captured) {
         Player currentPlayer = Bukkit.getPlayer(this.playerId);
         if (currentPlayer != null) {
            this.inventory = currentPlayer.getInventory();
         }

         RuntimeException failure = null;

         for (int slot : List.copyOf(this.borrowed)) {
            try {
               ItemStack current = this.inventory.getItem(slot);
               ItemStack original = this.originals.get(slot);
               if (!this.owned(current) && !Objects.equals(current, original)) {
                  throw new IllegalStateException("Diagnostic slot " + slot + " changed externally; original item retained");
               }

               this.inventory.setItem(slot, cloneItem(original));
               this.borrowed.remove(slot);
            } catch (RuntimeException var7) {
               if (failure == null) {
                  failure = var7;
               } else {
                  failure.addSuppressed(var7);
               }
            }
         }

         if (failure != null) {
            throw failure;
         } else {
            this.captured = false;
         }
      }
   }

   private boolean owned(ItemStack item) {
      return item != null && item.hasItemMeta() && this.owner.equals(item.getItemMeta().getPersistentDataContainer().get(OWNER, PersistentDataType.STRING));
   }

   private void write(int slot, ItemStack replacement) {
      ItemStack current = this.inventory.getItem(slot);
      if (!this.owned(current) && !Objects.equals(current, this.originals.get(slot))) {
         throw new IllegalStateException("Diagnostic slot " + slot + " changed externally");
      } else {
         if (replacement != null && !replacement.getType().isAir()) {
            ItemMeta meta = replacement.getItemMeta();
            meta.getPersistentDataContainer().set(OWNER, PersistentDataType.STRING, this.owner);
            meta.setUnbreakable(true);
            replacement.setItemMeta(meta);
         }

         this.borrowed.add(slot);
         this.inventory.setItem(slot, replacement);
      }
   }

   void equipWeapon(SkillType skillType) {
      ItemStack weapon = switch (skillType) {
         case SWORDS -> {
            ItemStack var18 = new ItemStack(Material.NETHERITE_SWORD);
            yield var18;
         }
         case AXES -> {
            ItemStack var17 = new ItemStack(Material.NETHERITE_AXE);
            yield var17;
         }
         case BOWS -> {
            ItemStack var16 = new ItemStack(Material.BOW);
            yield var16;
         }
         case CROSSBOWS -> {
            ItemStack var15 = new ItemStack(Material.CROSSBOW);
            yield var15;
         }
         case TRIDENTS -> {
            ItemStack var14 = new ItemStack(Material.TRIDENT);
            yield var14;
         }
         case HOES -> {
            ItemStack var13 = new ItemStack(Material.NETHERITE_HOE);
            yield var13;
         }
         case MACES -> {
            ItemStack var12 = new ItemStack(Material.MACE);
            yield var12;
         }
         case STAVES -> {
            ItemStack var11;
            try {
               var11 = explicitlyIdentified(new ItemStack(Material.WOODEN_SPEAR), SkillType.STAVES);
            } catch (NoSuchFieldError var6) {
               var11 = explicitlyIdentified(new ItemStack(Material.STICK), SkillType.STAVES);
               yield var11;
            }

            yield var11;
         }
         case WANDS -> {
            ItemStack var9 = explicitlyIdentified(new ItemStack(Material.BLAZE_ROD), SkillType.WANDS);
            yield var9;
         }
         case SPEARS -> {
            ItemStack var8;
            try {
               var8 = new ItemStack(Material.IRON_SPEAR);
            } catch (NoSuchFieldError var5) {
               var8 = new ItemStack(Material.TRIDENT);
               yield var8;
            }

            yield var8;
         }
         case ARMOR -> {
            Object var3 = null;
            yield var3;
         }
      };
      if (weapon != null) {
         this.inventory.setHeldItemSlot(this.heldSlot);
         this.write(this.heldSlot, weapon);
      }
   }

   private static ItemStack explicitlyIdentified(ItemStack itemStack, SkillType skillType) {
      String id = skillType == SkillType.WANDS ? "fmm_default_arcane_wand" : "fmm_default_arcane_staff";
      if (!MagicWeaponAPI.applyWeaponData(itemStack, id)) {
         throw new IllegalStateException("FMM authored weapon is unavailable: " + id);
      } else {
         return itemStack;
      }
   }

   void hold(ItemStack item) {
      this.inventory.setHeldItemSlot(this.heldSlot);
      this.write(this.heldSlot, item);
   }

   void equipArmorSet(int level) {
      this.write(39, this.createEliteArmor(Material.IRON_HELMET, level));
      this.write(38, this.createEliteArmor(Material.IRON_CHESTPLATE, level));
      this.write(37, this.createEliteArmor(Material.IRON_LEGGINGS, level));
      this.write(36, this.createEliteArmor(Material.IRON_BOOTS, level));
   }

   private ItemStack createEliteArmor(Material material, int level) {
      ItemStack armorPiece = new ItemStack(material);
      ItemMeta meta = armorPiece.getItemMeta();
      meta.addEnchant(Enchantment.UNBREAKING, 5, true);
      armorPiece.setItemMeta(meta);
      EliteItemManager.setEliteLevel(armorPiece, level);
      new EliteItemLore(armorPiece, false);
      return armorPiece;
   }

   private static ItemStack cloneItem(ItemStack itemStack) {
      return itemStack == null ? null : itemStack.clone();
   }
}
