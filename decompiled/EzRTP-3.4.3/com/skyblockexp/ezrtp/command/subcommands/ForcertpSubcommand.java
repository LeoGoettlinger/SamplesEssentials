package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ForcertpSubcommand extends Subcommand {
   private final EzRtpPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private final Supplier<EzRtpConfiguration> configurationSupplier;

   public ForcertpSubcommand(EzRtpPlugin plugin, Supplier<RandomTeleportService> teleportServiceSupplier, Supplier<EzRtpConfiguration> configurationSupplier) {
      super("forcertp", "ezrtp.forcertp");
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
      this.configurationSupplier = configurationSupplier;
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.COMMAND_NO_PERMISSION));
         return true;
      } else {
         boolean skipMessage = false;
         List<String> filteredArgs = new ArrayList<>();

         for (String arg : args) {
            if ("--skip-message".equalsIgnoreCase(arg)) {
               skipMessage = true;
            } else {
               filteredArgs.add(arg);
            }
         }

         args = filteredArgs.toArray(new String[0]);
         if (args.length < 1) {
            MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FORCERTP_INVALID_USAGE));
            return true;
         } else {
            String targetName = args[0];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null && target.isOnline()) {
               String worldName = args.length >= 2 ? args[1] : null;
               if ("auto".equalsIgnoreCase(worldName)) {
                  worldName = target.getWorld().getName();
               }

               EzRtpConfiguration configuration = this.configurationSupplier.get();
               RandomTeleportSettings settings = configuration != null ? configuration.getSettingsForWorld(worldName) : null;
               if (settings == null) {
                  MessageUtil.send(
                     sender,
                     this.plugin.getMessageProvider().format(MessageKey.FORCERTP_WORLD_MISSING, Map.of("world", worldName != null ? worldName : "default"))
                  );
                  return false;
               } else {
                  if (worldName != null && !worldName.equals(settings.getWorldName())) {
                     settings = settings.withWorldName(worldName);
                  }

                  if (skipMessage) {
                     settings = settings.withSuppressPlayerMessages(true);
                  }

                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  if (service == null) {
                     MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FORCERTP_SERVICE_UNAVAILABLE));
                     return false;
                  } else {
                     boolean suppressConsole = skipMessage || configuration != null && configuration.isSuppressConsoleMessages();
                     if (!suppressConsole) {
                        MessageUtil.send(
                           sender, this.plugin.getMessageProvider().format(MessageKey.FORCERTP_EXECUTOR_NOTIFICATION, Map.of("player", target.getName()))
                        );
                     }

                     if (!settings.isSuppressPlayerMessages()) {
                        MessageUtil.send(target, this.plugin.getMessageProvider().format(MessageKey.FORCERTP_TARGET_NOTIFICATION, target));
                     }

                     service.teleportPlayer(target, settings, TeleportReason.COMMAND);
                     return true;
                  }
               }
            } else {
               MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FORCERTP_PLAYER_NOT_FOUND, Map.of("player", targetName)));
               return true;
            }
         }
      }
   }

   @NotNull
   @Override
   public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         return Collections.emptyList();
      } else if (args.length == 1) {
         List<String> suggestions = new ArrayList<>();

         for (Player player : Bukkit.getOnlinePlayers()) {
            suggestions.add(player.getName());
         }

         return suggestions;
      } else if (args.length != 2) {
         if (args.length == 3) {
            List<String> suggestions = new ArrayList<>();
            if (!Arrays.asList(args).contains("--skip-message")) {
               suggestions.add("--skip-message");
            }

            return suggestions;
         } else {
            return Collections.emptyList();
         }
      } else {
         List<String> suggestions = new ArrayList<>();
         suggestions.add("auto");
         suggestions.add("--skip-message");

         for (World world : Bukkit.getWorlds()) {
            suggestions.add(world.getName());
         }

         return suggestions;
      }
   }
}
