package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class DiscordSRVConfig extends ConfigurationFile {
   private static String announcementRoomName;

   public DiscordSRVConfig() {
      super("DiscordSRV.yml");
   }

   @Override
   public void initializeValues() {
      announcementRoomName = ConfigurationEngine.setString(
         List.of("Documentation can be found here: https://github.com/MagmaGuy/EliteMobs/wiki/DiscordSRV---Discord-broadcasts"),
         this.file,
         this.fileConfiguration,
         "announcementRoomName",
         "YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_CONFIGURATION_FILE_CHECK_ELITEMOBS_WIKI_FOR_DETAILS",
         false
      );
   }

   @Generated
   public static String getAnnouncementRoomName() {
      return announcementRoomName;
   }
}
