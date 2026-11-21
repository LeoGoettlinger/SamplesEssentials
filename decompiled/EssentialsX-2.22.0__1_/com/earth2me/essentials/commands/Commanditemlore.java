package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Commanditemlore extends EssentialsCommand {
   public Commanditemlore() {
      super("itemlore");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      ItemStack item = Inventories.getItemInHand(user.getBase());
      if (item == null || MaterialUtil.isAir(item.getType())) {
         throw new TranslatableException("itemloreInvalidItem");
      } else if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemMeta im = item.getItemMeta();
         int loreSize = im.hasLore() ? im.getLore().size() : 0;
         if (args[0].equalsIgnoreCase("add") && args.length > 1) {
            if (loreSize >= this.ess.getSettings().getMaxItemLore() && !user.isAuthorized("essentials.itemlore.bypass")) {
               throw new TranslatableException("itemloreMaxLore");
            }

            String line = FormatUtil.formatString(user, "essentials.itemlore", getFinalArg(args, 1)).trim();
            List<String> lore = (List<String>)(im.hasLore() ? im.getLore() : new ArrayList<>());
            lore.add(line);
            im.setLore(lore);
            item.setItemMeta(im);
            user.sendTl("itemloreSuccess", line);
         } else if (args[0].equalsIgnoreCase("clear")) {
            im.setLore(new ArrayList());
            item.setItemMeta(im);
            user.sendTl("itemloreClear");
         } else {
            if (!args[0].equalsIgnoreCase("set") || args.length <= 2 || !NumberUtil.isInt(args[1])) {
               throw new NotEnoughArgumentsException();
            }

            if (!im.hasLore()) {
               throw new TranslatableException("itemloreNoLore");
            }

            int line = Integer.parseInt(args[1]);
            String newLine = FormatUtil.formatString(user, "essentials.itemlore", getFinalArg(args, 2)).trim();
            List<String> lore = im.getLore();

            try {
               lore.set(line - 1, newLine);
            } catch (Exception var12) {
               throw new TranslatableException(var12, "itemloreNoLine", line);
            }

            im.setLore(lore);
            item.setItemMeta(im);
            user.sendTl("itemloreSuccessLore", line, newLine);
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"add", "set", "clear"});
      } else if (args.length == 2) {
         String var10 = args[0].toLowerCase(Locale.ENGLISH);
         switch (var10) {
            case "set":
               ItemStack item = Inventories.getItemInHand(user.getBase());
               if (item != null && !MaterialUtil.isAir(item.getType()) && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                  List<String> lineNumbers = new ArrayList<>();

                  for (int i = 1; i <= item.getItemMeta().getLore().size(); i++) {
                     lineNumbers.add(String.valueOf(i));
                  }

                  return lineNumbers;
               } else {
                  return Collections.emptyList();
               }
            case "clear":
            case "add":
            default:
               return Collections.emptyList();
         }
      } else if (args.length == 3) {
         if (args[0].equalsIgnoreCase("set") && NumberUtil.isInt(args[1])) {
            int i = Integer.parseInt(args[1]);
            ItemStack item = Inventories.getItemInHand(user.getBase());
            if (item != null
               && !MaterialUtil.isAir(item.getType())
               && item.hasItemMeta()
               && item.getItemMeta().hasLore()
               && item.getItemMeta().getLore().size() >= i) {
               return Lists.newArrayList(new String[]{FormatUtil.unformatString(user, "essentials.itemlore", (String)item.getItemMeta().getLore().get(i - 1))});
            }
         }

         return Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }
}
