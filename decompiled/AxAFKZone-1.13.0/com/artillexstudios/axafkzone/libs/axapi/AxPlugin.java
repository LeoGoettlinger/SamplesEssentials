package com.artillexstudios.axafkzone.libs.axapi;

import com.artillexstudios.axafkzone.libs.axapi.command.AxAPICommand;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.ItemStackAdapter;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axafkzone.libs.axapi.config.adapters.WrappedItemStackAdapter;
import com.artillexstudios.axafkzone.libs.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axafkzone.libs.axapi.dependencies.UnsafeDependencyLoader;
import com.artillexstudios.axafkzone.libs.axapi.dependency.DependencyContainer;
import com.artillexstudios.axafkzone.libs.axapi.events.PacketEntityInteractEvent;
import com.artillexstudios.axafkzone.libs.axapi.gui.AnvilListener;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.InventoryUpdater;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.listener.InventoryClickListener;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.renderer.InventoryRenderers;
import com.artillexstudios.axafkzone.libs.axapi.hologram.Holograms;
import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.libraries.LibraryDownloader;
import com.artillexstudios.axafkzone.libs.axapi.nms.NMSHandlers;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axafkzone.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvents;
import com.artillexstudios.axafkzone.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.listeners.BuiltinPacketListener;
import com.artillexstudios.axafkzone.libs.axapi.packetentity.tracker.EntityTracker;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleTypes;
import com.artillexstudios.axafkzone.libs.axapi.placeholders.PaperPlaceholderHandler;
import com.artillexstudios.axafkzone.libs.axapi.placeholders.PlaceholderAPIHook;
import com.artillexstudios.axafkzone.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axafkzone.libs.axapi.utils.CommandUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axafkzone.libs.axapi.utils.Nameable;
import com.artillexstudios.axafkzone.libs.axapi.utils.PaperNameable;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.Version;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.file.FileUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.file.PaperFileUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import com.artillexstudios.axafkzone.libs.lamp.CommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandHandler;
import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class AxPlugin extends JavaPlugin {
   public EntityTracker tracker;

   public AxPlugin() {
      DependencyContainer.register(FileUtils.class, new PaperFileUtils(this));
      DependencyContainer.register(Nameable.class, new PaperNameable(this));
      PaperPlaceholderHandler.registerTransformer(Player.class, OfflinePlayer.class, player -> player);
      PaperPlaceholderHandler.registerTransformer(OfflinePlayer.class, Player.class, OfflinePlayer::getPlayer);
      TypeAdapterHolder.registerExtraAdapter(WrappedItemStack.class, new WrappedItemStackAdapter());
      TypeAdapterHolder.registerExtraAdapter(ItemStack.class, new ItemStackAdapter());
      FeatureFlags.refresh();
      this.updateFlags();
      Path librariesPath = this.getDataFolder().toPath().getParent().resolve("AxAPI").resolve("libraries").resolve(this.getName());
      LibraryDownloader manager = new LibraryDownloader(librariesPath);
      manager.withMaxDepth(FeatureFlags.LIBRARY_DOWNLOADER_DEPTH.get());
      DependencyManagerWrapper wrapper = new DependencyManagerWrapper(manager);
      wrapper.repository("https://repo.artillex-studios.com/releases/");
      wrapper.relocate("org{}apache{}commons{}math3", "com.artillexstudios.axafkzone.libs.axapi.libs.math3");
      wrapper.relocate("com{}github{}benmanes", "com.artillexstudios.axafkzone.libs.axapi.libs.caffeine");
      Version.downloadVersion(wrapper);
      this.dependencies(wrapper);
      wrapper.dependency("org{}apache{}commons:commons-math3:3.6.1");
      wrapper.dependency("com{}github{}ben-manes{}caffeine:caffeine:3.2.3");

      try {
         Class.forName("net.kyori.adventure.Adventure", false, this.getClass().getClassLoader());
      } catch (ClassNotFoundException var7) {
         wrapper.dependency("net{}kyori:adventure-api:4.26.1");
      }

      UnsafeDependencyLoader unsafeDependencyLoader = new UnsafeDependencyLoader();

      for (Path path : wrapper.wrapped().getLibraryPaths()) {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.debug("Loading library from path: {}", path);
         }

         unsafeDependencyLoader.loadUnsafeLibrary(UncheckedUtils.unsafeCast(this.getClassLoader()), path);
      }
   }

   public void updateFlags() {
   }

   public void onEnable() {
      if (!NMSHandlers.British.initialise(this)) {
         Bukkit.getPluginManager().disablePlugin(this);
      } else {
         if (!CommandUtils.isRegistered("axapi")) {
            CommandHandler handler = BukkitCommandHandler.create(this);
            handler.register(new AxAPICommand());
         }

         ComponentSerializer.INSTANCE.refresh();
         Scheduler.scheduler.init(this);
         if (FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.get()) {
            this.tracker = new EntityTracker(this);
            this.tracker.startTicking();
         }

         if (FeatureFlags.ENABLE_PACKET_LISTENERS.get()) {
            PacketEvents.INSTANCE.addListener(new BuiltinPacketListener(this.tracker));
         }

         Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuitEvent(@NotNull final PlayerQuitEvent event) {
               InventoryRenderers.disconnect(event.getPlayer().getUniqueId());
               ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(event.getPlayer());
               if (FeatureFlags.ENABLE_PACKET_LISTENERS.get()) {
                  wrapper.uninject();
               }

               if (AxPlugin.this.tracker != null) {
                  AxPlugin.this.tracker.untrackFor(ServerPlayerWrapper.wrap(event.getPlayer()));
               }
            }

            @EventHandler
            public void onPlayerJoinEvent(@NotNull final PlayerJoinEvent event) {
               ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(event.getPlayer());
               if (FeatureFlags.ENABLE_PACKET_LISTENERS.get()) {
                  wrapper.inject();
               }
            }

            @EventHandler
            public void onPacketEntityInteractEvent(@NotNull final PacketEntityInteractEvent event) {
               event.getPacketEntity().callInteract(event);
            }

            @EventHandler
            public void onPlayerChangedWorldEvent(@NotNull final PlayerChangedWorldEvent event) {
               if (AxPlugin.this.tracker != null) {
                  AxPlugin.this.tracker.untrackFor(ServerPlayerWrapper.wrap(event.getPlayer()));
               }
            }
         }, this);
         Bukkit.getPluginManager().registerEvents(new AnvilListener(), this);
         if (FeatureFlags.ENABLE_GUI_LISTENERS.get()) {
            Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
         }

         if (FeatureFlags.HOLOGRAM_UPDATE_TICKS.get() > 0L) {
            Holograms.startTicking();
         }

         if (FeatureFlags.USE_INVENTORY_UPDATER.get()) {
            InventoryUpdater.INSTANCE.start(this);
         }

         ParticleTypes.init();
         ClientboundPacketTypes.init();
         ServerboundPacketTypes.init();

         for (Player player : Bukkit.getOnlinePlayers()) {
            ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
            wrapper.inject();
         }

         this.enable();
         if (FeatureFlags.PLACEHOLDER_API_HOOK.get() && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
         }
      }
   }

   public void enable() {
   }

   public void onLoad() {
      this.load();
   }

   public void dependencies(DependencyManagerWrapper manager) {
   }

   public void load() {
   }

   public void onDisable() {
      this.disable();
      Scheduler.get().cancelAll();

      for (Player player : Bukkit.getOnlinePlayers()) {
         ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
         wrapper.uninject();
      }

      if (this.tracker != null) {
         this.tracker.shutdown();
      }

      Holograms.shutdown();
      InventoryUpdater.INSTANCE.shutdown();
   }

   public void disable() {
   }

   public void reload() {
   }

   public long reloadWithTime() {
      long start = System.currentTimeMillis();
      this.reload();
      return System.currentTimeMillis() - start;
   }
}
