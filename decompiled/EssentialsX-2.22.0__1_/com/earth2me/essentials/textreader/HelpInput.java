package com.earth2me.essentials.textreader;

import com.earth2me.essentials.User;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import net.ess3.provider.KnownCommandsProvider;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class HelpInput implements IText {
   private final transient List<String> lines = new ArrayList<>();
   private final transient List<String> chapters = new ArrayList<>();
   private final transient Map<String, Integer> bookmarks = new HashMap<>();

   public HelpInput(final User user, final String match, final IEssentials ess) {
      boolean reported = false;
      List<String> newLines = new ArrayList<>();
      String pluginName = "";
      String pluginNameLow = "";
      if (!match.equalsIgnoreCase("")) {
         this.lines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpMatching", match)));
      }

      Multimap<Plugin, Command> pluginCommands = HashMultimap.create();

      for (Command command : ess.provider(KnownCommandsProvider.class).getKnownCommands().values()) {
         if (command instanceof PluginIdentifiableCommand) {
            PluginIdentifiableCommand pluginIdentifiableCommand = (PluginIdentifiableCommand)command;
            pluginCommands.put(pluginIdentifiableCommand.getPlugin(), command);
         }
      }

      for (Plugin p : ess.getServer().getPluginManager().getPlugins()) {
         try {
            List<String> pluginLines = new ArrayList<>();
            PluginDescriptionFile desc = p.getDescription();
            Map<String, Map<String, Object>> cmds = desc.getCommands();
            pluginName = p.getDescription().getName();
            pluginNameLow = pluginName.toLowerCase(Locale.ENGLISH);
            if (pluginNameLow.equals(match)) {
               this.lines.clear();
               newLines.clear();
               this.lines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpFrom", p.getDescription().getName())));
            }

            boolean isOnWhitelist = user.isAuthorized("essentials.help." + pluginNameLow);

            for (Command commandx : pluginCommands.get(p)) {
               try {
                  String commandName = commandx.getName();
                  String commandDescription = commandx.getDescription();
                  if (match.equalsIgnoreCase("")
                     || pluginNameLow.contains(match)
                     || commandName.toLowerCase(Locale.ENGLISH).contains(match)
                     || commandDescription.toLowerCase(Locale.ENGLISH).contains(match)) {
                     if (pluginNameLow.contains("essentials")) {
                        String node = "essentials." + commandName;
                        if (!ess.getSettings().isCommandDisabled(commandName) && user.isAuthorized(node)) {
                           pluginLines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpLine", commandName, commandDescription)));
                        }
                     } else if (ess.getSettings().showNonEssCommandsInHelp()) {
                        String permissionRaw = commandx.getPermission();
                        String[] permissions;
                        if (permissionRaw == null) {
                           permissions = new String[0];
                        } else {
                           permissions = permissionRaw.split(";");
                        }

                        if (!isOnWhitelist && !user.isAuthorized("essentials.help." + pluginNameLow + "." + commandName)) {
                           if (permissions.length == 0) {
                              if (!ess.getSettings().hidePermissionlessHelp()) {
                                 pluginLines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpLine", commandName, commandDescription)));
                              }
                           } else {
                              boolean enabled = false;
                              String[] var24 = permissions;
                              int var25 = permissions.length;
                              int var26 = 0;

                              while (true) {
                                 if (var26 < var25) {
                                    String permission = var24[var26];
                                    if (!user.isAuthorized(permission)) {
                                       var26++;
                                       continue;
                                    }

                                    enabled = true;
                                 }

                                 if (enabled) {
                                    pluginLines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpLine", commandName, commandDescription)));
                                 }
                                 break;
                              }
                           }
                        } else {
                           pluginLines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpLine", commandName, commandDescription)));
                        }
                     }
                  }
               } catch (NullPointerException var28) {
               }
            }

            if (!pluginLines.isEmpty()) {
               newLines.addAll(pluginLines);
               if (pluginNameLow.equals(match)) {
                  break;
               }

               if (match.equalsIgnoreCase("")) {
                  this.lines.add(ess.getAdventureFacet().miniToLegacy(user.playerTl("helpPlugin", pluginName, pluginNameLow)));
               }
            }
         } catch (NullPointerException var29) {
         } catch (Exception var30) {
            if (!reported) {
               ess.getLogger()
                  .log(Level.WARNING, ess.getAdventureFacet().miniToLegacy(user.playerTl("commandHelpFailedForPlugin", pluginNameLow)), (Throwable)var30);
            }

            reported = true;
         }
      }

      this.lines.addAll(newLines);
   }

   @Override
   public List<String> getLines() {
      return this.lines;
   }

   @Override
   public List<String> getChapters() {
      return this.chapters;
   }

   @Override
   public Map<String, Integer> getBookmarks() {
      return this.bookmarks;
   }
}
