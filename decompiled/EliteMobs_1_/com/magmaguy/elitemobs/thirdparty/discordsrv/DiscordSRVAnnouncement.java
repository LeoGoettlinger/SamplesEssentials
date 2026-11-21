package com.magmaguy.elitemobs.thirdparty.discordsrv;

import com.magmaguy.elitemobs.config.DiscordSRVConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DiscordSRVAnnouncement {
   public DiscordSRVAnnouncement(String announcement) {
      if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
         if (!DiscordSRVConfig.getAnnouncementRoomName()
            .equals(
               "YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_CONFIGURATION_FILE_CHECK_ELITEMOBS_WIKI_FOR_DETAILS"
            )) {
            try {
               String room = DiscordSRVConfig.getAnnouncementRoomName();
               TextChannel channel = room.matches("[0-9]+") ? DiscordUtil.getTextChannelById(room) : null;
               if (channel == null) {
                  channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(room);
               }

               if (channel == null) {
                  List<TextChannel> matches = DiscordUtil.getJda().getTextChannelsByName(room, true);
                  if (!matches.isEmpty()) {
                     channel = matches.get(0);
                  }
               }

               if (channel == null) {
                  Logger.warn("Channel room " + room + " is not valid!");
                  return;
               }

               channel.sendMessage(ChatColor.stripColor(announcement))
                  .queue(ignored -> {}, failure -> Logger.warn("DiscordSRV announcement failed for " + room + ": " + failure.getMessage()));
            } catch (Exception var5) {
               Logger.warn("Failed to send announcement via DiscordsSRV! Is it configured correctly?");
            }
         }
      }
   }
}
