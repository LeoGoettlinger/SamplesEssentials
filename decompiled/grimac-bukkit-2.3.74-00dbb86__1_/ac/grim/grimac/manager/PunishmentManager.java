package ac.grim.grimac.manager;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.config.ConfigReloadable;
import ac.grim.grimac.api.event.events.CommandExecuteEvent;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.events.packets.ProxyAlertMessenger;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public class PunishmentManager implements ConfigReloadable {
   private static final CommandExecuteEvent.Channel COMMAND_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(CommandExecuteEvent.class);
   private final GrimPlayer player;
   private final List<PunishGroup> groups = new ArrayList<>();
   private String experimentalSymbol = "*";
   private String alertString;
   private boolean testMode;
   private String proxyAlertString = "";

   public PunishmentManager(GrimPlayer player) {
      this.player = player;
   }

   public void reload(ConfigManager config) {
      List<String> punish = config.getStringListElse("Punishments", new ArrayList<>());
      this.experimentalSymbol = config.getStringElse("experimental-symbol", "*");
      this.alertString = config.getStringElse(
         "alerts-format",
         "%prefix% &f%player% &bfailed <hover:show_text:\"&b%check_name%%experimental%\\n&8Description: &f%description%\">&f%check_name%%experimental%</hover> &f(x&c%vl%&f) &7%verbose%"
      );
      this.testMode = config.getBooleanElse("test-mode", false);
      this.proxyAlertString = config.getStringElse(
         "alerts-format-proxy",
         "%prefix% &f[&cproxy&f] &f%player% &bfailed <hover:show_text:\"&b%check_name%%experimental%\\n&8Description: &f%description%\">&f%check_name%%experimental%</hover> &f(x&c%vl%&f) &7%verbose%"
      );

      try {
         this.groups.clear();

         for (AbstractCheck check : this.player.checkManager.allChecks.values()) {
            check.setEnabled(false);
         }

         for (Object s : punish) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)s;
            List<String> checks = (List<String>)map.getOrDefault("checks", new ArrayList());
            List<String> commands = (List<String>)map.getOrDefault("commands", new ArrayList());
            int removeViolationsAfter = (Integer)map.getOrDefault("remove-violations-after", 300);
            List<ParsedCommand> parsed = new ArrayList<>();
            List<AbstractCheck> checksList = new ArrayList<>();
            List<AbstractCheck> excluded = new ArrayList<>();

            for (String command : checks) {
               command = command.toLowerCase(Locale.ROOT);
               boolean exclude = false;
               if (command.startsWith("!")) {
                  exclude = true;
                  command = command.substring(1);
               }

               for (AbstractCheck check : this.player.checkManager.allChecks.values()) {
                  if (check.getCheckName() != null
                     && (
                        check.getCheckName().toLowerCase(Locale.ROOT).contains(command)
                           || check.getAlternativeName().toLowerCase(Locale.ROOT).contains(command)
                     )) {
                     if (exclude) {
                        excluded.add(check);
                     } else {
                        checksList.add(check);
                        check.setEnabled(true);
                     }
                  }
               }

               for (AbstractCheck checkx : excluded) {
                  checksList.remove(checkx);
               }
            }

            for (String command : commands) {
               String firstNum = command.substring(0, command.indexOf(":"));
               String secondNum = command.substring(command.indexOf(":"), command.indexOf(" "));
               int threshold = Integer.parseInt(firstNum);
               int interval = Integer.parseInt(secondNum.substring(1));
               String commandString = command.substring(command.indexOf(" ") + 1);
               parsed.add(new ParsedCommand(threshold, interval, commandString));
            }

            this.groups.add(new PunishGroup(checksList, parsed, removeViolationsAfter * 1000));
         }
      } catch (Exception var19) {
         LogUtil.error("Error while loading punishments.yml! This is likely your fault!", var19);
      }
   }

   private String replaceAlertPlaceholders(String original, int vl, Check check, String verbose) {
      return MessageUtil.replacePlaceholders(
            this.player,
            original.replace("[alert]", this.alertString)
               .replace("[proxy]", this.proxyAlertString)
               .replace("%check_name%", check.getDisplayName())
               .replace("%experimental%", check.isExperimental() ? this.experimentalSymbol : "")
               .replace("%vl%", Integer.toString(vl))
               .replace("%description%", check.getDescription())
               .replace("%stable_key%", check.getStableKey())
         )
         .replace("%verbose%", MessageUtil.miniMessageSafe(verbose));
   }

   public boolean handleAlert(GrimPlayer player, String verbose, Check check) {
      String value = verbose == null ? "" : verbose;
      return this.handleAlert(player, () -> value, check);
   }

   public boolean handleAlert(GrimPlayer player, Supplier<String> verbose, Check check) {
      boolean sentDebug = false;

      for (PunishGroup group : this.groups) {
         if (group.checks.contains(check)) {
            int vl = this.getViolations(group, check);
            int violationCount = group.violations.size();

            for (ParsedCommand command : group.commands) {
               Set<PlatformPlayer> verboseListeners = null;
               if (command.command.equals("[alert]") && GrimAPI.INSTANCE.getAlertManager().hasVerboseListeners()) {
                  sentDebug = true;
                  String verboseForListeners = safeGet(verbose);
                  String listenerCmd = this.replaceAlertPlaceholders(command.command, vl, check, verboseForListeners);
                  verboseListeners = GrimAPI.INSTANCE.getAlertManager().sendVerbose(MessageUtil.miniMessage(listenerCmd), null);
               }

               if (violationCount >= command.threshold) {
                  boolean shouldRun = command.interval == 0 ? command.executeCount == 0 : violationCount >= command.nextBoundary;
                  if (shouldRun) {
                     String renderedVerbose = safeGet(verbose);
                     String cmd = this.replaceAlertPlaceholders(command.command, vl, check, renderedVerbose);
                     boolean canceled = COMMAND_CHANNEL.fire(player, check, renderedVerbose, cmd);
                     if (command.interval == 0) {
                        command.executeCount++;
                     } else {
                        advanceBoundary(command, violationCount);
                     }

                     if (canceled) {
                        continue;
                     }

                     String var16 = command.command;
                     switch (var16) {
                        case "[webhook]":
                           GrimAPI.INSTANCE.getDiscordManager().sendAlert(player, renderedVerbose, check.getDisplayName(), vl);
                           break;
                        case "[log]":
                           if (!check.isLastFlagStoredBinaryVerbose()) {
                              String verboseWithoutGl = renderedVerbose.replaceAll(" /gl .*", "");
                              GrimAPI.INSTANCE.getDataStoreLifecycle().liveWriteHooks().recordFlagFromCheck(player, check, vl, verboseWithoutGl);
                           }
                           break;
                        case "[proxy]":
                           ProxyAlertMessenger.sendPluginMessage(cmd);
                           break;
                        case "[alert]":
                           sentDebug = true;
                           Component message = MessageUtil.miniMessage(cmd);
                           if (this.testMode) {
                              if (verboseListeners == null || verboseListeners.contains(player.platformPlayer)) {
                                 player.sendMessage(message);
                              }
                           } else {
                              GrimAPI.INSTANCE.getAlertManager().sendAlert(message, verboseListeners);
                           }
                           break;
                        default:
                           GrimAPI.INSTANCE
                              .getScheduler()
                              .getGlobalRegionScheduler()
                              .run(
                                 GrimAPI.INSTANCE.getGrimPlugin(),
                                 () -> GrimAPI.INSTANCE.getPlatformServer().dispatchCommand(GrimAPI.INSTANCE.getPlatformServer().getConsoleSender(), cmd)
                              );
                     }
                  }

                  if (command.interval > 0) {
                     command.executeCount++;
                  }
               } else {
                  command.nextBoundary = command.threshold;
               }
            }
         }
      }

      return sentDebug;
   }

   private static String safeGet(Supplier<String> supplier) {
      try {
         String value = supplier.get();
         return value == null ? "" : value;
      } catch (Throwable var2) {
         return "";
      }
   }

   private static void advanceBoundary(ParsedCommand command, int violationCount) {
      command.nextBoundary = command.nextBoundary + ((violationCount - command.nextBoundary) / command.interval + 1) * command.interval;
   }

   public void handleViolation(Check check) {
      for (PunishGroup group : this.groups) {
         if (group.checks.contains(check)) {
            long currentTime = System.currentTimeMillis();
            group.violations.put(currentTime, check);
            group.violations.long2ObjectEntrySet().removeIf(time -> currentTime - time.getLongKey() > group.removeViolationsAfter);
         }
      }
   }

   private int getViolations(PunishGroup group, Check check) {
      int vl = 0;

      for (Check value : group.violations.values()) {
         if (value == check) {
            vl++;
         }
      }

      return vl;
   }
}
