package com.sk89q.worldedit;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder;
import com.fastasyncworldedit.core.history.DiskStorageHistory;
import com.fastasyncworldedit.core.internal.exception.FaweClipboardVersionMismatchException;
import com.fastasyncworldedit.core.internal.io.FaweInputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.math.sparsebits.SparseBitSet;
import com.fastasyncworldedit.core.util.BrushCache;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.TextureHolder;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.NavigationWand;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.internal.cui.ServerCUIHandler;
import com.sk89q.worldedit.jchronic.Chronic;
import com.sk89q.worldedit.jchronic.Options;
import com.sk89q.worldedit.jchronic.utils.Span;
import com.sk89q.worldedit.jchronic.utils.Time;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Identifiable;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

public class LocalSession implements TextureHolder {
   public static int MAX_HISTORY_SIZE = 15;
   private static final int CUI_VERSION_UNINITIALIZED = -1;
   private transient LocalConfiguration config;
   private final transient AtomicBoolean dirty = new AtomicBoolean();
   private transient int failedCuiAttempts = 0;
   private transient boolean hasCUISupport = false;
   private transient int cuiVersion = -1;
   private RegionSelector selector = new CuboidRegionSelector();
   private transient boolean placeAtPos1 = false;
   private final transient List<Object> history = Collections.synchronizedList(new LinkedList<Object>() {
      @Override
      public Object get(int index) {
         Object value = super.get(index);
         if (value instanceof Integer) {
            value = LocalSession.this.getChangeSet(value);
            this.set(index, value);
         }

         return value;
      }

      @Override
      public Object remove(int index) {
         return LocalSession.this.getChangeSet(super.remove(index));
      }
   });
   private transient volatile Integer historyNegativeIndex;
   private final transient ReentrantLock historyWriteLock = new ReentrantLock(true);
   private final transient Int2ObjectOpenHashMap<Tool> tools = new Int2ObjectOpenHashMap(0);
   private transient Mask sourceMask;
   private transient TextureUtil texture;
   private transient ResettableExtent transform = null;
   private transient World currentWorld;
   private transient boolean fastMode = false;
   private transient ClipboardHolder clipboard;
   private final transient Object clipboardLock = new Object();
   private transient boolean superPickaxe = false;
   private transient BlockTool pickaxeMode = new SinglePickaxe();
   private transient int maxBlocksChanged = -1;
   private transient int maxTimeoutTime;
   private transient boolean useInventory;
   private transient Snapshot snapshot;
   private transient com.sk89q.worldedit.world.snapshot.experimental.Snapshot snapshotExperimental;
   private transient SideEffectSet sideEffectSet = SideEffectSet.defaults();
   private transient Mask mask;
   private transient ZoneId timezone = ZoneId.systemDefault();
   private transient UUID uuid;
   private transient volatile long historySize = 0L;
   private transient BlockVector3 cuiTemporaryBlock;
   private final transient EditSession.ReorderMode reorderMode = EditSession.ReorderMode.MULTI_STAGE;
   private transient List<Countable<BlockState>> lastDistribution;
   private transient World worldOverride;
   private transient boolean tickingWatchdog = false;
   private transient boolean hasBeenToldVersion;
   private transient boolean tracingActions;
   private String lastScript;
   private RegionSelectorType defaultSelector;
   private boolean useServerCUI = false;
   private BaseItem wandItem;
   private BaseItem navWandItem;
   private transient boolean loadDefaults = true;

   public LocalSession() {
   }

   public LocalSession(@Nullable LocalConfiguration config) {
      this.config = config;
   }

   public void setConfiguration(LocalConfiguration config) {
      Preconditions.checkNotNull(config);
      this.config = config;
   }

   public void postLoad() {
      if (this.defaultSelector != null) {
         this.selector = this.defaultSelector.createSelector();
      }

      if (this.worldOverride != null) {
         this.selector.setWorld(this.worldOverride);
      } else {
         this.selector.setWorld(this.currentWorld);
      }
   }

