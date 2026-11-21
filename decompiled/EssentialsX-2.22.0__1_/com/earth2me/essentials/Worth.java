package com.earth2me.essentials;

import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.utils.VersionUtil;
import java.io.File;
import java.math.BigDecimal;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Worth implements IConf {
   private final EssentialsConfiguration config;

   public Worth(final File dataFolder) {
      this.config = new EssentialsConfiguration(new File(dataFolder, "worth.yml"), "/worth.yml");
      this.config.load();
   }

   public BigDecimal getPrice(final IEssentials ess, final ItemStack itemStack) {
      BigDecimal result = BigDecimal.ONE.negate();
      String itemname = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
      if (VersionUtil.PRE_FLATTENING) {
         result = this.config.getBigDecimal("worth." + itemname + "." + itemStack.getDurability(), BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         CommentedConfigurationNode itemNameMatch = this.config.getSection("worth." + itemname);
         if (itemNameMatch != null && ConfigurateUtil.getKeys(itemNameMatch).size() == 1) {
            result = this.config.getBigDecimal("worth." + itemname + ".0", BigDecimal.ONE.negate());
         }
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemname + ".*", BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemname, BigDecimal.ONE.negate());
      }

      return result.signum() < 0 ? null : result;
   }

   public int getAmount(final IEssentials ess, final User user, final ItemStack is, final String[] args, final boolean isBulkSell) throws Exception {
      if (is != null && is.getType() != Material.AIR) {
         int amount = 0;
         if (args.length > 1) {
            try {
               amount = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException var14) {
               throw new NotEnoughArgumentsException(var14);
            }

            if (args[1].startsWith("-")) {
               amount = -amount;
            }
         }

         boolean stack = args.length > 1 && args[1].endsWith("s");
         boolean requireStack = ess.getSettings().isTradeInStacks(is.getType());
         if (requireStack && !stack) {
            throw new TranslatableException("itemMustBeStacked");
         } else {
            int max = 0;

            for (ItemStack s : Inventories.getInventory(user.getBase(), false)) {
               if (s != null && s.isSimilar(is)) {
                  max += s.getAmount();
               }
            }

            if (stack) {
               amount *= is.getType().getMaxStackSize();
            }

            if (amount < 1) {
               amount += max;
            }

            if (requireStack) {
               amount -= amount % is.getType().getMaxStackSize();
            }

            if (amount <= max && amount >= 1) {
               return amount;
            } else if (!isBulkSell) {
               user.sendTl("itemNotEnough2");
               user.sendTl("itemNotEnough3");
               throw new TranslatableException("itemNotEnough1");
            } else {
               return amount;
            }
         }
      } else {
         throw new TranslatableException("itemSellAir");
      }
   }

   public void setPrice(final IEssentials ess, final ItemStack itemStack, final double price) {
      String path = "worth." + itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
      if (VersionUtil.PRE_FLATTENING && itemStack.getType().getData() == null) {
         path = path + "." + itemStack.getDurability();
      }

      this.config.setProperty(path, price);
      this.config.save();
   }

   public File getFile() {
      return this.config.getFile();
   }

   @Override
   public void reloadConfig() {
      this.config.load();
   }
}
