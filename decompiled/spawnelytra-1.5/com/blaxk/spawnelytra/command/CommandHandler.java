package com.blaxk.spawnelytra.command;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.listener.SpawnElytra;
import com.blaxk.spawnelytra.util.DisplayNames;
import com.blaxk.spawnelytra.util.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor, TabCompleter {
   private final Main plugin;

   public CommandHandler(Main plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         this.sendHelpMessage(sender);
         return true;
      } else {
         String subCommand = args[0].toLowerCase(Locale.ROOT);
         switch (subCommand) {
            case "reload":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               }

               this.plugin.reload();
               MessageUtil.send(sender, "reload_success");
               return true;
            case "info":
               this.sendInfoMessage(sender);
               return true;
            case "update":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               }

               this.plugin.performAutoUpdate(sender);
               return true;
            case "visualize":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               } else if (!(sender instanceof Player player)) {
                  MessageUtil.send(sender, "command_player_only");
                  return true;
               } else {
                  SpawnElytra elytraInstance = this.plugin.getSpawnElytraInstance(player.getWorld().getName());
                  if (elytraInstance == null) {
                     MessageUtil.send(sender, "spawnelytra_not_available");
                     return true;
                  }

                  int seconds = 30;
                  if (args.length >= 2) {
                     try {
                        int parsed = Integer.parseInt(args[1]);
                        if (parsed > 0) {
                           seconds = Math.min(parsed, 600);
                        }
                     } catch (NumberFormatException var19) {
                     }
                  }

                  elytraInstance.visualizeArea(player, seconds);
                  return true;
               }
            case "set":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               } else {
                  if (args.length >= 2 && sender instanceof Player p) {
                     String sub = args[1].toLowerCase(Locale.ROOT);
                     if ("pos1".equals(sub)) {
                        this.plugin.getSetupManager().setPosition(p, 1, p.getLocation());
                        return true;
                     }

                     if ("pos2".equals(sub)) {
                        this.plugin.getSetupManager().setPosition(p, 2, p.getLocation());
                        return true;
                     }
                  }

                  if (args.length < 3) {
                     return true;
                  }

                  String what = args[1].toLowerCase(Locale.ROOT);
                  String value = args[2];
                  if ("language".equals(what)) {
                     this.plugin.applyLanguageSetting(sender, value);
                  } else if ("style".equals(what)) {
                     this.plugin.applyStyleSetting(sender, value);
                  }

                  return true;
               }
            case "settings":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               } else {
                  if (!(sender instanceof Player)) {
                     MessageUtil.send(sender, "command_player_only");
                     return true;
                  }

                  this.plugin.sendSettingsMenu((Player)sender);
                  return true;
               }
            case "options":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               } else {
                  if (!(sender instanceof Player)) {
                     MessageUtil.send(sender, "command_player_only");
                     return true;
                  }

                  this.plugin.sendOptionsMenu((Player)sender);
                  return true;
               }
            case "dismiss":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               }

               this.plugin.markFirstInstallCompleted();
               Component dismissed = MiniMessage.miniMessage().deserialize("<#91f251>The first install message will no longer be shown.");
               if (sender instanceof Player p) {
                  MessageUtil.sendRaw(p, dismissed);
               } else {
                  MessageUtil.sendRaw(sender, dismissed);
               }

               return true;
            case "setup":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               } else {
                  if (sender instanceof Player pl) {
                     if (args.length == 1) {
                        if (!this.plugin.getSetupManager().isInSetup(pl)) {
                           this.plugin.markFirstInstallCompleted();
                           this.plugin.getSetupManager().start(pl);
                        } else {
                           this.plugin.getSetupManager().showOptions(pl);
                        }

                        return true;
                     }

                     String subSetup = args[1].toLowerCase(Locale.ROOT);
                     switch (subSetup) {
                        case "on":
                        case "start":
                           this.plugin.markFirstInstallCompleted();
                           this.plugin.getSetupManager().start(pl);
                           return true;
                        case "off":
                        case "exit":
                        case "cancel":
                           this.plugin.getSetupManager().exit(pl, false);
                           return true;
                        case "save":
                           this.plugin.getSetupManager().save(pl);
                           return true;
                        case "mode":
                           if (args.length >= 3) {
                              this.plugin.getSetupManager().selectActivationMode(pl, args[2]);
                           }

                           return true;
                        case "toggle":
                           if (args.length >= 3) {
                              String which = args[2].toLowerCase(Locale.ROOT);
                              if ("boost".equals(which)) {
                                 this.plugin.getSetupManager().toggleBoostActivatedMessage(pl);
                              } else if ("press".equals(which)) {
                                 this.plugin.getSetupManager().togglePressToBoostMessage(pl);
                              }
                           }

                           return true;
                        default:
                           MessageUtil.send(pl, "help_setup");
                           return true;
                     }
                  }

                  MessageUtil.send(sender, "command_player_only");
                  return true;
               }
            case "debug":
               if (!sender.hasPermission("spawnelytra.admin")) {
                  MessageUtil.send(sender, "no_permission");
                  return true;
               } else if (!(sender instanceof Player)) {
                  MessageUtil.send(sender, "command_player_only");
                  return true;
               } else {
                  if (args.length >= 2) {
                     String dbgWhat = args[1].toLowerCase(Locale.ROOT);
                     if ("firstinstall".equals(dbgWhat)) {
                        this.plugin.getConfig().set("first_install_completed", false);
                        this.plugin.saveConfig();
                        this.plugin.sendFirstInstallWelcome((Player)sender);
                        return true;
                     }
                  }

                  return true;
               }
            default:
               this.sendHelpMessage(sender);
               return true;
         }
      }
   }

   private void sendHelpMessage(CommandSender sender) {
      MessageUtil.send(sender, "help_header");
      MessageUtil.send(sender, "help_reload");
      MessageUtil.send(sender, "help_info");
      if (sender.hasPermission("spawnelytra.admin")) {
         MessageUtil.send(sender, "help_visualize");
         MessageUtil.send(sender, "help_settings");
         MessageUtil.send(sender, "help_setup");
      }
   }

   private void sendInfoMessage(CommandSender sender) {
      MessageUtil.send(sender, "info_header");
      String version = this.plugin.getDescription().getVersion();
      String author = this.plugin.getDescription().getAuthors().isEmpty() ? "Unknown" : (String)this.plugin.getDescription().getAuthors().getFirst();
      String website = this.plugin.getDescription().getWebsite() != null ? this.plugin.getDescription().getWebsite() : "-";
      String language = this.plugin.getConfig().getString("language", "en");
      MessageUtil.send(sender, "info_version", Placeholder.unparsed("value", version));
      MessageUtil.sendRaw(sender, this.getAuthorMessage(language.toLowerCase(Locale.ROOT), author));
      MessageUtil.send(sender, "info_website", Placeholder.unparsed("value", website));
      MessageUtil.send(sender, "info_language", Placeholder.unparsed("value", DisplayNames.language(language)));
      ConfigurationSection worldsSection = this.plugin.getConfig().getConfigurationSection("worlds");
      if (worldsSection != null && !worldsSection.getKeys(false).isEmpty()) {
         for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection world = worldsSection.getConfigurationSection(worldName);
            if (world != null) {
               int radius = world.getInt("radius", 100);
               int strength = world.getInt("boost.strength", 2);
               boolean boostEnabled = world.getBoolean("boost.enabled", true);
               String activationMode = world.getString("activation_mode", "double_jump");
               String spawnMode = world.getString("spawn_area.mode", "auto");
               double launchStrength = world.getDouble("f_key.launch_strength", 1.5);
               MessageUtil.send(sender, "info_world", Placeholder.unparsed("value", worldName));
               MessageUtil.send(sender, "info_radius", Placeholder.unparsed("value", String.valueOf(radius)));
               MessageUtil.send(sender, "info_strength", Placeholder.unparsed("value", String.valueOf(strength)));
               MessageUtil.send(sender, "info_boost_enabled", Placeholder.unparsed("value", String.valueOf(boostEnabled)));
               MessageUtil.send(sender, "info_activation_mode", Placeholder.unparsed("value", DisplayNames.activationMode(activationMode)));
               if ("f_key".equalsIgnoreCase(activationMode)) {
                  MessageUtil.send(sender, "info_offhand_key");
                  MessageUtil.send(sender, "info_f_key_launch_strength", Placeholder.unparsed("value", String.valueOf(launchStrength)));
               }

               MessageUtil.send(sender, "info_spawn_mode", Placeholder.unparsed("value", DisplayNames.spawnMode(spawnMode)));
            }
         }
      } else {
         MessageUtil.send(sender, "info_world", Placeholder.unparsed("value", "-"));
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         List<String> completions = new ArrayList<>(Arrays.asList("reload", "info"));
         if (sender.hasPermission("spawnelytra.admin")) {
            completions.add("visualize");
            completions.add("settings");
            completions.add("options");
            completions.add("setup");
         }

         String prefix = args[0].toLowerCase(Locale.ROOT);
         return completions.stream().filter(c -> c.startsWith(prefix)).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String prefix = args[1].toLowerCase(Locale.ROOT);
            if ("setup".equals(sub)) {
               List<String> second = List.of("exit");
               return second.stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
            }

            if ("set".equals(sub) && sender.hasPermission("spawnelytra.admin")) {
               List<String> second = Arrays.asList("pos1", "pos2");
               return second.stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
            }
         } else if (args.length == 3) {
            String subx = args[0].toLowerCase(Locale.ROOT);
            String prefixx = args[2].toLowerCase(Locale.ROOT);
            if ("setup".equals(subx)) {
               if ("mode".equals(args[1].toLowerCase(Locale.ROOT))) {
                  List<String> modes = Arrays.asList("double_jump", "auto", "sneak_jump", "f_key");
                  return modes.stream().filter(m -> m.startsWith(prefix)).collect(Collectors.toList());
               }

               if ("toggle".equals(args[1].toLowerCase(Locale.ROOT))) {
                  List<String> toggles = Arrays.asList("boost", "press");
                  return toggles.stream().filter(t -> t.startsWith(prefix)).collect(Collectors.toList());
               }
            }
         }

         return Collections.emptyList();
      }
   }

   private Component getAuthorMessage(String language, String author) {
      String rawStyle = this.plugin.getConfig().getString("messages.style", "classic");
      String style = (rawStyle == null ? "classic" : rawStyle).toLowerCase(Locale.ROOT);

      String text = switch (language) {
         case "de", "es", "pl" -> "<#fdba5e>Autor: <#91f251>" + author + "</#91f251>";
         case "fr" -> "<#fdba5e>Auteur: <#91f251>" + author + "</#91f251>";
         default -> "<#fdba5e>Author: <#91f251>" + author + "</#91f251>";
      };
      if ("small_caps".equals(style) && ("en".equals(language) || "de".equals(language))) {
         text = MessageUtil.toSmallCapsPreservingTags(text);
      }

      return MiniMessage.miniMessage().deserialize(text);
   }
}
