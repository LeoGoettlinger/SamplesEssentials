package com.viaversion.viaversion.api.platform;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.VersionInfo;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public interface ViaPlatform<T> {
   Logger getLogger();

   default Logger createLogger(String name) {
      return this.getLogger();
   }

   String getPlatformName();

   String getPlatformVersion();

   default boolean isProxy() {
      return false;
   }

   default String getPluginVersion() {
      return VersionInfo.getVersion();
   }

   default PlatformTask runAsync(Runnable runnable) {
      return new ViaPlatformTask(Via.getManager().getScheduler().execute(runnable));
   }

   default PlatformTask runRepeatingAsync(Runnable runnable, long ticks) {
      return new ViaPlatformTask(Via.getManager().getScheduler().scheduleRepeating(runnable, ticks * 50L, ticks * 50L, TimeUnit.MILLISECONDS));
   }

   default PlatformTask runSync(Runnable runnable) {
      return this.runAsync(runnable);
   }

   default PlatformTask runSync(Runnable runnable, long delay) {
      return new ViaPlatformTask(Via.getManager().getScheduler().schedule(runnable, delay * 50L, TimeUnit.MILLISECONDS));
   }

   default PlatformTask runRepeatingSync(Runnable runnable, long period) {
      return this.runRepeatingAsync(runnable, period);
   }

   default void sendMessage(UserConnection connection, String message) {
   }

   default boolean kickPlayer(UserConnection connection, String message) {
      return false;
   }

   default void modifyPlayerDetails(UserConnection connection, JsonObject payload) {
   }

   default void modifyServerDetails(UserConnection connection, JsonObject payload) {
   }

   default void sendCustomPayload(UserConnection connection, String channel, byte[] message) {
   }

   default void sendCustomPayloadToClient(UserConnection connection, String channel, byte[] message) {
   }

   ViaAPI<T> getApi();

   ViaVersionConfig getConf();

   File getDataFolder();

   default void onReload() {
   }

   default JsonObject getDump() {
      return new JsonObject();
   }

   default Collection<UnsupportedSoftware> getUnsupportedSoftwareClasses() {
      return List.of();
   }

   default boolean hasPlugin(String name) {
      return false;
   }

   default boolean couldBeReloading() {
      return false;
   }
}
