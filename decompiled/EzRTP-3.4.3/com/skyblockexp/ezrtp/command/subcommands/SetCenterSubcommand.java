package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetCenterSubcommand extends Subcommand {
   private final EzRtpPlugin plugin;
   private final Supplier<EzRtpConfiguration> configurationSupplier;

   public SetCenterSubcommand(EzRtpPlugin plugin, Supplier<EzRtpConfiguration> configurationSupplier) {
      super("setcenter", "ezrtp.setcenter");
      this.plugin = plugin;
      this.configurationSupplier = configurationSupplier;
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         sender.sendMessage("§cYou don't have permission to use this command.");
         return true;
      } else {
         String targetWorld;
         int xIndex;
         if (args.length == 2 && sender instanceof Player player) {
            targetWorld = player.getWorld().getName();
            xIndex = 0;
         } else {
            if (args.length != 3) {
               sender.sendMessage("§eUsage: /rtp setcenter <x> <z>  or  /rtp setcenter <world> <x> <z>");
               return true;
            }

            targetWorld = args[0];
            xIndex = 1;
         }

         try {
            int x = Integer.parseInt(args[xIndex]);
            int z = Integer.parseInt(args[xIndex + 1]);
            EzRtpConfiguration config = this.configurationSupplier.get();
            String defaultWorld = config != null && config.getDefaultSettings() != null ? config.getDefaultSettings().getWorldName() : null;
            FileConfiguration fc = this.plugin.getConfig();
            if (defaultWorld != null && defaultWorld.equalsIgnoreCase(targetWorld)) {
               fc.set("center.x", x);
               fc.set("center.z", z);
            } else {
               fc.set("world-centers." + targetWorld + ".x", x);
               fc.set("world-centers." + targetWorld + ".z", z);
            }

            this.plugin.saveConfig();
            this.plugin.reloadPluginConfiguration();
            sender.sendMessage(String.format("§aSet RTP center for world '%s' to X=%d Z=%d and reloaded configuration.", targetWorld, x, z));
         } catch (NumberFormatException var10) {
            sender.sendMessage("§cX and Z must be integers.");
         } catch (Exception var11) {
            sender.sendMessage("§cAn error occurred while saving the configuration: " + var11.getMessage());
         }

         return true;
      }
   }

   @NotNull
   @Override
   public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if (args.length == 1) {
         List<String> names = new ArrayList<>();
         this.plugin.getServer().getWorlds().forEach(w -> names.add(w.getName()));
         return names;
      } else {
         return Collections.emptyList();
      }
   }
}
