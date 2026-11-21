package eu.raidersheaven.rhsignitem.commands.rename;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.commands.sign.Help;
import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import eu.raidersheaven.rhsignitem.handlers.ColorFormatHandler;
import eu.raidersheaven.rhsignitem.handlers.LocaleHandler;
import eu.raidersheaven.rhsignitem.handlers.PAPIHandler;
import eu.raidersheaven.rhsignitem.handlers.SoundHandler;
import eu.raidersheaven.rhsignitem.handlers.VaultMoneyHandler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Rename {
   public static ArgumentCommandNode<CommandSourceStack, String> register(Main plugin) {
      return ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("", StringArgumentType.greedyString())
               .requires(
                  stack -> stack.getSender().hasPermission("RHSignItem.*")
                     || stack.getSender().hasPermission("RHSignItem.command.*")
                     || stack.getSender().hasPermission("RHSignItem.command.rename")
               ))
            .executes(Rename::renameItem))
         .build();
   }

   private static int renameItem(CommandContext<CommandSourceStack> context) {
      if (((CommandSourceStack)context.getSource()).getSender() instanceof Player player) {
         if (VaultMoneyHandler.shouldCancelStatus(player, () -> ConfigFile.vaultActionsRename)) {
            return 0;
         } else {
            ItemStack item = player.getInventory().getItemInMainHand();
            String argText = (String)context.getArgument("", String.class);
            if (!item.isEmpty() && !item.getType().isAir()) {
               ItemMeta meta = item.getItemMeta();
               if (meta == null) {
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(
                        PAPIHandler.parse(player, LocaleHandler.noItem.replace("%prefix%", ColorFormatHandler.formatString(LocaleHandler.prefix)))
                     )
                  );
                  SoundHandler.fail(player);
                  return 0;
               } else if (VaultMoneyHandler.shouldCancelPayment(player, () -> ConfigFile.vaultActionsRename)) {
                  return 0;
               } else {
                  meta.displayName(ColorFormatHandler.formatComponent(PAPIHandler.parse(player, ColorFormatHandler.formatString(argText))));
                  item.setItemMeta(meta);
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(
                        PAPIHandler.parse(player, LocaleHandler.itemRenamed.replace("%prefix%", ColorFormatHandler.formatString(LocaleHandler.prefix)))
                     )
                  );
                  SoundHandler.success(player);
                  return 1;
               }
            } else {
               player.sendMessage(
                  ColorFormatHandler.formatComponent(
                     PAPIHandler.parse(player, LocaleHandler.noItem.replace("%prefix%", ColorFormatHandler.formatString(LocaleHandler.prefix)))
                  )
               );
               SoundHandler.fail(player);
               return 0;
            }
         }
      } else {
         Help.noConsoleCommand(context);
         return 0;
      }
   }
}
