package vrx.economy.vRXClans;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarService {
   private final Map<String, WarService.TimedRequest> pendingAllyReqs = new ConcurrentHashMap<>();
   private final Map<String, WarService.TimedRequest> pendingRivalReqs = new ConcurrentHashMap<>();
   private final Map<String, WarService.TimedRequest> pendingNeutralReqs = new ConcurrentHashMap<>();
   private final Map<String, WarService.WarRequest> pendingWarReqs = new ConcurrentHashMap<>();
   private final Map<WarService.KillPair, Long> recentWarKills = new ConcurrentHashMap<>();

   public void setAllyReq(String target, String requester, int timeoutSeconds) {
      this.pendingAllyReqs.put(target, new WarService.TimedRequest(requester, this.expiresAt(timeoutSeconds)));
   }

   public String getAllyReq(String target) {
      return this.getTimedRequest(this.pendingAllyReqs, target);
   }

   public void clearAllyReq(String target) {
      this.pendingAllyReqs.remove(target);
   }

   public void setRivalReq(String target, String requester, int timeoutSeconds) {
      this.pendingRivalReqs.put(target, new WarService.TimedRequest(requester, this.expiresAt(timeoutSeconds)));
   }

   public String getRivalReq(String target) {
      return this.getTimedRequest(this.pendingRivalReqs, target);
   }

   public void clearRivalReq(String target) {
      this.pendingRivalReqs.remove(target);
   }

   public void setNeutralReq(String target, String requester, int timeoutSeconds) {
      this.pendingNeutralReqs.put(target, new WarService.TimedRequest(requester, this.expiresAt(timeoutSeconds)));
   }

   public String getNeutralReq(String target) {
      return this.getTimedRequest(this.pendingNeutralReqs, target);
   }

   public void clearNeutralReq(String target) {
      this.pendingNeutralReqs.remove(target);
   }

   public void setWarReq(String target, String requester, double wager) {
      this.pendingWarReqs.put(target, new WarService.WarRequest(requester, wager));
   }

   public WarService.WarRequest getWarReq(String target) {
      return this.pendingWarReqs.get(target);
   }

   public void clearWarReq(String target) {
      this.pendingWarReqs.remove(target);
   }

   public boolean canCountWarKill(UUID killer, UUID victim, long cooldownMillis) {
      if (cooldownMillis <= 0L) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         WarService.KillPair pair = new WarService.KillPair(killer, victim);
         Long last = this.recentWarKills.get(pair);
         if (last != null && now - last < cooldownMillis) {
            return false;
         } else {
            this.recentWarKills.put(pair, now);
            this.recentWarKills.entrySet().removeIf(entry -> now - entry.getValue() > cooldownMillis * 2L);
            return true;
         }
      }
   }

   private long expiresAt(int timeoutSeconds) {
      return timeoutSeconds <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeoutSeconds * 1000L;
   }

   private String getTimedRequest(Map<String, WarService.TimedRequest> map, String target) {
      WarService.TimedRequest request = map.get(target);
      if (request == null) {
         return null;
      } else if (System.currentTimeMillis() > request.expiresAt()) {
         map.remove(target);
         return null;
      } else {
         return request.requester();
      }
   }

   private record KillPair(UUID killer, UUID victim) {
   }

   private record TimedRequest(String requester, long expiresAt) {
   }

   public record WarRequest(String requester, double wager) {
   }
}
