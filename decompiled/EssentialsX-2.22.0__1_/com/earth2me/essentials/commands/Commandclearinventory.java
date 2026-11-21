package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commandclearinventory extends EssentialsCommand {
   private static final int EXTENDED_CAP = 8;

   public Commandclearinventory() {
      super("clearinventory");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      this.parseCommand(
         server,
         user.getSource(),
         commandLabel,
         args,
         user.isAuthorized("essentials.clearinventory.others"),
         user.isAuthorized("essentials.clearinventory.all") || user.isAuthorized("essentials.clearinventory.multiple")
      );
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      this.parseCommand(server, sender, commandLabel, args, true, true);
   }

   private void parseCommand(
      final Server server, final CommandSource sender, final String commandLabel, final String[] args, final boolean allowOthers, final boolean allowAll
   ) throws Exception {
      Collection<Player> players = new ArrayList<>();
      User senderUser = this.ess.getUser(sender.getPlayer());
      String previousClearCommand = "";
      int offset = 0;
      if (sender.isPlayer()) {
         players.add(sender.getPlayer());
         previousClearCommand = senderUser.getConfirmingClearCommand();
         senderUser.setConfirmingClearCommand(null);
      }

      if (allowAll && args.length > 0 && args[0].contentEquals("*")) {
         sender.sendTl("inventoryClearingFromAll");
         offset = 1;
         players = this.ess.getOnlinePlayers();
      } else if (allowOthers && args.length > 0 && args[0].trim().length() > 2) {
         offset = 1;
         players = server.matchPlayer(args[0].trim());
      }

      if (players.size() < 1) {
         throw new PlayerNotFoundException();
      } else {
         String formattedCommand = this.formatCommand(commandLabel, args);
         if (senderUser != null && senderUser.isPromptingClearConfirm() && !formattedCommand.equals(previousClearCommand)) {
            senderUser.setConfirmingClearCommand(formattedCommand);
            senderUser.sendTl("confirmClear", formattedCommand);
         } else {
            for (Player player : players) {
               this.clearHandler(sender, player, args, offset, players.size() < 8);
            }
         }
      }
   }

   protected void clearHandler(final CommandSource sender, final Player player, final String[] args, final int offset, final boolean showExtended) throws TranslatableException {
      Commandclearinventory.ClearHandlerType type = Commandclearinventory.ClearHandlerType.ALL_EXCEPT_ARMOR;
      Set<Commandclearinventory.Item> items = new HashSet<>();
      int amount = -1;
      if (args.length > offset + 1 && NumberUtil.isInt(args[offset + 1])) {
         amount = Integer.parseInt(args[offset + 1]);
      }

      if (args.length > offset) {
         if (args[offset].equalsIgnoreCase("**")) {
            type = Commandclearinventory.ClearHandlerType.ALL_INCLUDING_ARMOR;
         } else if (!args[offset].equalsIgnoreCase("*")) {
            String[] split = args[offset].split(",");

            for (String item : split) {
               String[] itemParts = item.split(":");

               short data;
               try {
                  data = Short.parseShort(itemParts[1]);
               } catch (Exception var18) {
                  data = 0;
               }

               try {
                  items.add(new Commandclearinventory.Item(this.ess.getItemDb().get(itemParts[0]).getType(), data));
               } catch (Exception var17) {
               }
            }

            type = Commandclearinventory.ClearHandlerType.SPECIFIC_ITEM;
         }
      }

      if (type != Commandclearinventory.ClearHandlerType.SPECIFIC_ITEM) {
         boolean armor = type == Commandclearinventory.ClearHandlerType.ALL_INCLUDING_ARMOR;
         if (showExtended) {
            sender.sendTl(armor ? "inventoryClearingAllArmor" : "inventoryClearingAllItems", player.getDisplayName());
         }

         Inventories.removeItems(player, itemx -> true, armor);
      } else {
         for (Commandclearinventory.Item item : items) {
            ItemStack stack = new ItemStack(item.getMaterial());
            if (VersionUtil.PRE_FLATTENING) {
               stack.setDurability(item.getData());
            }

            if (amount < -1) {
               throw new TranslatableException("cannotRemoveNegativeItems");
            }

            if (amount == -1) {
               int removedAmount = Inventories.removeItemSimilar(player, stack, true);
               if (removedAmount > 0 || showExtended) {
                  sender.sendTl("inventoryClearingStack", removedAmount, stack.getType().toString().toLowerCase(Locale.ENGLISH), player.getDisplayName());
               }
            } else if (Inventories.removeItemAmount(player, stack, amount)) {
               sender.sendTl("inventoryClearingStack", amount, stack.getType().toString().toLowerCase(Locale.ENGLISH), player.getDisplayName());
            } else if (showExtended) {
               sender.sendTl("inventoryClearFail", player.getDisplayName(), amount, stack.getType().toString().toLowerCase(Locale.ENGLISH));
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (user.isAuthorized("essentials.clearinventory.others")) {
         if (args.length != 1) {
            if (args.length == 2) {
               List<String> items = new ArrayList<>(this.getItems());
               items.add("*");
               items.add("**");
               return items;
            } else {
               return Collections.emptyList();
            }
         } else {
            List<String> options = this.getPlayers(user);
            if (user.isAuthorized("essentials.clearinventory.all") || user.isAuthorized("essentials.clearinventory.multiple")) {
               options.add("*");
            }

            return options;
         }
      } else if (args.length == 1) {
         List<String> items = new ArrayList<>(this.getItems());
         items.add("*");
         items.add("**");
         return items;
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = this.getPlayers(sender);
         options.add("*");
         return options;
      } else if (args.length == 2) {
         List<String> items = new ArrayList<>(this.getItems());
         items.add("*");
         items.add("**");
         return items;
      } else {
         return Collections.emptyList();
      }
   }

   private String formatCommand(final String commandLabel, final String[] args) {
      return args != null && args.length != 0 ? "/" + commandLabel + " " + StringUtil.joinList(" ", args) : "/" + commandLabel;
   }

   private static enum ClearHandlerType {
      ALL_EXCEPT_ARMOR,
      ALL_INCLUDING_ARMOR,
      SPECIFIC_ITEM;
   }

   private static class Item {
      private final Material material;
      private final short data;

      Item(final Material material, final short data) {
         this.material = material;
         this.data = data;
      }

      public Material getMaterial() {
         return this.material;
      }

      public short getData() {
         return this.data;
      }
   }
}
