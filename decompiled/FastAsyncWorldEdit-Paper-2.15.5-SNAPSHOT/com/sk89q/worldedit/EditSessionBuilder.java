package com.sk89q.worldedit;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.DisallowedBlocksExtent;
import com.fastasyncworldedit.core.extent.FaweRegionExtent;
import com.fastasyncworldedit.core.extent.HistoryExtent;
import com.fastasyncworldedit.core.extent.LimitExtent;
import com.fastasyncworldedit.core.extent.MultiRegionExtent;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.fastasyncworldedit.core.extent.SingleRegionExtent;
import com.fastasyncworldedit.core.extent.SlowExtent;
import com.fastasyncworldedit.core.extent.StripNBTExtent;
import com.fastasyncworldedit.core.extent.processor.EntityInBlockRemovingProcessor;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightmapProcessor;
import com.fastasyncworldedit.core.extent.processor.lighting.NullRelighter;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightProcessor;
import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;
import com.fastasyncworldedit.core.history.DiskStorageHistory;
import com.fastasyncworldedit.core.history.MemoryOptimizedHistory;
import com.fastasyncworldedit.core.history.RollbackOptimizedHistory;
import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet;
import com.fastasyncworldedit.core.history.changeset.BlockBagChangeSet;
import com.fastasyncworldedit.core.history.changeset.NullChangeSet;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.limit.PropertyRemap;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.regions.RegionWrapper;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.util.Permission;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldedit.util.Identifiable;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public final class EditSessionBuilder {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final SideEffectSet FAST_SIDE_EFFECTS = SideEffectSet.none()
      .with(SideEffect.HEIGHTMAPS)
      .with(SideEffect.LIGHTING, SideEffect.LIGHTING.getDefaultValue());
   private final EventBus eventBus;
   private FaweLimit limit;
   private AbstractChangeSet changeSet;
   private Region[] allowedRegions;
   private Region[] disallowedRegions;
   private Boolean fastMode;
   private Boolean checkMemory;
   private Boolean combineStages;
   private EditSessionEvent event;
   private String command;
   private RelightMode relightMode;
   private Relighter relighter;
   private Boolean wnaMode;
   private Extent bypassHistory;
   private Extent bypassAll;
   private Extent extent;
   private boolean compiled;
   private boolean wrapped;
   private SideEffectSet sideEffectSet = null;
   @Nullable
   private World world;
   private int maxBlocks = -1;
   @Nullable
   private Actor actor;
   @Nullable
   private BlockBag blockBag;
   private boolean tracing;

   EditSessionBuilder(EventBus eventBus) {
      this.eventBus = eventBus;
   }

   public EditSessionBuilder world(@Nullable World world) {
      this.world = world;
      return this.setDirty();
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public int getMaxBlocks() {
      return this.maxBlocks;
   }

   public EditSessionBuilder maxBlocks(int maxBlocks) {
      this.maxBlocks = maxBlocks;
      return this.setDirty();
   }

   @Nullable
   public Actor getActor() {
      return this.actor;
   }

   public EditSessionBuilder actor(@Nullable Actor actor) {
      this.actor = actor;
      return this.setDirty();
   }

   @Nullable
   public BlockBag getBlockBag() {
      return this.blockBag;
   }

   public EditSessionBuilder blockBag(@Nullable BlockBag blockBag) {
      this.blockBag = blockBag;
      return this.setDirty();
   }

   public boolean isTracing() {
      return this.tracing;
   }

   public EditSessionBuilder tracing(boolean tracing) {
      this.tracing = tracing;
      return this.setDirty();
   }

   public <A extends Actor & Locatable> EditSessionBuilder locatableActor(A locatable) {
      Extent extent = locatable.getExtent();
      Preconditions.checkArgument(extent instanceof World, "%s is not located in a World", locatable);
      return this.world((World)extent).actor(locatable);
   }

   public EditSession build() {
      return new EditSession(this);
   }

   private EditSessionBuilder setDirty() {
      this.compiled = false;
      return this;
   }

   public EditSessionBuilder event(@Nullable EditSessionEvent event) {
      this.event = event;
      return this.setDirty();
   }

   public EditSessionBuilder limit(@Nullable FaweLimit limit) {
      this.limit = limit;
      return this.setDirty();
   }

   public EditSessionBuilder limitUnlimited() {
      return this.limit(FaweLimit.MAX.copy());
   }

   public EditSessionBuilder limitUnprocessed(@Nonnull Actor player) {
      this.limitUnlimited();
      FaweLimit tmp = player.getLimit();
      this.limit.INVENTORY_MODE = tmp.INVENTORY_MODE;
      return this.setDirty();
   }

   public EditSessionBuilder changeSet(@Nullable AbstractChangeSet changeSet) {
      this.changeSet = changeSet;
      return this.setDirty();
   }

   public EditSessionBuilder changeSetNull() {
      return this.changeSet(new NullChangeSet(this.world));
   }

   public EditSessionBuilder command(String command) {
      this.command = command;
      return this;
   }

   public EditSessionBuilder changeSet(boolean disk, @Nullable UUID uuid) {
      if (disk) {
         if (Settings.settings().HISTORY.USE_DATABASE) {
            this.changeSet = new RollbackOptimizedHistory(this.world, uuid);
         } else {
            this.changeSet = new DiskStorageHistory(this.world, uuid);
         }
      } else {
         this.changeSet = new MemoryOptimizedHistory(this.world);
      }

      return this.setDirty();
   }

   public EditSessionBuilder allowedRegions(@Nullable Region[] allowedRegions) {
      this.allowedRegions = allowedRegions;
      return this.setDirty();
   }

   @Deprecated
   public EditSessionBuilder allowedRegions(@Nullable RegionWrapper[] allowedRegions) {
      this.allowedRegions = allowedRegions;
      return this.setDirty();
   }

   public EditSessionBuilder allowedRegions(@Nullable RegionWrapper allowedRegion) {
      this.allowedRegions = allowedRegion == null ? null : allowedRegion.toArray();
      return this.setDirty();
   }

   public EditSessionBuilder disallowedRegions(@Nullable Region[] disallowedRegions) {
      this.disallowedRegions = disallowedRegions;
      return this.setDirty();
   }

   @Deprecated
   public EditSessionBuilder disallowedRegions(@Nullable RegionWrapper[] disallowedRegions) {
      this.disallowedRegions = disallowedRegions;
      return this.setDirty();
   }

   public EditSessionBuilder disallowedRegions(@Nullable RegionWrapper disallowedRegion) {
      this.disallowedRegions = disallowedRegion == null ? null : disallowedRegion.toArray();
      return this.setDirty();
   }

   public EditSessionBuilder allowedRegionsEverywhere() {
      return this.allowedRegions(new Region[]{RegionWrapper.GLOBAL()});
   }

   public EditSessionBuilder fastMode(@Nullable Boolean fastMode) {
      this.fastMode = fastMode;
      return this.setDirty();
   }

   public EditSessionBuilder relightMode(@Nullable RelightMode relightMode) {
      this.relightMode = relightMode;
      return this.setDirty();
   }

   public EditSessionBuilder checkMemory(@Nullable Boolean checkMemory) {
      this.checkMemory = checkMemory;
      return this.setDirty();
   }

   public EditSessionBuilder combineStages(@Nullable Boolean combineStages) {
      this.combineStages = combineStages;
      return this.setDirty();
   }

   public EditSessionBuilder setSideEffectSet(@Nullable SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
      return this.setDirty();
   }

   public EditSessionBuilder compile() {
      if (this.compiled) {
         return this;
      } else {
         this.compiled = true;
         this.wrapped = false;
         if (this.event == null) {
            this.event = new EditSessionEvent(this.world, this.actor, -1, null);
         }

         if (this.actor == null && this.event.getActor() != null) {
            this.actor = this.event.getActor();
         }

         if (this.limit == null) {
            if (this.actor == null) {
               this.limit = FaweLimit.MAX;
            } else {
               this.limit = this.actor.getLimit();
            }
         }

         if (this.fastMode == null) {
            if (this.actor == null) {
               this.fastMode = !Settings.settings().HISTORY.ENABLE_FOR_CONSOLE;
            } else {
               this.fastMode = this.actor.getSession().hasFastMode();
            }
         }

         if (this.sideEffectSet == null) {
            this.sideEffectSet = this.fastMode ? FAST_SIDE_EFFECTS : SideEffectSet.defaults();
         }

         if (!this.fastMode && this.actor != null) {
            this.sideEffectSet = this.sideEffectSet
               .with(SideEffect.ENTITY_EVENTS, this.limit.SKIP_ENTITY_SPAWN_EVENTS ? SideEffect.State.OFF : SideEffect.State.ON);
         }

         if (this.checkMemory == null) {
            this.checkMemory = this.actor != null && !this.fastMode;
         }

         if (this.checkMemory && MemUtil.isMemoryLimitedSlow()) {
            if (this.actor != null && Permission.hasPermission(this.actor, "worldedit.fast")) {
               this.actor.print(Caption.of("fawe.info.worldedit.oom.admin"));
            } else {
               LOGGER.warn("Low memory");
            }

            throw FaweCache.LOW_MEMORY;
         } else {
            this.blockBag = this.limit.INVENTORY_MODE != 0 ? this.blockBag : null;
            this.limit = this.limit.copy();
            if (this.extent == null) {
               IQueueExtent<IQueueChunk> queue = null;
               World unwrapped = WorldWrapper.unwrap(this.world);
               boolean placeChunks = (this.fastMode || this.limit.FAST_PLACEMENT) && (this.wnaMode == null || !this.wnaMode);
               if (!placeChunks) {
                  this.wnaMode = true;
                  this.extent = this.world;
               } else {
                  this.wnaMode = false;
                  if (unwrapped instanceof IQueueExtent) {
                     this.extent = queue = (IQueueExtent<IQueueChunk>)unwrapped;
                  } else if (Settings.settings().QUEUE.PARALLEL_THREADS > 1 && !Fawe.isMainThread()) {
                     ParallelQueueExtent parallel = new ParallelQueueExtent(Fawe.instance().getQueueHandler(), this.world, this.fastMode, this.sideEffectSet);
                     queue = parallel.getExtent();
                     this.extent = parallel;
                  } else {
                     this.extent = queue = Fawe.instance().getQueueHandler().getQueue(this.world);
                  }

                  queue.setSideEffectSet(this.sideEffectSet);
               }

               if (this.combineStages == null) {
                  this.combineStages = Settings.settings().HISTORY.COMBINE_STAGES && this.limit.FAST_PLACEMENT && this.blockBag == null;
               }

               this.extent = this.bypassAll = this.wrapExtent(this.extent, this.eventBus, this.event, EditSession.Stage.BEFORE_CHANGE);
               this.bypassHistory = this.extent = this.wrapExtent(this.bypassAll, this.eventBus, this.event, EditSession.Stage.BEFORE_REORDER);
               if (!this.fastMode || this.sideEffectSet.shouldApply(SideEffect.HISTORY) || this.changeSet != null) {
                  if (this.changeSet == null) {
                     if (Settings.settings().HISTORY.USE_DISK) {
                        UUID uuid = this.actor == null ? Identifiable.CONSOLE : this.actor.getUniqueId();
                        if (Settings.settings().HISTORY.USE_DATABASE) {
                           this.changeSet = new RollbackOptimizedHistory(this.world, uuid);
                        } else {
                           this.changeSet = new DiskStorageHistory(this.world, uuid);
                        }
                     } else {
                        if (this.combineStages && Settings.settings().HISTORY.COMPRESSION_LEVEL == 0) {
                        }

                        this.changeSet = new MemoryOptimizedHistory(this.world);
                     }
                  }

                  if (this.limit.SPEED_REDUCTION > 0) {
                     this.extent = this.bypassHistory = new SlowExtent(this.bypassHistory, this.limit.SPEED_REDUCTION);
                  }

                  if (this.command != null && this.changeSet instanceof RollbackOptimizedHistory) {
                     ((RollbackOptimizedHistory)this.changeSet).setCommand(this.command);
                  }

                  if (!(this.changeSet instanceof NullChangeSet)) {
                     if (this.blockBag != null) {
                        this.changeSet = new BlockBagChangeSet(this.changeSet, this.blockBag, this.limit.INVENTORY_MODE == 1);
                     }

                     if (this.combineStages) {
                        this.extent = this.extent.enableHistory(this.changeSet);
                     } else {
                        this.extent = new HistoryExtent(this.extent, this.changeSet);
                     }
                  }
               }

               if (this.allowedRegions == null
                  && Settings.settings().REGION_RESTRICTIONS
                  && this.actor != null
                  && !this.actor.hasPermission("fawe.bypass.regions")
                  && this.actor instanceof Player player) {
                  this.allowedRegions = player.getAllowedRegions();
               }

               if (this.disallowedRegions == null
                  && Settings.settings().REGION_RESTRICTIONS
                  && Settings.settings().REGION_RESTRICTIONS_OPTIONS.ALLOW_BLACKLISTS
                  && this.actor != null
                  && !this.actor.hasPermission("fawe.bypass.regions")
                  && this.actor instanceof Player player) {
                  this.disallowedRegions = player.getDisallowedRegions();
               }

               if (!placeChunks) {
                  this.relighter = NullRelighter.INSTANCE;
               } else {
                  if (this.sideEffectSet.shouldApply(SideEffect.LIGHTING) || this.relightMode != null && this.relightMode != RelightMode.NONE) {
                     this.relighter = WorldEdit.getInstance()
                        .getPlatformManager()
                        .queryCapability(Capability.WORLD_EDITING)
                        .getRelighterFactory()
                        .createRelighter(this.relightMode, this.world, queue);
                     queue.addProcessor(new RelightProcessor(this.relighter));
                  }

                  if (this.sideEffectSet.shouldApply(SideEffect.HEIGHTMAPS)) {
                     queue.addProcessor(new HeightmapProcessor(this.world.getMinY(), this.world.getMaxY()));
                  }

                  if (this.sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
                     Region region = (Region)(this.allowedRegions != null && this.allowedRegions.length != 0
                        ? (this.allowedRegions.length == 1 ? this.allowedRegions[0] : new RegionIntersection(this.allowedRegions))
                        : null);
                     queue.addProcessor(
                        WorldEdit.getInstance()
                           .getPlatformManager()
                           .queryCapability(Capability.WORLD_EDITING)
                           .getPlatformPlacementProcessor(this.extent, null, region)
                     );
                  }

                  if (!Settings.settings().EXPERIMENTAL.KEEP_ENTITIES_IN_BLOCKS) {
                     queue.addProcessor(new EntityInBlockRemovingProcessor());
                  }

                  IBatchProcessor platformProcessor = WorldEdit.getInstance()
                     .getPlatformManager()
                     .queryCapability(Capability.WORLD_EDITING)
                     .getPlatformProcessor(this.fastMode);
                  if (platformProcessor != null) {
                     queue.addProcessor(platformProcessor);
                  }

                  IBatchProcessor platformPostProcessor = WorldEdit.getInstance()
                     .getPlatformManager()
                     .queryCapability(Capability.WORLD_EDITING)
                     .getPlatformPostProcessor(this.fastMode);
                  if (platformPostProcessor != null) {
                     queue.addPostProcessor(platformPostProcessor);
                  }
               }

               if (this.limit != null && this.limit.STRIP_NBT != null && !this.limit.STRIP_NBT.isEmpty()) {
                  StripNBTExtent ext = new StripNBTExtent(this.extent, this.limit.STRIP_NBT);
                  if (placeChunks) {
                     queue.addProcessor(ext);
                  }

                  if (!placeChunks || !this.combineStages) {
                     this.extent = ext;
                  }
               }

               if (this.limit != null && !this.limit.isUnlimited()) {
                  Set<String> limitBlocks = new HashSet<>();
                  if (this.getActor() != null && !this.getActor().hasPermission("worldedit.anyblock") && this.limit.UNIVERSAL_DISALLOWED_BLOCKS) {
                     limitBlocks.addAll(WorldEdit.getInstance().getConfiguration().disallowedBlocks);
                  }

                  if (this.limit.DISALLOWED_BLOCKS != null && !this.limit.DISALLOWED_BLOCKS.isEmpty()) {
                     limitBlocks.addAll(this.limit.DISALLOWED_BLOCKS);
                  }

                  Set<PropertyRemap<?>> remaps = this.limit.REMAP_PROPERTIES;
                  if (!limitBlocks.isEmpty() || remaps != null && !remaps.isEmpty()) {
                     DisallowedBlocksExtent extx = new DisallowedBlocksExtent(this.extent, limitBlocks, remaps);
                     if (placeChunks) {
                        queue.addProcessor(extx);
                     }

                     if (!placeChunks || !this.combineStages) {
                        this.extent = extx;
                     }
                  }
               }

               FaweRegionExtent regionExtent = null;
               if (this.allowedRegions != null && this.allowedRegions.length == 0) {
                  regionExtent = new NullExtent(this.extent, FaweCache.NO_REGION);
               } else if (this.disallowedRegions != null && this.disallowedRegions.length != 0) {
                  regionExtent = new MultiRegionExtent(this.extent, this.limit, this.allowedRegions, this.disallowedRegions);
               } else if (this.allowedRegions == null) {
                  this.allowedRegions = new Region[]{RegionWrapper.GLOBAL()};
               } else if (this.allowedRegions.length == 1) {
                  regionExtent = new SingleRegionExtent(this.extent, this.limit, this.allowedRegions[0]);
               } else {
                  regionExtent = new MultiRegionExtent(this.extent, this.limit, this.allowedRegions, null);
               }

               if (regionExtent != null) {
                  if (placeChunks) {
                     queue.addProcessor(regionExtent);
                  }

                  if (!placeChunks || !this.combineStages) {
                     this.extent = regionExtent;
                  }
               }

               Consumer<Component> onErrorMessage;
               if (this.getActor() != null) {
                  onErrorMessage = c -> this.getActor().print(Caption.of("fawe.error.occurred-continuing", c));
               } else {
                  onErrorMessage = c -> {};
               }

               if (this.limit != null && !this.limit.isUnlimited()) {
                  this.extent = new LimitExtent(this.extent, this.limit, onErrorMessage, placeChunks && this.combineStages);
                  if (placeChunks && this.combineStages) {
                     queue.addProcessor((LimitExtent)this.extent);
                  }
               }

               this.extent = this.wrapExtent(this.extent, this.eventBus, this.event, EditSession.Stage.BEFORE_HISTORY);
            }

            return this;
         }
      }
   }

   public Relighter getRelighter() {
      return this.relighter;
   }

   public boolean isWNAMode() {
      return this.wnaMode;
   }

   @Nullable
   public Region[] getAllowedRegions() {
      return this.allowedRegions;
   }

   public EditSessionBuilder forceWNA() {
      this.wnaMode = true;
      return this.setDirty();
   }

   public boolean isWrapped() {
      return this.wrapped;
   }

   public Extent getBypassHistory() {
      return this.bypassHistory;
   }

   public Extent getBypassAll() {
      return this.bypassAll;
   }

   @Nonnull
   public FaweLimit getLimit() {
      return this.limit;
   }

   public AbstractChangeSet getChangeTask() {
      return this.changeSet;
   }

   public SideEffectSet getSideEffectSet() {
      return this.sideEffectSet;
   }

   public Extent getExtent() {
      return (Extent)(this.extent != null ? this.extent : this.world);
   }

   private Extent wrapExtent(final Extent extent, final EventBus eventBus, EditSessionEvent event, final EditSession.Stage stage) {
      event = event.clone(stage);
      event.setExtent(extent);
      eventBus.post(event);
      if (event.isCancelled()) {
         return new NullExtent(extent, FaweCache.MANUAL);
      } else {
         Extent toReturn = event.getExtent();
         if (toReturn instanceof com.sk89q.worldedit.extent.NullExtent) {
            return new NullExtent(toReturn, FaweCache.MANUAL);
         } else {
            if (toReturn != extent) {
               String className = toReturn.getClass().getName().toLowerCase(Locale.ROOT);

               for (String allowed : Settings.settings().EXTENT.ALLOWED_PLUGINS) {
                  if (className.contains(allowed.toLowerCase(Locale.ROOT))) {
                     this.wrapped = true;
                     return toReturn;
                  }
               }

               if (Settings.settings().EXTENT.DEBUG) {
                  String blockedClassName = toReturn.getClass().getName();
                  if (event.getActor() != null) {
                     event.getActor().printDebug(TextComponent.of("Potentially unsafe extent blocked: " + blockedClassName));
                     event.getActor()
                        .print(TextComponent.of("- For area restrictions and block logging, it is recommended that third party plugins use the FAWE API"));
                     event.getActor()
                        .print(
                           TextComponent.of("- Add the following line to the `allowed-plugins` list in the FAWE config.yml to let FAWE recognize the extent:")
                        );
                     event.getActor().print(blockedClassName);
                  } else {
                     LOGGER.warn(
                        "Potentially unsafe extent blocked: {}\n - For area restrictions and block logging, it is recommended that third party plugins use the FAWE API\n - Add the following classpath to the `allowed-plugins` list in the FAWE config.yml to let FAWE recognize the extent:\n{}",
                        blockedClassName,
                        blockedClassName
                     );
                  }
               }
            }

            return extent;
         }
      }
   }
}
