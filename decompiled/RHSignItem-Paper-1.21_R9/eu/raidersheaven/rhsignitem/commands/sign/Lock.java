package eu.raidersheaven.rhsignitem.commands.sign;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import eu.raidersheaven.rhsignitem.handlers.ColorFormatHandler;
import eu.raidersheaven.rhsignitem.handlers.LocaleHandler;
import eu.raidersheaven.rhsignitem.handlers.PAPIHandler;
import eu.raidersheaven.rhsignitem.handlers.SoundHandler;
import eu.raidersheaven.rhsignitem.handlers.VaultMoneyHandler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Lock {
   public static LiteralArgumentBuilder<CommandSourceStack> register(Main plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("lock")
            .requires(
               stack -> stack.getSender().hasPermission("RHSignItem.*")
                  || stack.getSender().hasPermission("RHSignItem.command.*")
                  || stack.getSender().hasPermission("RHSignItem.command.sign.lock")
            ))
         .executes(Lock::lockItem);
   }

   private static int lockItem(CommandContext<CommandSourceStack> context) {
      if (((CommandSourceStack)context.getSource()).getSender() instanceof Player player) {
         if (VaultMoneyHandler.shouldCancelStatus(player, () -> ConfigFile.vaultActionsLock)) {
            return 0;
         } else {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.isEmpty() && !item.getType().isAir()) {
               ItemMeta meta = item.getItemMeta();
               PersistentDataContainer data = meta.getPersistentDataContainer();
               NamespacedKey lockedKey = new NamespacedKey(Main.get(), "locked");
               NamespacedKey lockedByKey = new NamespacedKey(Main.get(), "locked-by");
               if (data.has(lockedKey, PersistentDataType.BOOLEAN)) {
                  String lockedOwnerR8 = Bukkit.getOfflinePlayer(
                        UUID.fromString(Objects.requireNonNull((String)data.get(lockedByKey, PersistentDataType.STRING)))
                     )
                     .getName();
                  String lockedByUUID = (String)data.get(lockedByKey, PersistentDataType.STRING);
                  if (lockedByUUID != null && lockedByUUID.equals(player.getUniqueId().toString())) {
                     player.sendMessage(
                        ColorFormatHandler.formatComponent(
                           PAPIHandler.parse(
                              player,
                              LocaleHandler.itemAlreadyLockedByPlayer
                                 .replace("%prefix%", LocaleHandler.prefix)
                                 .replace("%player%", ColorFormatHandler.formatString(lockedOwnerR8))
                           )
                        )
                     );
                  } else {
                     player.sendMessage(
                        ColorFormatHandler.formatComponent(
                           PAPIHandler.parse(
                              player,
                              LocaleHandler.itemAlreadyLocked
                                 .replace("%prefix%", LocaleHandler.prefix)
                                 .replace("%player%", ColorFormatHandler.formatString(lockedOwnerR8))
                           )
                        )
                     );
                  }

                  SoundHandler.fail(player);
                  return 0;
               } else if (VaultMoneyHandler.shouldCancelPayment(player, () -> ConfigFile.vaultActionsLock)) {
                  return 0;
               } else {
                  data.set(lockedKey, PersistentDataType.BOOLEAN, true);
                  data.set(lockedByKey, PersistentDataType.STRING, player.getUniqueId().toString());
                  item.setItemMeta(meta);
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.itemLocked.replace("%prefix%", LocaleHandler.prefix)))
                  );
                  SoundHandler.success(player);
                  return 1;
               }
            } else {
               player.sendMessage(ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.noItem.replace("%prefix%", LocaleHandler.prefix))));
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
