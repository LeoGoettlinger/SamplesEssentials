package vrx.economy.vRXClans;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class InviteService {
   private final Map<UUID, String> pendingInvites = new ConcurrentHashMap<>();
   private final Map<UUID, Long> inviteExpirations = new ConcurrentHashMap<>();
   private final Map<UUID, Long> joinCooldowns = new ConcurrentHashMap<>();

   public void setJoinCooldown(Player player, int seconds) {
      UUID uuid = player.getUniqueId();
      if (seconds <= 0) {
         this.joinCooldowns.remove(uuid);
      } else {
         this.joinCooldowns.put(uuid, System.currentTimeMillis() + seconds * 1000L);
      }
   }

   public long getJoinCooldownSeconds(Player player) {
      long expiry = this.joinCooldowns.getOrDefault(player.getUniqueId(), 0L);
      long timeLeft = expiry - System.currentTimeMillis();
      if (timeLeft <= 0L) {
         this.joinCooldowns.remove(player.getUniqueId());
         return 0L;
      } else {
         return Math.max(1L, timeLeft / 1000L);
      }
   }

   public void addInvite(Player player, Clan clan, int timeoutSeconds) {
      UUID uuid = player.getUniqueId();
      this.pendingInvites.put(uuid, clan.getName());
      this.inviteExpirations.put(uuid, System.currentTimeMillis() + timeoutSeconds * 1000L);
   }

   public String getPendingInvite(Player player) {
      UUID uuid = player.getUniqueId();
      if (!this.pendingInvites.containsKey(uuid)) {
         return null;
      } else if (System.currentTimeMillis() > this.inviteExpirations.getOrDefault(uuid, 0L)) {
         this.clearInvite(player);
         return null;
      } else {
         return this.pendingInvites.get(uuid);
      }
   }

   public void clearInvite(Player player) {
      UUID uuid = player.getUniqueId();
      this.pendingInvites.remove(uuid);
      this.inviteExpirations.remove(uuid);
   }
}
