package eu.kennytv.maintenance.api;

import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface Settings {
   boolean isMaintenance();

   @Nullable
   String activeMode();

   String activeReason();

   void setActiveReason(@Nullable String var1);

   boolean isEnablePingMessages();

   boolean isJoinNotifications();

   boolean hasCustomIcon();

   Map<UUID, String> getWhitelistedPlayers();

   boolean isWhitelisted(UUID var1);

   boolean removeWhitelistedPlayer(UUID var1);

   @Deprecated
   boolean removeWhitelistedPlayer(String var1);

   boolean addWhitelistedPlayer(UUID var1, String var2);

   void reloadConfigs();

   boolean debugEnabled();
}
