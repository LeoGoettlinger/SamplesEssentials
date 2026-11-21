package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Enchantments;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import java.util.Locale;
import net.ess3.api.IEssentials;
import net.ess3.provider.MaterialTagProvider;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class SignEnchant extends EssentialsSign {
   public SignEnchant() {
      super("Enchant");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      String itemName = sign.getLine(1);
      MaterialTagProvider tagProvider = ess.provider(MaterialTagProvider.class);

      ItemStack stack;
      try {
         stack = !itemName.equals("*") && !itemName.equalsIgnoreCase("any") && (tagProvider == null || !tagProvider.tagExists(itemName))
            ? this.getItemStack(sign.getLine(1), 1, ess)
            : null;
      } catch (SignException var15) {
         sign.setLine(1, "§c<item|any>");
         throw var15;
      }

      String[] enchantLevel = sign.getLine(2).split(":");
      int level = 1;
      Enchantment enchantment = Enchantments.getByName(enchantLevel[0]);
      if (enchantment == null) {
         sign.setLine(2, "§c<enchant>");
         throw new SignException("enchantmentNotFound");
      } else {
         if (enchantLevel.length > 1) {
            try {
               level = Integer.parseInt(enchantLevel[1]);
            } catch (NumberFormatException var13) {
               sign.setLine(2, "§c<enchant>");
               throw new SignException(var13, "errorWithMessage", var13.getMessage());
            }
         }

         boolean allowUnsafe = ess.getSettings().allowUnsafeEnchantments()
            && player.isAuthorized("essentials.enchantments.allowunsafe")
            && player.isAuthorized("essentials.signs.enchant.allowunsafe");
         if (level < 0 || !allowUnsafe && level > enchantment.getMaxLevel()) {
            level = enchantment.getMaxLevel();
            sign.setLine(2, enchantLevel[0] + ":" + level);
         }

         try {
            if (stack != null) {
               if (allowUnsafe) {
                  stack.addUnsafeEnchantment(enchantment, level);
               } else {
                  stack.addEnchantment(enchantment, level);
               }
            }
         } catch (Throwable var14) {
            throw new SignException(var14, "errorWithMessage", var14.getMessage());
         }

         this.getTrade(sign, 3, ess);
         return true;
      }
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      ItemStack playerHand = Inventories.getItemInHand(player.getBase());
      MaterialTagProvider tagProvider = ess.provider(MaterialTagProvider.class);
      String itemName = sign.getLine(1);
      ItemStack search = !itemName.equals("*")
            && !itemName.equalsIgnoreCase("any")
            && (tagProvider == null || !tagProvider.tagExists(itemName) || !tagProvider.isTagged(itemName, playerHand.getType()))
         ? this.getItemStack(itemName, 1, ess)
         : null;
      Trade charge = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);
      String[] enchantLevel = sign.getLine(2).split(":");
      Enchantment enchantment = Enchantments.getByName(enchantLevel[0]);
      if (enchantment == null) {
         throw new SignException("enchantmentNotFound");
      } else {
         int level = 1;
         if (enchantLevel.length > 1) {
            try {
               level = Integer.parseInt(enchantLevel[1]);
            } catch (NumberFormatException var14) {
               throw new SignException(var14, "errorWithMessage", var14.getMessage());
            }
         }

         if (playerHand != null
            && playerHand.getAmount() == 1
            && (!playerHand.containsEnchantment(enchantment) || playerHand.getEnchantmentLevel(enchantment) != level)) {
            if (search != null && playerHand.getType() != search.getType()) {
               throw new SignException("missingItems", 1, search.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' '));
            } else {
               try {
                  if (level == 0) {
                     playerHand.removeEnchantment(enchantment);
                  } else if (ess.getSettings().allowUnsafeEnchantments() && player.isAuthorized("essentials.signs.enchant.allowunsafe")) {
                     playerHand.addUnsafeEnchantment(enchantment, level);
                  } else {
                     playerHand.addEnchantment(enchantment, level);
                  }
               } catch (Exception var15) {
                  throw new SignException(var15, "errorWithMessage", var15.getMessage());
               }

               String enchantmentName = Enchantments.getRealName(enchantment);
               if (level == 0) {
                  player.sendTl("enchantmentRemoved", enchantmentName.replace('_', ' '));
               } else {
                  player.sendTl("enchantmentApplied", enchantmentName.replace('_', ' '));
               }

               charge.charge(player);
               Trade.log("Sign", "Enchant", "Interact", username, charge, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
               player.getBase().updateInventory();
               return true;
            }
         } else {
            throw new SignException("missingItems", 1, sign.getLine(1));
         }
      }
   }
}
