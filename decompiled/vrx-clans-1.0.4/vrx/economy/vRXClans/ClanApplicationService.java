package vrx.economy.vRXClans;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClanApplicationService {
   private final Map<String, Map<UUID, ClanApplication>> applications = new ConcurrentHashMap<>();

   public boolean apply(UUID uuid, String name, String clanName, int timeoutSeconds) {
      String key = clanName.toLowerCase(Locale.ROOT);
      this.cleanup(key);
      Map<UUID, ClanApplication> clanApps = this.applications.computeIfAbsent(key, ignored -> new ConcurrentHashMap<>());
      if (clanApps.containsKey(uuid)) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         clanApps.put(uuid, new ClanApplication(uuid, name, clanName, now, now + Math.max(30, timeoutSeconds) * 1000L));
         return true;
      }
   }

   public List<ClanApplication> list(String clanName) {
      String key = clanName.toLowerCase(Locale.ROOT);
      this.cleanup(key);
      return this.applications.getOrDefault(key, Map.of()).values().stream().sorted(Comparator.comparingLong(ClanApplication::createdAt)).toList();
   }

   public ClanApplication find(String clanName, String playerName) {
      String key = clanName.toLowerCase(Locale.ROOT);
      this.cleanup(key);

      for (ClanApplication application : this.applications.getOrDefault(key, Map.of()).values()) {
         if (application.applicantName().equalsIgnoreCase(playerName)) {
            return application;
         }
      }

      return null;
   }

   public void remove(String clanName, UUID uuid) {
      String key = clanName.toLowerCase(Locale.ROOT);
      Map<UUID, ClanApplication> clanApps = this.applications.get(key);
      if (clanApps != null) {
         clanApps.remove(uuid);
         if (clanApps.isEmpty()) {
            this.applications.remove(key);
         }
      }
   }

   public void removeAllForPlayer(UUID uuid) {
      for (Map<UUID, ClanApplication> clanApps : this.applications.values()) {
         clanApps.remove(uuid);
      }

      this.applications.entrySet().removeIf(entry -> entry.getValue().isEmpty());
   }

   private void cleanup(String key) {
      Map<UUID, ClanApplication> clanApps = this.applications.get(key);
      if (clanApps != null) {
         clanApps.values().removeIf(ClanApplication::expired);
         if (clanApps.isEmpty()) {
            this.applications.remove(key);
         }
      }
   }
}
