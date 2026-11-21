package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaItemDurabilityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

final class EnchantmentItemAccess {
   private final UUID actor;
   private final int slot;
   private final ItemStack expected;

   EnchantmentItemAccess(UUID actor, int slot, ItemStack expected) {
      if (slot >= 0 && slot <= 40) {
         this.actor = actor;
         this.slot = slot;
         this.expected = expected.clone();
      } else {
         throw new IllegalArgumentException("Invalid player inventory slot");
      }
   }

   LuaTable table() {
      LuaTable table = new LuaTable();
      LuaItemDurabilityTable.attach(table, this::read, this::update);
      table.set("consume", LuaTableSupport.tableMethod(table, args -> {
         int amount = args.checkint(1);
         if (amount < 1) {
            throw new IllegalArgumentException("Consumption must be positive");
         } else {
            return LuaValue.valueOf(this.update(stack -> {
               if (stack.getAmount() < amount) {
                  return null;
               } else {
                  ItemStack changed = stack.clone();
                  changed.setAmount(changed.getAmount() - amount);
                  return changed;
               }
            }));
         }
      }));
      return table;
   }

   boolean isEquipped() {
      return Bukkit.getEntity(this.actor) instanceof Player player && this.read() != null
         ? this.slot >= 36 || this.slot == player.getInventory().getHeldItemSlot()
         : false;
   }

   private ItemStack read() {
      if (Bukkit.getEntity(this.actor) instanceof Player player && player.isOnline()) {
         ItemStack current = player.getInventory().getItem(this.slot);
         return this.sameItem(current) ? current.clone() : null;
      } else {
         return null;
      }
   }

   private boolean update(UnaryOperator<ItemStack> change) {
      ItemStack current = this.read();
      if (current == null) {
         return false;
      } else {
         ItemStack changed = change.apply(current);
         if (changed == null) {
            return false;
         } else {
            Player player = (Player)Bukkit.getEntity(this.actor);
            ItemStack live = player.getInventory().getItem(this.slot);
            live.setAmount(changed.getAmount());
            if (changed.getAmount() > 0) {
               live.setItemMeta(changed.getItemMeta());
            }

            return true;
         }
      }
   }

   private boolean sameItem(ItemStack current) {
      if (current != null && current.getAmount() > 0 && current.getType() == this.expected.getType()) {
         ItemStack first = current.clone();
         ItemStack second = this.expected.clone();
         clearDamage(first);
         clearDamage(second);
         return first.isSimilar(second);
      } else {
         return false;
      }
   }

   private static void clearDamage(ItemStack item) {
      if (item.getItemMeta() instanceof Damageable meta) {
         meta.setDamage(0);
         item.setItemMeta(meta);
      }
   }
}
