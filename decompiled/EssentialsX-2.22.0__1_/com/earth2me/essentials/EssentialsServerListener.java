package com.earth2me.essentials;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.ess3.nms.refl.ReflUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class EssentialsServerListener implements Listener {
   private static final List<String> ignoredSLPECallers = Arrays.asList(
      ".LegacyPingHandler.channelRead(", "de.dytanic.cloudnet.bridge.BukkitBootstrap", "de.dytanic.cloudnet.ext.bridge.bukkit.BukkitCloudNetBridgePlugin"
   );
   private final transient net.ess3.api.IEssentials ess;
   private final boolean isPaperSample;
   private boolean unsupportedLogged = false;
   private boolean npeWarned = false;
   private Method setSampleText;
   private Method getSampleText;

   public EssentialsServerListener(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
      if (ReflUtil.getClassCached("com.destroystokyo.paper.event.server.PaperServerListPingEvent") == null) {
         this.setSampleText = ReflUtil.getMethodCached(ServerListPingEvent.class, "setSampleText", List.class);
         this.getSampleText = ReflUtil.getMethodCached(ServerListPingEvent.class, "getSampleText");
         if (this.setSampleText != null && this.getSampleText != null) {
            ess.getLogger().info("ServerListPingEvent: Paper 1.12.2 setSampleText API");
            this.isPaperSample = true;
            return;
         }
      }

      ess.getLogger().info("ServerListPingEvent: Spigot iterator API");
      this.isPaperSample = false;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onServerListPing(final ServerListPingEvent event) throws Exception {
      if (this.isPaperSample) {
         try {
            List<String> playerNames = (List<String>)this.getSampleText.invoke(event, null);
            playerNames.removeIf(playerx -> this.ess.getUser(playerx).isVanished());
            this.setSampleText.invoke(event, playerNames);
         } catch (InvocationTargetException | ClassCastException | IllegalAccessException var4) {
            if (!this.unsupportedLogged && this.shouldWarnSLPECaller(var4)) {
               this.ess.getLogger().log(Level.WARNING, "Unable to hide players from server list ping using Paper 1.12 method!", (Throwable)var4);
               this.unsupportedLogged = true;
            }
         } catch (NullPointerException var5) {
            if (!this.npeWarned && this.shouldWarnSLPECaller(var5)) {
               this.npeWarned = true;
               Exception ex = new Exception(
                  "A plugin has fired a ServerListPingEvent without implementing Paper's methods. Point the author to https://git.io/v7Xzl."
               );
               ex.setStackTrace(var5.getStackTrace());
               throw ex;
            }
         }
      } else {
         try {
            Iterator<Player> iterator = event.iterator();

            while (iterator.hasNext()) {
               Player player = iterator.next();
               if (this.ess.getUser(player).isVanished()) {
                  iterator.remove();
               }
            }
         } catch (UnsupportedOperationException var6) {
            if (!this.unsupportedLogged && this.shouldWarnSLPECaller(var6)) {
               this.ess.getLogger().log(Level.WARNING, "Could not hide vanished players while handling " + event.getClass().getName(), (Throwable)var6);
               this.unsupportedLogged = true;
            }
         }
      }
   }

   private boolean shouldWarnSLPECaller(final Throwable throwable) {
      int maxStackDepth = 20;
      int depth = 0;

      for (StackTraceElement element : throwable.getStackTrace()) {
         if (++depth > 20) {
            break;
         }

         for (String ignoredString : ignoredSLPECallers) {
            if (element.toString().contains(ignoredString)) {
               return false;
            }
         }
      }

      return true;
   }
}
