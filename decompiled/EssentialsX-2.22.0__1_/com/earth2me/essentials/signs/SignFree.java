package com.earth2me.essentials.signs;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SignFree extends EssentialsSign {
   public SignFree() {
      super("Free");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      try {
         ItemStack item = this.getItemStack(sign.getLine(1), 1, ess);
         item = this.getItemMeta(item, sign.getLine(2), ess);
         item = this.getItemMeta(item, sign.getLine(3), ess);
         return true;
      } catch (SignException var6) {
         sign.setLine(1, "§c<item>");
         throw new SignException(var6, "errorWithMessage", var6.getMessage());
      }
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      ItemStack itemStack = this.getItemStack(sign.getLine(1), 1, ess);
      itemStack = this.getItemMeta(player.getSource(), itemStack, sign.getLine(2), ess);
      ItemStack item = this.getItemMeta(player.getSource(), itemStack, sign.getLine(3), ess);
      if (item.getType() == Material.AIR) {
         throw new SignException("cantSpawnItem", "Air");
      } else {
         item.setAmount(item.getType().getMaxStackSize());
         ItemMeta meta = item.getItemMeta();
         String displayName = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();
         Inventory invent = ess.getServer().createInventory(player.getBase(), 36, displayName);

         for (int i = 0; i < 36; i++) {
            invent.addItem(new ItemStack[]{item});
         }

         player.getBase().openInventory(invent);
         Trade.log("Sign", "Free", "Interact", username, null, username, new Trade(item, ess), sign.getBlock().getLocation(), player.getMoney(), ess);
         return true;
      }
   }
}
