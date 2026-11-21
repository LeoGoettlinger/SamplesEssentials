package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class Commandgive extends EssentialsLoopCommand {
   public Commandgive() {
      super("give");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = this.ess.getItemDb().get(args[1]);
         String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         if (sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).canSpawnItem(stack.getType())) {
            throw new TranslatableException("cantSpawnItem", itemname);
         } else {
            try {
               if (args.length > 3 && VersionUtil.PRE_FLATTENING && NumberUtil.isInt(args[2]) && NumberUtil.isInt(args[3])) {
                  stack.setAmount(Integer.parseInt(args[2]));
                  stack.setDurability(Short.parseShort(args[3]));
               } else if (args.length > 2 && Integer.parseInt(args[2]) > 0) {
                  stack.setAmount(Integer.parseInt(args[2]));
               } else if (this.ess.getSettings().getDefaultStackSize() > 0) {
                  stack.setAmount(this.ess.getSettings().getDefaultStackSize());
               } else if (this.ess.getSettings().getOversizedStackSize() > 0 && sender.isAuthorized("essentials.oversizedstacks")) {
                  stack.setAmount(this.ess.getSettings().getOversizedStackSize());
               }
            } catch (NumberFormatException var11) {
               throw new NotEnoughArgumentsException();
            }

            MetaItemStack metaStack = new MetaItemStack(stack);
            if (!metaStack.canSpawn(this.ess)) {
               throw new TranslatableException("unableToSpawnItem", itemname);
            } else {
               if (args.length > 3) {
                  boolean allowUnsafe = this.ess.getSettings().allowUnsafeEnchantments();
                  if (allowUnsafe && sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.enchantments.allowunsafe")) {
                     allowUnsafe = false;
                  }

                  int metaStart = NumberUtil.isInt(args[3]) ? 4 : 3;
                  if (args.length > metaStart) {
                     metaStack.parseStringMeta(sender, allowUnsafe, args, metaStart, this.ess);
                  }

                  stack = metaStack.getItemStack();
               }

               if (stack.getType() == Material.AIR) {
                  throw new TranslatableException("cantSpawnItem", "Air");
               } else {
                  String itemName = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
                  boolean isDropItemsIfFull = this.ess.getSettings().isDropItemsIfFull();
                  ItemStack finalStack = stack;
                  this.loopOnlinePlayersConsumer(
                     server,
                     sender,
                     false,
                     true,
                     args[0],
                     player -> {
                        sender.sendTl("giveSpawn", finalStack.getAmount(), itemName, player.getDisplayName());
                        Map<Integer, ItemStack> leftovers = Inventories.addItem(
                           player.getBase(), player.isAuthorized("essentials.oversizedstacks") ? this.ess.getSettings().getOversizedStackSize() : 0, finalStack
                        );

                        for (ItemStack item : leftovers.values()) {
                           if (isDropItemsIfFull) {
                              World w = player.getWorld();
                              w.dropItemNaturally(player.getLocation(), item);
                           } else {
                              sender.sendTl("giveSpawnFailure", item.getAmount(), itemName, player.getDisplayName());
                           }
                        }

                        player.getBase().updateInventory();
                     }
                  );
               }
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else if (args.length == 2) {
         return this.getItems();
      } else if (args.length == 3) {
         return Lists.newArrayList(new String[]{"1", "64"});
      } else {
         return (List<String>)(args.length == 4 ? Lists.newArrayList(new String[]{"0"}) : Collections.emptyList());
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
   }
}
