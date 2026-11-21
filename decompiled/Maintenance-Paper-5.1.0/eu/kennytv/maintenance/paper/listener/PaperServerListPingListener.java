package eu.kennytv.maintenance.paper.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent.ListedPlayerInfo;
import com.destroystokyo.paper.profile.PlayerProfile;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.paper.MaintenancePaperPlugin;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PaperServerListPingListener implements Listener {
   private static final BiConsumer<PaperServerListPingEvent, Settings> PLAYER_LIST_SETTER = hasListedPlayers() ? (event, settings) -> {
      List<ListedPlayerInfo> playerInfoList = event.getListedPlayers();
      playerInfoList.clear();

      for (String string : settings.getLegacyParsedPlayerCountHoverLines()) {
         playerInfoList.add(new ListedPlayerInfo(string, UUID.randomUUID()));
      }
   } : (event, settings) -> {
      List<PlayerProfile> sample = event.getPlayerSample();
      sample.clear();

      for (String string : settings.getLegacyParsedPlayerCountHoverLines()) {
         sample.add(Bukkit.createProfileExact(UUID.randomUUID(), string));
      }
   };
   private final MaintenancePaperPlugin plugin;
   private final Settings settings;

   private static boolean hasListedPlayers() {
      try {
         PaperServerListPingEvent.class.getDeclaredMethod("getListedPlayers");
         return true;
      } catch (NoSuchMethodException var1) {
         return false;
      }
   }

   public PaperServerListPingListener(MaintenancePaperPlugin plugin, Settings settings) {
      this.plugin = plugin;
      this.settings = settings;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void serverListPing(PaperServerListPingEvent event) {
      if (this.settings.isMaintenance()) {
         if (this.settings.isEnablePingMessages()) {
            event.motd(this.settings.getRandomPingMessage());
         }

         if (this.settings.hasCustomPlayerCountMessage()) {
            event.setProtocolVersion(-1);
            event.setVersion(this.settings.getLegacyParsedPlayerCountMessage());
         }

         if (this.settings.hasCustomPlayerCountHoverMessage()) {
            PLAYER_LIST_SETTER.accept(event, this.settings);
         }

         if (this.settings.hasCustomIcon() && this.plugin.getFavicon() != null) {
            event.setServerIcon(this.plugin.getFavicon());
         }
      }
   }
}
