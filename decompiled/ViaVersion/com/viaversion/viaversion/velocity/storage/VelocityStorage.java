package com.viaversion.viaversion.velocity.storage;

import com.velocitypowered.api.proxy.Player;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.util.ReflectionUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class VelocityStorage implements StorableObject {
   private final Player player;
   private List<UUID> cachedBossbar;
   private static Method getServerBossBars;
   private static Class<?> clientPlaySessionHandler;
   private static Method getMinecraftConnection;

   public VelocityStorage(Player player) {
      this.player = player;
   }

   public List<UUID> getBossbar() {
      if (this.cachedBossbar == null) {
         if (clientPlaySessionHandler == null) {
            return null;
         }

         if (getServerBossBars == null) {
            return null;
         }

         if (getMinecraftConnection == null) {
            return null;
         }

         try {
            Object connection = getMinecraftConnection.invoke(this.player);
            Object sessionHandler = ReflectionUtil.invoke(connection, "getSessionHandler");
            if (clientPlaySessionHandler.isInstance(sessionHandler)) {
               this.cachedBossbar = (List<UUID>)getServerBossBars.invoke(sessionHandler);
            }
         } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var3) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to get bossbar list", (Throwable)var3);
         }
      }

      return this.cachedBossbar;
   }

   static {
      try {
         clientPlaySessionHandler = Class.forName("com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler");
         getServerBossBars = clientPlaySessionHandler.getDeclaredMethod("getServerBossBars");
         getMinecraftConnection = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer").getDeclaredMethod("getMinecraftConnection");
      } catch (ClassNotFoundException | NoSuchMethodException var1) {
         Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to initialize Velocity bossbar support, bossbars will not work.", (Throwable)var1);
      }
   }
}
