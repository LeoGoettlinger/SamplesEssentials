package dev.vatten.baserad;

import dev.vatten.baserad.commands.Command;
import dev.vatten.baserad.events.PlayerJoinEvent;
import dev.vatten.baserad.events.PlayerLeaveEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import lombok.Generated;

public class VattenPlugin {
   protected final EventHandler eventHandler = new EventHandler(this);
   protected final VattenPlatform<?, ?> pluginInterface;
   protected final PluginInfo pluginInfo;
   protected VattenPlugin.API api = new VattenPlugin.API();
   protected final Path path;
   protected final Map<UUID, VattenPlayer> players = new HashMap<>();

   protected VattenPlugin(VattenPlatform<?, ?> pluginInterface, VattenPlugin.Type type, Path path) {
      this.pluginInterface = pluginInterface;
      this.path = path;
      Properties p = new Properties();

      try {
         p.load(VattenPlugin.class.getResourceAsStream("/plugin.properties"));
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }

      this.pluginInfo = new PluginInfo(p.getProperty("name"), p.getProperty("displayName"), p.getProperty("version"), type);
   }

   VattenPlayer getPlayer(UUID uuid) {
      return this.players.get(uuid);
   }

   protected void onEnable() {
      this.eventHandler.registerEventHandler(PlayerJoinEvent.class, this::onPlayerJoin);
      this.eventHandler.registerEventHandler(PlayerLeaveEvent.class, this::onPlayerLeave);
   }

   protected void reload() {
   }

   protected void registerCommands(Command... commands) {
      for (Command command : commands) {
         this.pluginInterface.registerCommand(command);
      }
   }

   private void onPlayerJoin(PlayerJoinEvent event) {
      this.players.put(event.getPlayer().getUuid(), event.getPlayer());
   }

   private void onPlayerLeave(PlayerLeaveEvent event) {
      this.players.remove(event.getPlayer().getUuid());
   }

   @Generated
   EventHandler getEventHandler() {
      return this.eventHandler;
   }

   @Generated
   public PluginInfo getPluginInfo() {
      return this.pluginInfo;
   }

   @Generated
   public VattenPlugin.API getApi() {
      return this.api;
   }

   @Generated
   Path getPath() {
      return this.path;
   }

   @Generated
   Map<UUID, VattenPlayer> getPlayers() {
      return this.players;
   }

   public class API {
      public void reload() {
         VattenPlugin.this.reload();
      }
   }

   public static enum Type {
      SERVER,
      PROXY;
   }
}
