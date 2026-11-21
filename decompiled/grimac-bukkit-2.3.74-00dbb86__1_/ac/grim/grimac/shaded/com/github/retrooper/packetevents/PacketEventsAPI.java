package ac.grim.grimac.shaded.com.github.retrooper.packetevents;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.EventManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.injector.ChannelInjector;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.InternalPacketListener;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.player.PlayerManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.registry.ItemRegistry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.registry.RegistryManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.NettyManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.settings.PacketEventsSettings;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.PEVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.PEVersions;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.AdventureConversionInjector;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.logger.JulLegacyLogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.SynchronizedRegistriesHandler;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.updatechecker.UpdateChecker;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.logging.Logger;

public abstract class PacketEventsAPI<T> {
   private final EventManager eventManager = new EventManager();
   private final PacketEventsSettings settings = new PacketEventsSettings();
   private final UpdateChecker updateChecker = new UpdateChecker();
   private final LogManager logManager = LogManager.construct(this);

   public EventManager getEventManager() {
      return this.eventManager;
   }

   public PacketEventsSettings getSettings() {
      return this.settings;
   }

   public UpdateChecker getUpdateChecker() {
      return this.updateChecker;
   }

   public PEVersion getVersion() {
      return PEVersions.CURRENT;
   }

   @Deprecated
   public Logger getLogger() {
      return JulLegacyLogManager.getLogger();
   }

   public LogManager getLogManager() {
      return this.logManager;
   }

   public void load() {
      this.getLogManager().info("Loading packetevents...");
      WrappedBlockState.ensureLoad();
      SynchronizedRegistriesHandler.init();
      PacketType.prepare();
      AdventureConversionInjector.inject();
      this.registerInternalListener();
   }

   protected void registerInternalListener() {
      this.getEventManager().registerListener(new InternalPacketListener());
   }

   public abstract boolean isLoaded();

   public abstract void init();

   public abstract boolean isInitialized();

   public void terminate() {
      try {
         this.getInjector().uninject();
      } catch (Throwable var2) {
         this.getLogManager().warn("Failed to uninject during termination, this error can be ignored on shutdown", var2);
      }

      this.getEventManager().unregisterAllListeners();
      AdventureConversionInjector.uninject();
   }

   public abstract boolean isTerminated();

   public abstract T getPlugin();

   public abstract ServerManager getServerManager();

   public abstract ProtocolManager getProtocolManager();

   public abstract PlayerManager getPlayerManager();

   public abstract NettyManager getNettyManager();

   public abstract ChannelInjector getInjector();

   public RegistryManager getRegistryManager() {
      return new RegistryManager() {
         @Override
         public ItemRegistry getItemRegistry() {
            return new ItemRegistry() {
               @Nullable
               @Override
               public ItemType getByName(String name) {
                  return null;
               }

               @Nullable
               @Override
               public ItemType getById(int id) {
                  return null;
               }
            };
         }
      };
   }
}
