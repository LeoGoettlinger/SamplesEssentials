package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.teleport.ChunkyAdapter;
import com.skyblockexp.ezrtp.teleport.ChunkyProvider;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

public class PregenerateSubcommand extends Subcommand {
   private final EzRtpPlugin plugin;
   private final ChunkyProvider chunkyAPI;
   private final ChunkyWarmupCoordinator chunkyWarmupCoordinator;

   public PregenerateSubcommand(EzRtpPlugin plugin, ChunkyProvider chunkyAPI, ChunkyWarmupCoordinator chunkyWarmupCoordinator) {
      super("pregenerate", "ezrtp.pregenerate");
      this.plugin = plugin;
      this.chunkyAPI = chunkyAPI;
      this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         sender.sendMessage("§cYou don't have permission to use this command.");
         return true;
      } else if (this.chunkyAPI == null) {
         MessageUtil.send(sender, "<red>Chunky plugin is not available.</red>");
         return true;
      } else if (args.length > 2) {
         MessageUtil.send(sender, "<red>Usage: /rtp pregenerate [&lt;world&gt;] [&lt;radius&gt;]</red>");
         return true;
      } else {
         String defaultWorld = this.plugin.getConfig().getString("world", "world");
         int defaultRadius = this.plugin.getConfig().getInt("radius.min", 1000);
         String worldName = args.length > 0 ? args[0] : defaultWorld;
         int radius = defaultRadius;
         if (args.length > 1) {
            try {
               radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException var27) {
               MessageUtil.send(sender, "<red>Invalid radius: " + args[1] + "</red>");
               return true;
            }
         }

         World world = Bukkit.getWorld(worldName);
         if (world == null) {
            MessageUtil.send(sender, "<red>World '" + worldName + "' not found.</red>");
            return true;
         } else {
            try {
               if (ChunkyAdapter.isRunning(this.chunkyAPI, worldName)) {
                  MessageUtil.send(sender, "<red>Pregeneration is already running for world '" + worldName + "'.</red>");
                  return true;
               }
            } catch (Throwable var26) {
            }

            String shape = "circle";
            String pattern = "concentric";
            int numTasks = Math.min(10, Math.max(1, radius / 100));
            int smallRadius = 50;
            boolean anyStarted = false;

            for (int i = 0; i < numTasks; i++) {
               double angle = Math.random() * 2.0 * Math.PI;
               double distance = Math.random() * radius;
               double centerX = Math.cos(angle) * distance;
               double centerZ = Math.sin(angle) * distance;
               boolean started = ChunkyAdapter.startTask(this.chunkyAPI, worldName, shape, centerX, centerZ, smallRadius, smallRadius, pattern);
               if (started) {
                  anyStarted = true;
                  if (this.chunkyWarmupCoordinator != null) {
                     int centerChunkX = (int)Math.floor(centerX / 16.0);
                     int centerChunkZ = (int)Math.floor(centerZ / 16.0);
                     int radiusChunks = Math.max(0, (smallRadius + 15) / 16);
                     this.chunkyWarmupCoordinator.markRegionPlanned(worldName, centerChunkX, centerChunkZ, radiusChunks);
                  }
               }
            }

            if (anyStarted) {
               MessageUtil.send(
                  sender, "<green>Started " + numTasks + " pregeneration tasks for world '" + worldName + "' within radius " + radius + ".</green>"
               );
            } else {
               MessageUtil.send(sender, "<red>Failed to start pregeneration tasks for world '" + worldName + "'.</red>");
            }

            return true;
         }
      }
   }

   @NotNull
   @Override
   public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if (args.length == 1) {
         return Bukkit.getWorlds()
            .stream()
            .<String>map(WorldInfo::getName)
            .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
            .collect(Collectors.toList());
      } else if (args.length == 2) {
         List<String> suggestions = Arrays.asList("500", "1000", "2000", "5000", "10000");
         return suggestions.stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
      } else {
         return Collections.emptyList();
      }
   }
}