   public boolean loadSessionHistoryFromDisk(UUID uuid, World world) {
      if (world != null && uuid != null) {
         if (Settings.settings().HISTORY.USE_DISK) {
            MAX_HISTORY_SIZE = Integer.MAX_VALUE;
         }

         world = WorldWrapper.unwrap(world);
         if (!world.equals(this.currentWorld)) {
            this.uuid = uuid;
            this.saveHistoryNegativeIndex(uuid, this.currentWorld);
            this.history.clear();
            this.currentWorld = world;
            if (this.loadHistoryChangeSets(uuid, this.currentWorld)) {
               this.loadHistoryNegativeIndex(uuid, this.currentWorld);
               return true;
            }

            this.historyNegativeIndex = 0;
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean loadHistoryChangeSets(UUID uuid, World world) {
      SparseBitSet set = new SparseBitSet();
      File folder = MainUtil.getFile(
         Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HISTORY + File.separator + world.getName() + File.separator + uuid
      );
      if (folder.isDirectory()) {
         folder.listFiles(pathname -> {
            String name = pathname.getName();
            Integer val = null;
            if (pathname.isDirectory()) {
               val = StringMan.toInteger(name, 0, name.length());
            } else {
               int i = name.lastIndexOf(46);
               if (i != -1) {
                  val = StringMan.toInteger(name, 0, i);
               }
            }

            if (val != null) {
               set.set(val);
            }

            return false;
         });
      }

      if (!set.isEmpty()) {
         this.historySize = MainUtil.getTotalSize(folder.toPath());

         for (int index = set.nextSetBit(0); index != -1; index = set.nextSetBit(index + 1)) {
            this.history.add(index);
         }
      } else {
         this.historySize = 0L;
      }

      return !set.isEmpty();
   }

   private void loadHistoryNegativeIndex(UUID uuid, World world) {
      if (Settings.settings().HISTORY.USE_DISK) {
         File file = MainUtil.getFile(
            Fawe.<IFawe>platform().getDirectory(),
            Settings.settings().PATHS.HISTORY + File.separator + world.getName() + File.separator + uuid + File.separator + "index"
         );
         if (file.exists()) {
            try (FaweInputStream is = new FaweInputStream(new FileInputStream(file))) {
               this.historyNegativeIndex = Math.min(Math.max(0, is.readInt()), this.history.size());
            } catch (IOException var9) {
               var9.printStackTrace();
            }
         } else {
            this.historyNegativeIndex = 0;
         }
      }
   }

   private void saveHistoryNegativeIndex(UUID uuid, World world) {
      if (world != null && Settings.settings().HISTORY.USE_DISK) {
         File file = MainUtil.getFile(
            Fawe.<IFawe>platform().getDirectory(),
            Settings.settings().PATHS.HISTORY + File.separator + world.getNameUnsafe() + File.separator + uuid + File.separator + "index"
         );
         if (this.getHistoryNegativeIndex() != 0) {
            try {
               if (!file.exists()) {
                  file.getParentFile().mkdirs();
                  file.createNewFile();
               }

               try (FaweOutputStream os = new FaweOutputStream(new FileOutputStream(file))) {
                  os.writeInt(this.getHistoryNegativeIndex());
               }
            } catch (IOException var9) {
               var9.printStackTrace();
            }
         } else if (file.exists()) {
            file.delete();
         }
      }
   }

   public boolean isDirty() {
      return this.dirty.get();
   }

   private void setDirty() {
      this.dirty.set(true);
   }

   public int getHistoryIndex() {
      return this.history.size() - 1 - (this.historyNegativeIndex == null ? 0 : this.historyNegativeIndex);
   }

   public int getHistoryNegativeIndex() {
      return this.historyNegativeIndex == null ? (this.historyNegativeIndex = 0) : this.historyNegativeIndex;
   }

   public List<ChangeSet> getHistory() {
      return this.history.stream().map(this::getChangeSet).collect(Collectors.toList());
   }

   public boolean save() {
      this.saveHistoryNegativeIndex(this.uuid, this.currentWorld);
      if (this.defaultSelector == RegionSelectorType.CUBOID) {
         this.defaultSelector = null;
      }

      return this.lastScript != null || this.defaultSelector != null;
   }

   public boolean compareAndResetDirty() {
      return this.dirty.compareAndSet(true, false);
   }

   public ZoneId getTimeZone() {
      return this.timezone;
   }

   public void setTimezone(ZoneId timezone) {
      Preconditions.checkNotNull(timezone);
      this.timezone = timezone;
   }

   public void clearHistory() {
      boolean mainThread = Fawe.isMainThread();
      if (mainThread && !this.historyWriteLock.tryLock()) {
         TaskManager.taskManager().async(this::clearHistoryTask);
      } else {
         try {
            this.clearHistoryTask();
         } finally {
            if (mainThread) {
               this.historyWriteLock.unlock();
            }
         }
      }
   }

   private void clearHistoryTask() {
      this.historyWriteLock.lock();

      try {
         for (Object item : this.history) {
            this.getChangeSet(item).delete();
         }

         this.history.clear();
      } finally {
         this.historyWriteLock.unlock();
      }

      this.historyNegativeIndex = 0;
      this.save();
      this.historySize = 0L;
      this.currentWorld = null;
   }

   public void remember(EditSession editSession) {
      Preconditions.checkNotNull(editSession);
      if (editSession.size() != 0) {
         Actor actor = editSession.getActor();
         int limit = actor == null ? Integer.MAX_VALUE : actor.getLimit().MAX_HISTORY;
         this.remember(editSession, true, limit);
      }
   }

   private ChangeSet getChangeSet(Object o) {
      if (o instanceof ChangeSet cs) {
         try {
            cs.close();
            return cs;
         } catch (IOException var4) {
            var4.printStackTrace();
            return null;
         }
      } else {
         if (o instanceof Integer) {
            File folder = MainUtil.getFile(
               Fawe.<IFawe>platform().getDirectory(),
               Settings.settings().PATHS.HISTORY + File.separator + this.currentWorld.getName() + File.separator + this.uuid
            );
            File specific = new File(folder, o.toString());
            if (!specific.isDirectory()) {
               return new DiskStorageHistory(this.currentWorld, this.uuid, (Integer)o);
            }
         }

         return null;
      }
   }

   public void remember(Identifiable player, World world, ChangeSet changeSet, FaweLimit limit) {
      this.historyWriteLock.lock();

      try {
         if (Settings.settings().HISTORY.USE_DISK) {
            MAX_HISTORY_SIZE = Integer.MAX_VALUE;
         }

         if (changeSet.longSize() == 0L) {
            return;
         }

         this.loadSessionHistoryFromDisk(player.getUniqueId(), world);
         if (changeSet instanceof ChangeSet) {
            ListIterator<Object> iter = this.history.listIterator();
            int i = 0;
            int cutoffIndex = this.history.size() - this.getHistoryNegativeIndex();

            while (iter.hasNext()) {
               Object item = iter.next();
               if (++i > cutoffIndex) {
                  ChangeSet oldChangeSet;
                  if (item instanceof ChangeSet) {
                     oldChangeSet = (ChangeSet)item;
                  } else {
                     oldChangeSet = this.getChangeSet(item);
                  }

                  this.historySize = this.historySize - MainUtil.getSize(oldChangeSet);
                  iter.remove();
               }
            }
         }

         this.historySize = this.historySize + MainUtil.getSize(changeSet);
         this.history.add(changeSet);
         if (this.getHistoryNegativeIndex() != 0) {
            this.setDirty();
            this.historyNegativeIndex = 0;
         }

         if (limit != null) {
            int limitMb = limit.MAX_HISTORY;

            while (
               (!Settings.settings().HISTORY.USE_DISK && this.history.size() > MAX_HISTORY_SIZE || this.historySize >> 20 > limitMb) && this.history.size() > 1
            ) {
               ChangeSet item = (ChangeSet)this.history.remove(0);
               item.delete();
               long size = MainUtil.getSize(item);
               this.historySize -= size;
            }
         }
      } finally {
         this.historyWriteLock.unlock();
      }
   }

   public void remember(EditSession editSession, boolean append, int limitMb) {
      this.historyWriteLock.lock();

      try {
         if (Settings.settings().HISTORY.USE_DISK) {
            MAX_HISTORY_SIZE = Integer.MAX_VALUE;
         }

         editSession.flushQueue();
         if (editSession.getChangeSet() == null || limitMb == 0 || this.historySize >> 20 > limitMb && !append) {
            return;
         }

         ChangeSet changeSet = editSession.getChangeSet();
         if (!changeSet.isEmpty()) {
            Actor actor = editSession.getActor();
            if (actor != null) {
               this.loadSessionHistoryFromDisk(actor.getUniqueId(), editSession.getWorld());
            }

            if (append) {
               ListIterator<Object> iter = this.history.listIterator();
               int i = 0;
               int cutoffIndex = this.history.size() - this.getHistoryNegativeIndex();

               while (iter.hasNext()) {
                  Object item = iter.next();
                  if (++i > cutoffIndex) {
                     ChangeSet oldChangeSet;
                     if (item instanceof ChangeSet) {
                        oldChangeSet = (ChangeSet)item;
                     } else {
                        oldChangeSet = this.getChangeSet(item);
                     }

                     this.historySize = this.historySize - MainUtil.getSize(oldChangeSet);
                     iter.remove();
                  }
               }
            }

            this.historySize = this.historySize + MainUtil.getSize(changeSet);
            if (append) {
               this.history.add(changeSet);
               if (this.getHistoryNegativeIndex() != 0) {
                  this.setDirty();
                  this.historyNegativeIndex = 0;
               }
            } else {
               this.history.add(0, changeSet);
            }

            while (
               (!Settings.settings().HISTORY.USE_DISK && this.history.size() > MAX_HISTORY_SIZE || this.historySize >> 20 > limitMb) && this.history.size() > 1
            ) {
               ChangeSet item = (ChangeSet)this.history.remove(0);
               item.delete();
               long size = MainUtil.getSize(item);
               this.historySize -= size;
            }

            new MaskTraverser(this.mask).reset(NullExtent.INSTANCE);
            return;
         }
      } finally {
         this.historyWriteLock.unlock();
      }
   }

   public EditSession undo(@Nullable BlockBag newBlockBag, Actor actor) {
      Preconditions.checkNotNull(actor);
      World world = actor instanceof Player ? ((Player)actor).getWorldForEditing() : this.getWorldOverride();
      if (world == null) {
         throw new MissingWorldException();
      } else {
         this.loadSessionHistoryFromDisk(actor.getUniqueId(), world);
         if (this.getHistoryNegativeIndex() < this.history.size()) {
            ChangeSet changeSet = this.getChangeSet(this.history.get(this.getHistoryIndex()));
            EditSessionBuilder builder = WorldEdit.getInstance()
               .newEditSessionBuilder()
               .world(world)
               .checkMemory(false)
               .changeSetNull()
               .fastMode(false)
               .limitUnprocessed(actor)
               .actor(actor);
            if (actor instanceof Player) {
               builder = builder.blockBag(this.getBlockBag((Player)actor));
            }

            if (!actor.getLimit().RESTRICT_HISTORY_TO_REGIONS) {
               builder = builder.allowedRegionsEverywhere();
            }

            EditSession var12;
            try (EditSession newEditSession = builder.build()) {
               newEditSession.setBlocks(changeSet, ChangeSetExecutor.Type.UNDO);
               this.setDirty();
               Integer var7 = this.historyNegativeIndex;
               this.historyNegativeIndex = this.historyNegativeIndex + 1;
               var12 = newEditSession;
            }

            return var12;
         } else {
            int size = this.history.size();
            if (this.getHistoryNegativeIndex() != size) {
               this.historyNegativeIndex = this.history.size();
               this.setDirty();
            }

            return null;
         }
      }
   }

   public EditSession redo(@Nullable BlockBag newBlockBag, Actor actor) {
      Preconditions.checkNotNull(actor);
      World world = actor instanceof Player ? ((Player)actor).getWorldForEditing() : this.getWorldOverride();
      if (world == null) {
         throw new MissingWorldException();
      } else {
         this.loadSessionHistoryFromDisk(actor.getUniqueId(), world);
         if (this.getHistoryNegativeIndex() > 0) {
            this.setDirty();
            Integer changeSet = this.historyNegativeIndex;
            this.historyNegativeIndex = this.historyNegativeIndex - 1;
            ChangeSet changeSetx = this.getChangeSet(this.history.get(this.getHistoryIndex()));
            EditSessionBuilder builder = WorldEdit.getInstance()
               .newEditSessionBuilder()
               .world(world)
               .checkMemory(false)
               .changeSetNull()
               .fastMode(false)
               .limitUnprocessed(actor)
               .actor(actor);
            if (actor instanceof Player) {
               builder = builder.blockBag(this.getBlockBag((Player)actor));
            }

            if (!actor.getLimit().RESTRICT_HISTORY_TO_REGIONS) {
               builder = builder.allowedRegionsEverywhere();
            }

            EditSession var7;
            try (EditSession newEditSession = builder.build()) {
               newEditSession.setBlocks(changeSetx, ChangeSetExecutor.Type.REDO);
               var7 = newEditSession;
            }

            return var7;
         } else {
            return null;
         }
      }
   }

   public boolean hasWorldOverride() {
      return this.worldOverride != null;
   }

   @Nullable
   public World getWorldOverride() {
      return this.worldOverride;
   }

   public void setWorldOverride(@Nullable World worldOverride) {
      this.worldOverride = worldOverride;
   }

   public boolean isTickingWatchdog() {
      return this.tickingWatchdog;
   }

   public void setTickingWatchdog(boolean tickingWatchdog) {
      this.tickingWatchdog = tickingWatchdog;
   }

   public boolean isTracingActions() {
      return this.tracingActions;
   }

   public void setTracingActions(boolean tracingActions) {
      this.tracingActions = tracingActions;
   }

   public RegionSelectorType getDefaultRegionSelector() {
      return this.defaultSelector;
   }

   public void setDefaultRegionSelector(RegionSelectorType defaultSelector) {
      Preconditions.checkNotNull(defaultSelector);
      this.defaultSelector = defaultSelector;
      this.setDirty();
   }

   public RegionSelector getRegionSelector(World world) {
      Preconditions.checkNotNull(world);
      if (this.selector.getWorld() == null || !this.selector.getWorld().equals(world)) {
         this.selector.setWorld(world);
         this.selector.clear();
         if (this.hasWorldOverride() && !world.equals(this.getWorldOverride())) {
            this.setWorldOverride(null);
         }
      }

      return this.selector;
   }

   public void setRegionSelector(World world, RegionSelector selector) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(selector);
      selector.setWorld(world);
      this.selector = selector;
      this.setDirty();
      if (this.hasWorldOverride() && !world.equals(this.getWorldOverride())) {
         this.setWorldOverride(null);
      }
   }

   public boolean isSelectionDefined(World world) {
      Preconditions.checkNotNull(world);
      return this.selector.getIncompleteRegion().getWorld() != null && this.selector.getIncompleteRegion().getWorld().equals(world)
         ? this.selector.isDefined()
         : false;
   }

   public Region getSelection() throws IncompleteRegionException {
      return this.getSelection(this.getSelectionWorld());
   }

   public Region getSelection(@Nullable World world) throws IncompleteRegionException {
      if (world != null && this.selector.getIncompleteRegion().getWorld() != null && this.selector.getIncompleteRegion().getWorld().equals(world)) {
         return this.selector.getRegion();
      } else {
         throw new IncompleteRegionException() {
            @Override
            public synchronized Throwable fillInStackTrace() {
               return this;
            }
         };
      }
   }

   @Nullable
   public World getSelectionWorld() {
      World world = this.selector.getIncompleteRegion().getWorld();
      return world instanceof WorldWrapper ? ((WorldWrapper)world).getParent() : world;
   }

   public ClipboardHolder getClipboard() throws EmptyClipboardException {
      synchronized (this.clipboardLock) {
         if (this.clipboard == null) {
            throw new EmptyClipboardException();
         } else {
            return this.clipboard;
         }
      }
   }

   @Nullable
   public ClipboardHolder getExistingClipboard() {
      synchronized (this.clipboardLock) {
         return this.clipboard == null ? null : this.clipboard;
      }
   }

   public void addClipboard(@Nonnull MultiClipboardHolder toAppend) {
      Preconditions.checkNotNull(toAppend);
      ClipboardHolder existing = this.getExistingClipboard();
      if (existing instanceof MultiClipboardHolder multi) {
         for (ClipboardHolder holder : toAppend.getHolders()) {
            multi.add(holder);
         }
      } else {
         multi = toAppend;
         if (existing != null) {
            toAppend.add(existing);
         }
      }

      this.setClipboard(multi);
   }

   public void flushClipboard() {
      synchronized (this.clipboardLock) {
         if (this.clipboard != null) {
            try {
               this.clipboard.flush();
            } catch (IOException var4) {
               var4.printStackTrace();
            }
         }
      }
   }

   public void loadClipboardFromDisk(File file) throws FaweClipboardVersionMismatchException, ExecutionException, InterruptedException {
      synchronized (this.clipboardLock) {
         if (file.exists() && file.length() > 5L) {
            try {
               if (this.getClipboard() != null) {
                  return;
               }
            } catch (EmptyClipboardException var7) {
            }

            DiskOptimizedClipboard doc = Fawe.instance().submitUUIDKeyQueuedTask(this.uuid, () -> DiskOptimizedClipboard.loadFromFile(file)).get();
            Clipboard clip = doc.toClipboard();
            ClipboardHolder holder = new ClipboardHolder(clip);
            this.setClipboard(holder);
         }
      }
   }

   public void deleteClipboardOnDisk() {
      synchronized (this.clipboardLock) {
         ClipboardHolder holder = this.getExistingClipboard();
         if (holder != null) {
            for (Clipboard clipboard : holder.getClipboards()) {
               if (!(clipboard instanceof DiskOptimizedClipboard doc)) {
                  if (!(clipboard instanceof BlockArrayClipboard) || !(((BlockArrayClipboard)clipboard).getParent() instanceof DiskOptimizedClipboard)) {
                     continue;
                  }

                  doc = (DiskOptimizedClipboard)((BlockArrayClipboard)clipboard).getParent();
               }

               Fawe.instance().submitUUIDKeyQueuedTask(this.uuid, () -> {
                  doc.close();
                  doc.getFile().delete();
               });
            }
         }
      }
   }

   public void setClipboard(@Nullable ClipboardHolder clipboard) {
      synchronized (this.clipboardLock) {
         if (this.clipboard != clipboard) {
            if (this.clipboard != null && (clipboard == null || !clipboard.contains(this.clipboard.getClipboard()))) {
               this.clipboard.close();
            }

            this.clipboard = clipboard;
         }
      }
   }

   @Deprecated
   public boolean isToolControlEnabled() {
      return true;
   }

   @Deprecated
   public void setToolControl(boolean toolControl) {
   }

   public int getBlockChangeLimit() {
      return this.maxBlocksChanged;
   }

   public void setBlockChangeLimit(int maxBlocksChanged) {
      this.maxBlocksChanged = maxBlocksChanged;
   }

   public int getTimeout() {
      return this.maxTimeoutTime;
   }

   public void setTimeout(int timeout) {
      this.maxTimeoutTime = timeout;
   }

   public boolean hasSuperPickAxe() {
      return this.superPickaxe;
   }

   public void enableSuperPickAxe() {
      this.superPickaxe = true;
   }

   public void disableSuperPickAxe() {
      this.superPickaxe = false;
   }

   public boolean toggleSuperPickAxe() {
      this.superPickaxe = !this.superPickaxe;
      return this.superPickaxe;
   }

   public BlockVector3 getPlacementPosition(Actor actor) throws IncompleteRegionException {
      Preconditions.checkNotNull(actor);
      if (!this.placeAtPos1) {
         if (actor instanceof Locatable) {
            return ((Locatable)actor).getBlockLocation().toVector().toBlockPoint();
         } else {
            throw new IncompleteRegionException();
         }
      } else {
         return this.selector.getPrimaryPosition();
      }
   }

   public void setPlaceAtPos1(boolean placeAtPos1) {
      this.placeAtPos1 = placeAtPos1;
   }

   public boolean isPlaceAtPos1() {
      return this.placeAtPos1;
   }

   public boolean togglePlacementPosition() {
      this.placeAtPos1 = !this.placeAtPos1;
      return this.placeAtPos1;
   }

   @Nullable
   public BlockBag getBlockBag(Player player) {
      Preconditions.checkNotNull(player);
      return !this.useInventory && player.getLimit().INVENTORY_MODE == 0 ? null : player.getInventoryBlockBag();
   }

   @Nullable
   public Snapshot getSnapshot() {
      return this.snapshot;
   }

   public void setSnapshot(@Nullable Snapshot snapshot) {
      this.snapshot = snapshot;
   }

   @Nullable
   public com.sk89q.worldedit.world.snapshot.experimental.Snapshot getSnapshotExperimental() {
      return this.snapshotExperimental;
   }

   public void setSnapshotExperimental(@Nullable com.sk89q.worldedit.world.snapshot.experimental.Snapshot snapshotExperimental) {
      this.snapshotExperimental = snapshotExperimental;
   }

   public BlockTool getSuperPickaxe() {
      return this.pickaxeMode;
   }

   public void setSuperPickaxe(BlockTool tool) {
      Preconditions.checkNotNull(tool);
      this.pickaxeMode = tool;
   }

   @Nullable
   @Deprecated
   public Tool getTool(ItemType item, Player player) {
      Tool tool;
      synchronized (this.tools) {
         tool = (Tool)this.tools.get(item.getInternalId());
      }

      if (tool == SelectionWand.INSTANCE && !SelectionWand.INSTANCE.canUse(player)) {
         this.tools.remove(this.wandItem.getType().getInternalId());
         this.loadDefaults(player, true);
         return null;
      } else {
         return tool;
      }
   }

   @Nullable
   @Deprecated
   public Tool getTool(ItemType item) {
      synchronized (this.tools) {
         return (Tool)this.tools.get(item.getInternalId());
      }
   }

   @Nullable
   public Tool getTool(Player player) {
      this.loadDefaults(player, false);
      if (!Settings.settings().EXPERIMENTAL.PERSISTENT_BRUSHES && this.tools.isEmpty()) {
         return null;
      } else {
         BaseItem item = player.getItemInHand(HandSide.MAIN_HAND);
         return this.getTool(item, player);
      }
   }

   public Tool getTool(BaseItem item, Player player) {
      this.loadDefaults(player, false);
      if (Settings.settings().EXPERIMENTAL.PERSISTENT_BRUSHES && item.getNativeItem() != null) {
         BrushTool tool = BrushCache.getTool(player, this, item);
         if (tool != null) {
            return tool;
         }
      }

      return this.getTool(item.getType(), player);
   }

   public void loadDefaults(Actor actor, boolean force) {
      if (this.loadDefaults || force) {
         this.loadDefaults = false;
         LocalConfiguration config = WorldEdit.getInstance().getConfiguration();
         ParserContext context = new ParserContext();
         context.setActor(actor);
         if (this.wandItem == null) {
            this.wandItem = WorldEdit.getInstance().getItemFactory().parseFromInput(config.wandItem, context);
         }

         if (this.navWandItem == null) {
            this.navWandItem = WorldEdit.getInstance().getItemFactory().parseFromInput(config.navigationWand, context);
         }

         synchronized (this.tools) {
            if (this.tools.get(this.navWandItem.getType().getInternalId()) == null && NavigationWand.INSTANCE.canUse(actor)) {
               this.tools.put(this.navWandItem.getType().getInternalId(), NavigationWand.INSTANCE);
            }

            if (this.tools.get(this.wandItem.getType().getInternalId()) == null && SelectionWand.INSTANCE.canUse(actor)) {
               this.tools.put(this.wandItem.getType().getInternalId(), SelectionWand.INSTANCE);
            }
         }
      }
   }

   @Deprecated
   public BrushTool getBrushTool(ItemType item) throws InvalidToolBindException {
      return this.getBrushTool(item.getDefaultState(), null, true);
   }

   public BrushTool getBrushTool(Player player) throws InvalidToolBindException {
      return this.getBrushTool(player, true);
   }

   public BrushTool getBrushTool(Player player, boolean create) throws InvalidToolBindException {
      BaseItem item = player.getItemInHand(HandSide.MAIN_HAND);
      return this.getBrushTool(item, player, create);
   }

   public BrushTool getBrushTool(BaseItem item, Player player, boolean create) throws InvalidToolBindException {
      if (item.getType().hasBlockType()) {
         throw new InvalidToolBindException(item.getType(), Caption.of("worldedit.error.blocks-cant-be-used"));
      } else {
         Tool tool = this.getTool(item, player);
         if (!(tool instanceof BrushTool)) {
            if (!create) {
               return null;
            }

            tool = new BrushTool();
            this.setTool(item, tool, player);
         }

         return (BrushTool)tool;
      }
   }

   @Nullable
   public BrushTool getBrush(ItemType item) {
      return this.getTool(item) instanceof BrushTool tool ? tool : null;
   }

   @Deprecated
   public void setTool(ItemType item, @Nullable Tool tool) throws InvalidToolBindException {
      this.setTool(new BaseItem(item), tool);
   }

   public void setTool(BaseItem item, @Nullable Tool tool) throws InvalidToolBindException {
      if (item.getType().hasBlockType()) {
         throw new InvalidToolBindException(item.getType(), Caption.of("worldedit.error.blocks-cant-be-used"));
      } else if (tool instanceof SelectionWand) {
         this.changeTool(this.wandItem, this.wandItem = item, tool);
         this.setDirty();
      } else if (tool instanceof NavigationWand) {
         this.changeTool(this.navWandItem, this.navWandItem = item, tool);
         this.setDirty();
      } else {
         this.setTool(item, tool, null);
      }
   }

   public void setTool(Player player, @Nullable Tool tool) throws InvalidToolBindException {
      BaseItemStack item = player.getItemInHand(HandSide.MAIN_HAND);
      this.setTool(item, tool, player);
   }

   private void changeTool(BaseItem oldItem, BaseItem newItem, Tool newTool) {
      if (oldItem != null) {
         synchronized (this.tools) {
            this.tools.remove(oldItem.getType().getInternalId());
         }
      }

      synchronized (this.tools) {
         if (newTool == null) {
            this.tools.remove(newItem.getType().getInternalId());
         } else {
            this.tools.put(newItem.getType().getInternalId(), newTool);
         }
      }
   }

   public void setTool(BaseItem item, @Nullable Tool tool, Player player) throws InvalidToolBindException {
      ItemType type = item.getType();
      if (type.hasBlockType() && type.getBlockType().getMaterial().isAir()) {
         throw new InvalidToolBindException(type, Caption.of("worldedit.error.blocks-cant-be-used"));
      } else if (tool instanceof SelectionWand) {
         this.changeTool(this.wandItem, this.wandItem = item, tool);
         this.setDirty();
      } else if (tool instanceof NavigationWand) {
         this.changeTool(this.navWandItem, this.navWandItem = item, tool);
         this.setDirty();
      } else {
         if (player != null
            && (tool instanceof BrushTool || tool == null)
            && Settings.settings().EXPERIMENTAL.PERSISTENT_BRUSHES
            && item.getNativeItem() != null) {
            Tool previous = BrushCache.getCachedTool(item);
            BrushCache.setTool(item, (BrushTool)tool);
            if (tool != null) {
               ((BrushTool)tool).setHolder(item);
            } else {
               synchronized (this.tools) {
                  this.tools.remove(type.getInternalId());
               }
            }
         } else {
            synchronized (this.tools) {
               Tool previous = (Tool)this.tools.get(type.getInternalId());
               if (tool != null) {
                  this.tools.put(type.getInternalId(), tool);
               } else {
                  this.tools.remove(type.getInternalId());
               }
            }
         }
      }
   }

   public boolean isUsingInventory() {
      return this.useInventory;
   }

   public void setUseInventory(boolean useInventory) {
      this.useInventory = useInventory;
   }

   @Nullable
   public String getLastScript() {
      return this.lastScript;
   }

   public void setLastScript(@Nullable String lastScript) {
      this.lastScript = lastScript;
      this.setDirty();
   }

   public void tellVersion(Actor actor) {
      if (!this.hasBeenToldVersion) {
         this.hasBeenToldVersion = true;
         actor.sendAnnouncements();
      }
   }

   public boolean shouldUseServerCUI() {
      return this.useServerCUI;
   }

   public void setUseServerCUI(boolean useServerCUI) {
      this.useServerCUI = useServerCUI;
      this.setDirty();
   }

   public void updateServerCUI(Actor actor) {
      if (actor.isPlayer()) {
         if (this.config.serverSideCUI) {
            Player player = (Player)actor;
            if (this.useServerCUI && !this.hasCUISupport) {
               BaseBlock block = ServerCUIHandler.createStructureBlock(player);
               if (block != null) {
                  LinCompoundTag tags = Objects.requireNonNull(block.getNbt(), "createStructureBlock should return nbt");
                  BlockVector3 tempCuiTemporaryBlock = BlockVector3.at(
                     tags.getTag("x", LinTagType.intTag()).valueAsInt(),
                     tags.getTag("y", LinTagType.intTag()).valueAsInt(),
                     tags.getTag("z", LinTagType.intTag()).valueAsInt()
                  );
                  if (this.cuiTemporaryBlock != null && !tempCuiTemporaryBlock.equals(this.cuiTemporaryBlock)) {
                     player.sendFakeBlock(this.cuiTemporaryBlock, null);
                  }

                  this.cuiTemporaryBlock = tempCuiTemporaryBlock;
                  player.sendFakeBlock(this.cuiTemporaryBlock, block);
                  player.sendFakeOP();
               } else if (this.cuiTemporaryBlock != null) {
                  player.sendFakeBlock(this.cuiTemporaryBlock, null);
                  this.cuiTemporaryBlock = null;
               }
            } else {
               if (this.cuiTemporaryBlock != null) {
                  player.sendFakeBlock(this.cuiTemporaryBlock, null);
                  this.cuiTemporaryBlock = null;
               }
            }
         }
      }
   }

   public void dispatchCUIEvent(Actor actor, CUIEvent event) {
      Preconditions.checkNotNull(actor);
      Preconditions.checkNotNull(event);
      if (this.hasCUISupport) {
         actor.dispatchCUIEvent(event);
      } else if (this.useServerCUI) {
         this.updateServerCUI(actor);
      }
   }

   public void dispatchCUISetup(Actor actor) {
      if (this.selector != null) {
         this.dispatchCUISelection(actor);
      }
   }

   public void dispatchCUISelection(Actor actor) {
      Preconditions.checkNotNull(actor);
      if (!this.hasCUISupport) {
         if (this.useServerCUI) {
            this.updateServerCUI(actor);
         }
      } else {
         if (this.selector instanceof CUIRegion tempSel) {
            if (tempSel.getProtocolVersion() > this.cuiVersion) {
               actor.dispatchCUIEvent(new SelectionShapeEvent(tempSel.getLegacyTypeID()));
               tempSel.describeLegacyCUI(this, actor);
            } else {
               actor.dispatchCUIEvent(new SelectionShapeEvent(tempSel.getTypeID()));
               tempSel.describeCUI(this, actor);
            }
         }
      }
   }

   public void describeCUI(Actor actor) {
      Preconditions.checkNotNull(actor);
      if (this.hasCUISupport) {
         if (this.selector instanceof CUIRegion tempSel) {
            if (tempSel.getProtocolVersion() > this.cuiVersion) {
               tempSel.describeLegacyCUI(this, actor);
            } else {
               tempSel.describeCUI(this, actor);
            }
         }
      }
   }

   public void handleCUIInitializationMessage(String text, Actor actor) {
      Preconditions.checkNotNull(text);
      if (this.hasCUISupport) {
         this.dispatchCUISelection(actor);
      } else if (this.failedCuiAttempts <= 3) {
         String[] split = text.split("\\|", 2);
         if (split.length > 1 && split[0].equalsIgnoreCase("v")) {
            if (split[1].length() > 4) {
               this.failedCuiAttempts++;
               return;
            }

            int version;
            try {
               version = Integer.parseInt(split[1]);
            } catch (NumberFormatException var6) {
               WorldEdit.logger.warn("Error while reading CUI init message: " + var6.getMessage());
               this.failedCuiAttempts++;
               return;
            }

            this.setCUISupport(true);
            this.setCUIVersion(version);
            this.dispatchCUISelection(actor);
         }
      }
   }

   public boolean hasCUISupport() {
      return this.hasCUISupport;
   }

   public void setCUISupport(boolean support) {
      this.hasCUISupport = support;
   }

   public int getCUIVersion() {
      return this.cuiVersion;
   }

   public void setCUIVersion(int cuiVersion) {
      if (cuiVersion < 0) {
         throw new IllegalArgumentException("CUI protocol version must be non-negative, but '" + cuiVersion + "' was received.");
      } else {
         this.cuiVersion = cuiVersion;
      }
   }

   @Nullable
   public Calendar detectDate(String input) {
      Preconditions.checkNotNull(input);
      TimeZone tz = TimeZone.getTimeZone(this.getTimeZone());
      Time.setTimeZone(tz);
      Options opt = new Options();
      opt.setNow(Calendar.getInstance(tz));
      Span date = Chronic.parse(input, opt);
      return date == null ? null : date.getBeginCalendar();
   }

   public EditSession createEditSession(Actor actor) {
      return this.createEditSession(actor, null);
   }

   public EditSession createEditSession(Actor actor, String command) {
      Preconditions.checkNotNull(actor);
      World world = null;
      if (this.hasWorldOverride()) {
         world = this.getWorldOverride();
      } else if (actor instanceof Locatable && ((Locatable)actor).getExtent() instanceof World) {
         world = (World)((Locatable)actor).getExtent();
      }

      EditSessionBuilder builder = WorldEdit.getInstance().newEditSessionBuilder().world(world);
      if (actor.isPlayer() && actor instanceof Player) {
         BlockBag blockBag = this.getBlockBag((Player)actor);
         builder.actor(actor);
         builder.blockBag(blockBag);
      }

      builder.command(command);
      builder.fastMode(this.fastMode);
      if (!this.fastMode) {
         builder.setSideEffectSet(this.sideEffectSet);
      }

      EditSession editSession = builder.build();
      if (this.mask != null) {
         editSession.setMask(this.mask);
      }

      if (this.sourceMask != null) {
         editSession.setSourceMask(this.sourceMask);
      }

      if (this.transform != null) {
         editSession.addTransform(this.transform);
      }

      editSession.setTickingWatchdog(this.tickingWatchdog);
      return editSession;
   }

   private void prepareEditingExtents(EditSession editSession, Actor actor) {
      editSession.setSideEffectApplier(this.sideEffectSet);
      editSession.setReorderMode(this.reorderMode);
      if (editSession.getSurvivalExtent() != null) {
         editSession.getSurvivalExtent().setStripNbt(!actor.hasPermission("worldedit.setnbt"));
      }

      editSession.setTickingWatchdog(this.tickingWatchdog);
   }

   public SideEffectSet getSideEffectSet() {
      return this.sideEffectSet;
   }

   public void setSideEffectSet(SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
   }

   @Deprecated
   public boolean hasFastMode() {
      return this.fastMode;
   }

   @Deprecated
   public void setFastMode(boolean fastMode) {
      this.fastMode = fastMode;
   }

   @Deprecated
   public EditSession.ReorderMode getReorderMode() {
      return EditSession.ReorderMode.FAST;
   }

   @Deprecated
   public void setReorderMode(EditSession.ReorderMode reorderMode) {
   }

   public Mask getMask() {
      return this.mask;
   }

   public Mask getSourceMask() {
      return this.sourceMask;
   }

   public void setMask(Mask mask) {
      this.mask = mask;
   }

   public void setSourceMask(Mask mask) {
      this.sourceMask = mask;
   }

   public synchronized void setTextureUtil(TextureUtil texture) {
      this.texture = texture;
   }

   @Override
   public TextureUtil getTextureUtil() {
      TextureUtil tmp = this.texture;
      if (tmp == null) {
         synchronized (this) {
            tmp = Fawe.instance().getCachedTextureUtil(true, 0, 100);
            this.texture = tmp;
         }
      }

      return tmp;
   }

   @Deprecated
   public String getWandItem() {
      return this.wandItem.getType().id();
   }

   @Deprecated
   public String getNavWandItem() {
      return this.navWandItem.getType().id();
   }

   public BaseItem getWandBaseItem() {
      return this.wandItem == null ? null : new BaseItem(this.wandItem.getType(), this.wandItem.getNbtReference());
   }

   public BaseItem getNavWandBaseItem() {
      return this.navWandItem == null ? null : new BaseItem(this.navWandItem.getType(), this.navWandItem.getNbtReference());
   }

   public List<Countable<BlockState>> getLastDistribution() {
      return this.lastDistribution == null ? null : Collections.unmodifiableList(this.lastDistribution);
   }

   public void setLastDistribution(List<Countable<BlockState>> dist) {
      this.lastDistribution = dist;
   }

   public ResettableExtent getTransform() {
      return this.transform;
   }

   public void setTransform(ResettableExtent transform) {
      this.transform = transform;
   }

   public void onIdle() {
      this.cuiVersion = -1;
      this.hasCUISupport = false;
      this.failedCuiAttempts = 0;
   }
}
