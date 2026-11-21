package eu.raidersheaven.rhsignitem.commands.sign;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import eu.raidersheaven.rhsignitem.handlers.ColorFormatHandler;
import eu.raidersheaven.rhsignitem.handlers.DateFormatHandler;
import eu.raidersheaven.rhsignitem.handlers.LocaleHandler;
import eu.raidersheaven.rhsignitem.handlers.PAPIHandler;
import eu.raidersheaven.rhsignitem.handlers.SoundHandler;
import eu.raidersheaven.rhsignitem.handlers.VaultMoneyHandler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Sign {
   public static ArgumentCommandNode<CommandSourceStack, String> register(Main plugin) {
      return ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("", StringArgumentType.greedyString())
               .requires(
                  stack -> stack.getSender().hasPermission("RHSignItem.*")
                     || stack.getSender().hasPermission("RHSignItem.command.*")
                     || stack.getSender().hasPermission("RHSignItem.command.sign.sign")
               ))
            .executes(Sign::signItem))
         .build();
   }

   private static List<Component> setLoreDefault(Player player, String text) {
      String argumentString = PAPIHandler.parse(player, text).replace("\\n", "\n").replace("\\\n", "\n").replace("<br>", "\n").replace("<newline>", "\n");
      Pattern formatPattern = Pattern.compile("(<[^>]+>|&[0-9a-fk-or])");
      List<String> processedLines = new ArrayList<>();
      String lastFormat = "";

      for (String line : ConfigFile.defaultContent) {
         String parsedLine = PAPIHandler.parse(player, line)
            .replace("%text%", argumentString)
            .replace("%player%", player.getName())
            .replace("%date%", DateFormatHandler.getCurrentDate(ConfigFile.formatsDate))
            .replace("%time%", DateFormatHandler.getCurrentDate(ConfigFile.formatsTime));
         String[] splitLines = parsedLine.split("\n");

         for (int i = 0; i < splitLines.length; i++) {
            Matcher matcher = formatPattern.matcher(splitLines[i]);

            while (matcher.find()) {
               lastFormat = matcher.group();
            }

            if (i > 0) {
               splitLines[i] = lastFormat + splitLines[i];
            }

            processedLines.add(splitLines[i]);
         }
      }

      return ColorFormatHandler.formatStringList(processedLines);
   }

   private static List<Component> setLoreFast(Player player) {
      Pattern formatPattern = Pattern.compile("(<[^>]+>|&[0-9a-fk-or])");
      List<String> processedLines = new ArrayList<>();
      String lastFormat = "";

      for (String line : ConfigFile.shortContent) {
         String parsedLine = PAPIHandler.parse(player, line)
            .replace("%player%", player.getName())
            .replace("%date%", DateFormatHandler.getCurrentDate(ConfigFile.formatsDate))
            .replace("%time%", DateFormatHandler.getCurrentDate(ConfigFile.formatsTime));
         String[] splitLines = parsedLine.split("\n");

         for (int i = 0; i < splitLines.length; i++) {
            Matcher matcher = formatPattern.matcher(splitLines[i]);

            while (matcher.find()) {
               lastFormat = matcher.group();
            }

            if (i > 0) {
               splitLines[i] = lastFormat + splitLines[i];
            }

            processedLines.add(splitLines[i]);
         }
      }

      return ColorFormatHandler.formatStringList(processedLines);
   }

   private static void setLoreLineCount(ItemMeta meta, int addedLines) {
      PersistentDataContainer data = meta.getPersistentDataContainer();
      NamespacedKey key = new NamespacedKey(Main.get(), "lines");
      int currentCount = (Integer)data.getOrDefault(key, PersistentDataType.INTEGER, 0);
      data.set(key, PersistentDataType.INTEGER, currentCount + addedLines);
   }

   private static int signItem(CommandContext<CommandSourceStack> context) {
      if (((CommandSourceStack)context.getSource()).getSender() instanceof Player player) {
         if (VaultMoneyHandler.shouldCancelStatus(player, () -> ConfigFile.vaultActionsSign)) {
            return 0;
         } else {
            ItemStack item = player.getInventory().getItemInMainHand();
            int stackSize = ConfigFile.maxStackSize;
            if (!item.isEmpty() && !item.getType().isAir()) {
               if (stackSize <= 0 || stackSize > 64) {
                  stackSize = 64;
               }

               if (item.getAmount() > stackSize) {
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(
                        PAPIHandler.parse(
                           player,
                           LocaleHandler.aboveStackLimit
                              .replace("%prefix%", LocaleHandler.prefix)
                              .replace("%stacksize%", ColorFormatHandler.formatString(String.valueOf(stackSize)))
                        )
                     )
                  );
                  SoundHandler.fail(player);
                  return 0;
               } else if (ConfigFile.itemBlacklist.stream().map(String::toUpperCase).anyMatch(itemType -> itemType.equals(item.getType().toString()))) {
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.itemBlacklisted.replace("%prefix%", LocaleHandler.prefix)))
                  );
                  SoundHandler.fail(player);
                  return 0;
               } else {
                  ItemMeta meta = item.getItemMeta();
                  if (meta == null) {
                     player.sendMessage(
                        ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.noItem.replace("%prefix%", LocaleHandler.prefix)))
                     );
                     SoundHandler.fail(player);
                     return 0;
                  } else {
                     PersistentDataContainer data = meta.getPersistentDataContainer();
                     NamespacedKey signedKey = new NamespacedKey(Main.get(), "signed");
                     NamespacedKey ownerKey = new NamespacedKey(Main.get(), "owner");
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
                                    LocaleHandler.itemIsLockedByPlayer
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
                                    LocaleHandler.itemIsLocked
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(lockedOwnerR8))
                                 )
                              )
                           );
                        }

                        SoundHandler.fail(player);
                        return 0;
                     } else if (data.has(signedKey, PersistentDataType.BOOLEAN)) {
                        String ownerNameR8 = Bukkit.getOfflinePlayer(
                              UUID.fromString(Objects.requireNonNull((String)data.get(ownerKey, PersistentDataType.STRING)))
                           )
                           .getName();
                        String ownerUUID = (String)data.get(ownerKey, PersistentDataType.STRING);
                        if (ownerUUID != null && ownerUUID.equals(player.getUniqueId().toString())) {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(
                                    player,
                                    LocaleHandler.itemAlreadySignedByPlayer
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(ownerNameR8))
                                 )
                              )
                           );
                        } else {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(
                                    player,
                                    LocaleHandler.itemAlreadySigned
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(ownerNameR8))
                                 )
                              )
                           );
                        }

                        SoundHandler.fail(player);
                        return 0;
                     } else if (ConfigFile.offhandItemModeEnabled && !offhandHandler(player)) {
                        return 0;
                     } else {
                        List<Component> existingLore = meta.lore();
                        if (existingLore == null) {
                           existingLore = new ArrayList<>();
                        } else if (ConfigFile.denyItemsWithLore) {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(player, LocaleHandler.itemContainsLore.replace("%prefix%", LocaleHandler.prefix))
                              )
                           );
                           SoundHandler.fail(player);
                           return 0;
                        }

                        String text = StringArgumentType.getString(context, "");
                        List<Component> newLore = setLoreDefault(player, text);
                        existingLore.addAll(newLore);
                        meta.lore(existingLore);
                        if (VaultMoneyHandler.shouldCancelPayment(player, () -> ConfigFile.vaultActionsSign)) {
                           return 0;
                        } else {
                           data.set(new NamespacedKey(Main.get(), "signed"), PersistentDataType.BOOLEAN, true);
                           data.set(new NamespacedKey(Main.get(), "owner"), PersistentDataType.STRING, player.getUniqueId().toString());
                           setLoreLineCount(meta, newLore.size());
                           item.setItemMeta(meta);
                           autoLock(player);
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.itemSigned.replace("%prefix%", LocaleHandler.prefix)))
                           );
                           SoundHandler.success(player);
                           return 1;
                        }
                     }
                  }
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

   public static int fastSignItem(CommandContext<CommandSourceStack> context) {
      if (((CommandSourceStack)context.getSource()).getSender() instanceof Player player) {
         if (VaultMoneyHandler.shouldCancelStatus(player, () -> ConfigFile.vaultActionsSign)) {
            return 0;
         } else {
            ItemStack item = player.getInventory().getItemInMainHand();
            int stackSize = ConfigFile.maxStackSize;
            if (!item.isEmpty() && !item.getType().isAir()) {
               if (stackSize <= 0 || stackSize > 64) {
                  stackSize = 64;
               }

               if (item.getAmount() > stackSize) {
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(
                        PAPIHandler.parse(
                           player,
                           LocaleHandler.aboveStackLimit
                              .replace("%prefix%", LocaleHandler.prefix)
                              .replace("%stacksize%", ColorFormatHandler.formatString(String.valueOf(stackSize)))
                        )
                     )
                  );
                  SoundHandler.fail(player);
                  return 0;
               } else if (ConfigFile.itemBlacklist.stream().map(String::toUpperCase).anyMatch(itemType -> itemType.equals(item.getType().toString()))) {
                  player.sendMessage(
                     ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.itemBlacklisted.replace("%prefix%", LocaleHandler.prefix)))
                  );
                  SoundHandler.fail(player);
                  return 0;
               } else {
                  ItemMeta meta = item.getItemMeta();
                  if (meta == null) {
                     player.sendMessage(
                        ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.noItem.replace("%prefix%", LocaleHandler.prefix)))
                     );
                     SoundHandler.fail(player);
                     return 0;
                  } else {
                     PersistentDataContainer data = meta.getPersistentDataContainer();
                     NamespacedKey signedKey = new NamespacedKey(Main.get(), "signed");
                     NamespacedKey ownerKey = new NamespacedKey(Main.get(), "owner");
                     NamespacedKey lockedKey = new NamespacedKey(Main.get(), "locked");
                     NamespacedKey lockedByKey = new NamespacedKey(Main.get(), "locked-by");
                     if (data.has(lockedKey, PersistentDataType.BOOLEAN)) {
                        String lockedOwner = Objects.requireNonNull(
                              Bukkit.getPlayer(
                                 UUID.fromString(
                                    Objects.requireNonNull((String)data.get(new NamespacedKey(Main.get(), "locked-by"), PersistentDataType.STRING))
                                 )
                              )
                           )
                           .getName();
                        String lockedByUUID = (String)data.get(lockedByKey, PersistentDataType.STRING);
                        if (lockedByUUID != null && lockedByUUID.equals(player.getUniqueId().toString())) {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(
                                    player,
                                    LocaleHandler.itemIsLockedByPlayer
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(lockedOwner))
                                 )
                              )
                           );
                        } else {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(
                                    player,
                                    LocaleHandler.itemIsLocked
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(lockedOwner))
                                 )
                              )
                           );
                        }

                        SoundHandler.fail(player);
                        return 0;
                     } else if (data.has(signedKey, PersistentDataType.BOOLEAN)) {
                        String ownerName = Objects.requireNonNull(
                              Bukkit.getPlayer(UUID.fromString(Objects.requireNonNull((String)data.get(ownerKey, PersistentDataType.STRING))))
                           )
                           .getName();
                        String ownerUUID = (String)data.get(ownerKey, PersistentDataType.STRING);
                        if (ownerUUID != null && ownerUUID.equals(player.getUniqueId().toString())) {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(
                                    player,
                                    LocaleHandler.itemAlreadySignedByPlayer
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(ownerName))
                                 )
                              )
                           );
                        } else {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(
                                    player,
                                    LocaleHandler.itemAlreadySigned
                                       .replace("%prefix%", LocaleHandler.prefix)
                                       .replace("%player%", ColorFormatHandler.formatString(ownerName))
                                 )
                              )
                           );
                        }

                        SoundHandler.fail(player);
                        return 0;
                     } else if (ConfigFile.offhandItemModeEnabled && !offhandHandler(player)) {
                        return 0;
                     } else {
                        List<Component> existingLore = meta.lore();
                        if (existingLore == null) {
                           existingLore = new ArrayList<>();
                        } else if (ConfigFile.denyItemsWithLore) {
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(
                                 PAPIHandler.parse(player, LocaleHandler.itemContainsLore.replace("%prefix%", LocaleHandler.prefix))
                              )
                           );
                           SoundHandler.fail(player);
                           return 0;
                        }

                        List<Component> newLore = setLoreFast(player);
                        existingLore.addAll(newLore);
                        meta.lore(existingLore);
                        if (VaultMoneyHandler.shouldCancelPayment(player, () -> ConfigFile.vaultActionsSign)) {
                           return 0;
                        } else {
                           data.set(new NamespacedKey(Main.get(), "signed"), PersistentDataType.BOOLEAN, true);
                           data.set(new NamespacedKey(Main.get(), "owner"), PersistentDataType.STRING, player.getUniqueId().toString());
                           setLoreLineCount(meta, newLore.size());
                           item.setItemMeta(meta);
                           autoLock(player);
                           player.sendMessage(
                              ColorFormatHandler.formatComponent(PAPIHandler.parse(player, LocaleHandler.itemSigned.replace("%prefix%", LocaleHandler.prefix)))
                           );
                           SoundHandler.success(player);
                           return 1;
                        }
                     }
                  }
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

   private static void autoLock(Player player) {
      if (ConfigFile.autoLockEnabled) {
         ItemStack item = player.getInventory().getItemInMainHand();
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey lockedKey = new NamespacedKey(Main.get(), "locked");
            NamespacedKey lockedByKey = new NamespacedKey(Main.get(), "locked-by");
            if (ConfigFile.autoLockPermissionBased) {
               if (player.hasPermission("RHSignItem.*") || player.hasPermission("RHSignItem.command.sign.lock.auto")) {
                  data.set(lockedKey, PersistentDataType.BOOLEAN, true);
                  data.set(lockedByKey, PersistentDataType.STRING, player.getUniqueId().toString());
                  item.setItemMeta(meta);
               }
            } else {
               data.set(lockedKey, PersistentDataType.BOOLEAN, true);
               data.set(lockedByKey, PersistentDataType.STRING, player.getUniqueId().toString());
               item.setItemMeta(meta);
            }
         }
      }
   }

   public static boolean offhandHandler(Player player) {
      if (!ConfigFile.offhandItemModeEnabled) {
         return false;
      } else {
         ItemStack item = player.getInventory().getItemInOffHand();
         Material type = item.getType();
         ItemMeta meta = item.getItemMeta();
         if (!item.isEmpty() && !item.getType().isAir()) {
            if (!ConfigFile.offhandItemModeType.equalsIgnoreCase(String.valueOf(type))) {
               player.sendMessage(
                  ColorFormatHandler.formatComponent(
                     PAPIHandler.parse(
                        player,
                        LocaleHandler.wrongItemInOffhand
                           .replace("%prefix%", LocaleHandler.prefix)
                           .replace("%item%", ConfigFile.offhandItemModeType.toUpperCase())
                     )
                  )
               );
               SoundHandler.fail(player);
               return false;
            } else {
               int maxDurability = type.getMaxDurability();
               int currentAmount = item.getAmount();
               if (ConfigFile.offhandItemModeUseOnAction) {
                  if (maxDurability > 0) {
                     if (meta instanceof Damageable damageable) {
                        int currentDamage = damageable.getDamage();
                        int newDamage = currentDamage + 1;
                        if (newDamage >= maxDurability) {
                           player.getInventory().setItemInOffHand(null);
                        } else {
                           damageable.setDamage(newDamage);
                           item.setItemMeta(meta);
                        }
                     }
                  } else if (currentAmount > 0) {
                     item.setAmount(currentAmount - 1);
                  }
               }

               return true;
            }
         } else {
            player.sendMessage(
               ColorFormatHandler.formatComponent(
                  PAPIHandler.parse(
                     player,
                     LocaleHandler.noItemInOffhand.replace("%prefix%", LocaleHandler.prefix).replace("%item%", ConfigFile.offhandItemModeType.toUpperCase())
                  )
               )
            );
            SoundHandler.fail(player);
            return false;
         }
      }
   }
}
