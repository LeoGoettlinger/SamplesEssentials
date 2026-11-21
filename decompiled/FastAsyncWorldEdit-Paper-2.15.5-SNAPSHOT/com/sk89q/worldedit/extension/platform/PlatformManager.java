package com.sk89q.worldedit.extension.platform;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.function.pattern.PatternTraverser;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.wrappers.LocationMaskedPlayerWrapper;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.DoubleActionBlockTool;
import com.sk89q.worldedit.command.tool.DoubleActionTraceTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.TraceTool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.event.platform.Interaction;
import com.sk89q.worldedit.event.platform.PlatformInitializeEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.concurrency.EvenMoreExecutors;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class PlatformManager {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final WorldEdit worldEdit;
   private final PlatformCommandManager platformCommandManager;
   private final SimpleLifecycled<ListeningExecutorService> executorService;
   private final Map<Platform, Boolean> platforms = Maps.newHashMap();
   private final Map<Capability, Platform> preferences = new EnumMap<>(Capability.class);
   @Nullable
   private String firstSeenVersion;
   private final AtomicBoolean initialized = new AtomicBoolean();
   private final AtomicBoolean configured = new AtomicBoolean();

   public PlatformManager(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
      this.platformCommandManager = new PlatformCommandManager(worldEdit, this);
      this.executorService = SimpleLifecycled.invalid();
      worldEdit.getEventBus().register(this);
   }

   public synchronized void register(Platform platform) {
      Preconditions.checkNotNull(platform);
      LOGGER.info("Got request to register " + platform.getClass() + " with WorldEdit [" + super.toString() + "]");
      this.platforms.put(platform, false);
      if (this.firstSeenVersion != null) {
         if (!this.firstSeenVersion.equals(platform.getVersion())) {
            LOGGER.warn(
               "Multiple ports of WorldEdit are installed but they report different versions ({} and {}). If these two versions are truly different, then you may run into unexpected crashes and errors.",
               this.firstSeenVersion,
               platform.getVersion()
            );
         }
      } else {
         this.firstSeenVersion = platform.getVersion();
      }
   }

   public synchronized boolean unregister(Platform platform) {
      Preconditions.checkNotNull(platform);
      boolean removed = this.platforms.remove(platform) != null;
      if (removed) {
         LOGGER.info("Unregistering " + platform.getClass().getCanonicalName() + " from WorldEdit");
         boolean choosePreferred = false;
         Iterator<Entry<Capability, Platform>> it = this.preferences.entrySet().iterator();

         while (it.hasNext()) {
            Entry<Capability, Platform> entry = it.next();
            if (entry.getValue().equals(platform)) {
               entry.getKey().uninitialize(this, entry.getValue());
               it.remove();
               choosePreferred = true;
            }
         }

         if (choosePreferred) {
            this.choosePreferred();
         }
      }

      return removed;
   }

   public synchronized Platform queryCapability(Capability capability) throws NoCapablePlatformException {
      Platform platform = this.preferences.get(Preconditions.checkNotNull(capability));
      if (platform != null) {
         return platform;
      } else if (this.preferences.isEmpty()) {
         throw new NoCapablePlatformException("Not all platforms have been registered yet! Please wait until FastAsyncWorldEdit is initialized.");
      } else {
         throw new NoCapablePlatformException("No platform was found supporting " + capability.name());
      }
   }

   private synchronized void choosePreferred() {
      for (Capability capability : Capability.values()) {
         Platform preferred = this.findMostPreferred(capability);
         if (preferred != null) {
            Platform oldPreferred = this.preferences.put(capability, preferred);
            if (preferred != oldPreferred) {
               if (oldPreferred != null) {
                  capability.uninitialize(this, oldPreferred);
               }

               capability.initialize(this, preferred);
            }
         }
      }

      if (this.preferences.containsKey(Capability.CONFIGURATION) && this.configured.compareAndSet(false, true)) {
         this.worldEdit.getEventBus().post(new ConfigurationLoadEvent(this.queryCapability(Capability.CONFIGURATION).getConfiguration()));
      }
   }

   @Nullable
   private synchronized Platform findMostPreferred(Capability capability) {
      Platform preferred = null;
      Preference highest = null;

      for (Platform platform : this.platforms.keySet()) {
         Preference preference = platform.getCapabilities().get(capability);
         if (preference != null && (highest == null || preference.isPreferredOver(highest))) {
            preferred = platform;
            highest = preference;
         }
      }

      return preferred;
   }

   public synchronized List<Platform> getPlatforms() {
      return new ArrayList<>(this.platforms.keySet());
   }

   public World getWorldForEditing(World base) {
      Preconditions.checkNotNull(base);
      base = WorldWrapper.unwrap(base);
      World match = this.queryCapability(Capability.WORLD_EDITING).matchWorld(base);
      return match != null ? match : base;
   }

   public <T extends Actor> T createProxyActor(T base) {
      Preconditions.checkNotNull(base);
      if (base instanceof Player player) {
         Player permActor = this.queryCapability(Capability.PERMISSIONS).matchPlayer(player);
         if (permActor == null) {
            permActor = player;
         }

         Player cuiActor = this.queryCapability(Capability.WORLDEDIT_CUI).matchPlayer(player);
         if (cuiActor == null) {
            cuiActor = player;
         }

         return player == permActor && player == cuiActor
            ? this.proxyFawe((T)player)
            : this.proxyFawe((T)(new PlayerProxy(player, permActor, cuiActor, this.getWorldForEditing(player.getWorld()))));
      } else {
         return base;
      }
   }

   private <T extends Player> T proxyFawe(T player) {
      return (T)(new LocationMaskedPlayerWrapper(player, player.getLocation(), true));
   }

   public PlatformCommandManager getPlatformCommandManager() {
      return this.platformCommandManager;
   }

   public ListeningExecutorService getExecutorService() {
      return this.executorService.valueOrThrow();
   }

   private static ListeningExecutorService createExecutor() {
      return MoreExecutors.listeningDecorator(EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 20, "WorldEdit Task Executor - %s"));
   }

   public LocalConfiguration getConfiguration() {
      return this.queryCapability(Capability.CONFIGURATION).getConfiguration();
   }

   public Collection<SideEffect> getSupportedSideEffects() {
      return this.queryCapability(Capability.WORLD_EDITING).getSupportedSideEffects();
   }

   public boolean isInitialized() {
      return this.initialized.get();
   }

   @Deprecated
   public void handlePlatformReady(PlatformReadyEvent event) {
      this.handlePlatformsRegistered(new PlatformsRegisteredEvent());
   }

   @Subscribe
   public void handlePlatformsRegistered(PlatformsRegisteredEvent event) {
      this.choosePreferred();
      if (this.initialized.compareAndSet(false, true)) {
         this.worldEdit.getEventBus().post(new PlatformInitializeEvent());
      }
   }

   @Subscribe
   public void handleNewPlatformReady(PlatformReadyEvent event) {
      this.preferences.forEach((cap, platform) -> cap.ready(this, platform));
      this.platforms.put(event.getPlatform(), true);
      if (!this.executorService.isValid()) {
         this.executorService.newValue(createExecutor());
      }
   }

   @Subscribe
   public void handleNewPlatformUnready(PlatformUnreadyEvent event) {
      this.preferences.forEach((cap, platform) -> cap.unready(this, platform));
      this.platforms.put(event.getPlatform(), false);
      if (!this.platforms.containsValue(true)) {
         this.executorService.value().ifPresent(ExecutorService::shutdownNow);
         this.executorService.invalidate();
      }
   }

   private <T extends Tool> T reset(T tool) {
      new PatternTraverser(tool).reset(null);
      return tool;
   }

   @Subscribe
   public void handleBlockInteract(BlockInteractEvent event) {
      Actor actor = this.createProxyActor(event.getCause());
      Location location = event.getLocation();
      if (actor instanceof Player player) {
         LocalSession session = this.worldEdit.getSessionManager().get(actor);
         Request.reset();
         Request.request().setSession(session);
         Request.request().setWorld(player.getWorld());

         try {
            if (event.getType() != Interaction.HIT) {
               if (event.getType() == Interaction.OPEN) {
                  Tool tool = session.getTool(player);
                  if (tool instanceof BlockTool && tool.canUse(player) && player.checkAction()) {
                     player.runAction(
                        () -> {
                           BlockTool blockTool = (BlockTool)tool;
                           if (!(tool instanceof BrushTool)) {
                              blockTool = this.reset(blockTool);
                           }

                           blockTool.actPrimary(
                              this.queryCapability(Capability.WORLD_EDITING), this.getConfiguration(), player, session, location, event.getFace()
                           );
                        },
                        false,
                        true
                     );
                     event.setCancelled(true);
                  }
               }
            } else {
               if (session.hasSuperPickAxe() && player.isHoldingPickAxe()) {
                  BlockTool superPickaxe = session.getSuperPickaxe();
                  if (superPickaxe != null && superPickaxe.canUse(player)) {
                     player.runAction(
                        () -> this.reset(superPickaxe)
                           .actPrimary(this.queryCapability(Capability.WORLD_EDITING), this.getConfiguration(), player, session, location, event.getFace()),
                        false,
                        true
                     );
                     event.setCancelled(true);
                     return;
                  }
               }

               Tool tool = session.getTool(player);
               if (tool instanceof DoubleActionBlockTool && tool.canUse(player)) {
                  player.runAction(
                     () -> ((DoubleActionBlockTool)this.reset((DoubleActionBlockTool)tool))
                        .actSecondary(this.queryCapability(Capability.WORLD_EDITING), this.getConfiguration(), player, session, location, event.getFace()),
                     false,
                     true
                  );
                  event.setCancelled(true);
               }
            }
         } catch (Throwable var10) {
            this.handleThrowable(var10, actor);
            return;
         } finally {
            Request.reset();
         }
      }
   }

   public void handleThrowable(Throwable e, Actor actor) {
      FaweException faweException = FaweException.get(e);
      if (faweException != null) {
         actor.print(Caption.of("fawe.cancel.reason", faweException.getComponent()));
      } else {
         actor.print(Caption.of("worldedit.command.error.report"));
         actor.print(TextComponent.of(e.getClass().getName() + ": " + e.getMessage()));
         LOGGER.error("Error occurred executing player action", e);
      }
   }

   @Subscribe
   public void handlePlayerInput(PlayerInputEvent event) {
      Player player = this.createProxyActor(event.getPlayer());
      LocalSession session = this.worldEdit.getSessionManager().get(player);

      try {
         try {
            switch (event.getInputType()) {
               case PRIMARY:
                  Tool toolx = session.getTool(player);
                  if (!(toolx instanceof DoubleActionTraceTool) || !toolx.canUse(player)) {
                     return;
                  }

                  player.runAsyncIfFree(
                     () -> ((DoubleActionTraceTool)this.reset((DoubleActionTraceTool)tool))
                        .actSecondary(this.queryCapability(Capability.WORLD_EDITING), this.getConfiguration(), player, session)
                  );
                  event.setCancelled(true);
                  return;
               case SECONDARY:
                  Tool tool = session.getTool(player);
                  if (tool instanceof TraceTool && tool.canUse(player)) {
                     player.runAction(
                        () -> ((TraceTool)this.reset((TraceTool)tool))
                           .actPrimary(this.queryCapability(Capability.WORLD_EDITING), this.getConfiguration(), player, session),
                        false,
                        true
                     );
                     event.setCancelled(true);
                     return;
                  }
            }
         } catch (Throwable var8) {
            this.handleThrowable(var8, player);
         }
      } finally {
         Request.reset();
      }
   }
}
