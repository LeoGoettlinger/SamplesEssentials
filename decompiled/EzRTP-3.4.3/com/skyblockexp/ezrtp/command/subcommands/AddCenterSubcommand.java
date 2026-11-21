package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AddCenterSubcommand extends Subcommand {
   private final EzRtpPlugin plugin;

   public AddCenterSubcommand(EzRtpPlugin plugin) {
      super("addcenter", "ezrtp.setcenter");
      this.plugin = plugin;
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         MessageUtil.send(sender, "<red>You don't have permission to use this command.</red>");
         return true;
      } else if (sender instanceof Player player) {
         if (args.length == 1 && !args[0].isBlank()) {
            String name = args[0].trim();
            String normalizedName = name.toLowerCase(Locale.ROOT);
            if (!normalizedName.matches("[a-z0-9_-]+")) {
               MessageUtil.send(sender, "<red>Center names may only use letters, numbers, '-' and '_'.</red>");
               return true;
            } else {
               File rtpFile = new File(this.plugin.getDataFolder(), "rtp.yml");
               FileConfiguration rtpConfig = YamlConfiguration.loadConfiguration(rtpFile);
               rtpConfig.set("centers.named." + normalizedName + ".world", player.getWorld().getName());
               rtpConfig.set("centers.named." + normalizedName + ".center.x", player.getLocation().getBlockX());
               rtpConfig.set("centers.named." + normalizedName + ".center.z", player.getLocation().getBlockZ());

               try {
                  rtpConfig.save(rtpFile);
                  this.plugin.reloadPluginConfiguration();
                  MessageUtil.send(
                     sender,
                     "<green>Saved RTP center '<white>"
                        + normalizedName
                        + "</white>' for world '<white>"
                        + player.getWorld().getName()
                        + "</white>' at X=<white>"
                        + player.getLocation().getBlockX()
                        + "</white> Z=<white>"
                        + player.getLocation().getBlockZ()
                        + "</white>.</green>"
                  );
               } catch (IOException var9) {
                  MessageUtil.send(sender, "<red>Failed to save center: " + var9.getMessage() + "</red>");
               }

               return true;
            }
         } else {
            MessageUtil.send(sender, "<yellow>Usage: /rtp addcenter <name></yellow>");
            return true;
         }
      } else {
         MessageUtil.send(sender, "<red>This command may only be used by players.</red>");
         return true;
      }
   }

   @NotNull
   @Override
   public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      return Collections.emptyList();
   }
}
