package com.earth2me.essentials;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class EssentialsTimer implements Runnable {
   private final transient net.ess3.api.IEssentials ess;
   private final transient Set<UUID> onlineUsers = new HashSet<>();
   private final LinkedList<Double> history = new LinkedList<>();
   private final long maxTime = 10000000L;
   private final long tickInterval = 50L;
   private transient long lastPoll = System.nanoTime();
   private int skip1 = 0;
   private int skip2 = 0;

   EssentialsTimer(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
      this.history.add(20.0);
   }

   @Override
   public void run() {
      long startTime = System.nanoTime();
      long currentTime = System.currentTimeMillis();
      long timeSpent = (startTime - this.lastPoll) / 1000L;
      if (timeSpent == 0L) {
         timeSpent = 1L;
      }

      if (this.history.size() > 10) {
         this.history.remove();
      }

      double tps = 5.0E7 / timeSpent;
      if (tps <= 21.0) {
         this.history.add(tps);
      }

      this.lastPoll = startTime;
      int count = 0;
      this.onlineUsers.clear();

      for (Player player : this.ess.getOnlinePlayers()) {
         count++;
         if (this.skip1 > 0) {
            this.skip1--;
         } else {
            if (count % 10 == 0 && System.nanoTime() - startTime > 5000000L) {
               this.skip1 = count - 1;
               break;
            }

            try {
               User user = this.ess.getUser(player);
               this.onlineUsers.add(user.getBase().getUniqueId());
               user.setLastOnlineActivity(currentTime);
               user.checkActivity();
            } catch (Exception var13) {
               this.ess.getLogger().log(Level.WARNING, "EssentialsTimer Error:", (Throwable)var13);
            }
         }
      }

      count = 0;
      Iterator<UUID> iterator = this.onlineUsers.iterator();

      while (iterator.hasNext()) {
         count++;
         if (this.skip2 > 0) {
            this.skip2--;
         } else {
            if (count % 10 == 0 && System.nanoTime() - startTime > 10000000L) {
               this.skip2 = count - 1;
               break;
            }

            User user = this.ess.getUser(iterator.next());
            if (user == null) {
               iterator.remove();
            } else if (user.getLastOnlineActivity() < currentTime && user.getLastOnlineActivity() > user.getLastLogout()) {
               if (!user.isHidden()) {
                  user.setLastLogout(user.getLastOnlineActivity());
               }

               iterator.remove();
            } else {
               user.checkMuteTimeout(currentTime);
               user.checkJailTimeout(currentTime);
               user.resetInvulnerabilityAfterTeleport();
            }
         }
      }
   }

   public double getAverageTPS() {
      double avg = 0.0;

      for (Double f : this.history) {
         if (f != null) {
            avg += f;
         }
      }

      return avg / this.history.size();
   }
}
