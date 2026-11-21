package com.fastasyncworldedit.core.history.changeset;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.change.EntityCreate;
import com.sk89q.worldedit.history.change.EntityRemove;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Future;

public class AbstractDelegateChangeSet extends AbstractChangeSet {
   public final AbstractChangeSet parent;

   public AbstractDelegateChangeSet(AbstractChangeSet parent) {
      super(parent.getWorld());
      this.parent = parent;
   }

   public final AbstractChangeSet getParent() {
      return this.parent;
   }

   @Override
   public World getWorld() {
      return this.parent.getWorld();
   }

   @Override
   public void closeAsync() {
      this.parent.closeAsync();
   }

   @Override
   public void flush() {
      this.parent.flush();
   }

   @Override
   public void addBiomeChange(int x, int y, int z, BiomeType from, BiomeType to) {
      this.parent.addBiomeChange(x, y, z, from, to);
   }

   @Override
   public Iterator<Change> getIterator(BlockBag blockBag, int mode, boolean redo) {
      return this.parent.getIterator(blockBag, mode, redo);
   }

   @Override
   public ChangeExchangeCoordinator getCoordinatedChanges(final BlockBag blockBag, final int mode, final boolean dir) {
      return this.parent.getCoordinatedChanges(blockBag, mode, dir);
   }

   @Override
   public Iterator<Change> getIterator(boolean redo) {
      return this.parent.getIterator(redo);
   }

   @Override
   public EditSession toEditSession(Actor actor) {
      return this.parent.toEditSession(actor);
   }

   @Override
   public EditSession toEditSession(Actor actor, Region[] regions) {
      return this.parent.toEditSession(actor, regions);
   }

   @Override
   public void add(int x, int y, int z, int combinedFrom, int combinedTo) {
      this.parent.add(x, y, z, combinedFrom, combinedTo);
   }

   @Override
   public void add(EntityCreate change) {
      this.parent.add(change);
   }

   @Override
   public void add(EntityRemove change) {
      this.parent.add(change);
   }

   @Override
   public void add(Change change) {
      this.parent.add(change);
   }

   @Override
   public void add(BlockChange change) {
      this.parent.add(change);
   }

   @Override
   public void add(BlockVector3 loc, BaseBlock from, BaseBlock to) {
      this.parent.add(loc, from, to);
   }

   @Override
   public void add(int x, int y, int z, BaseBlock from, BaseBlock to) {
      this.parent.add(x, y, z, from, to);
   }

   @Override
   public void add(int x, int y, int z, int combinedFrom, BaseBlock to) {
      this.parent.add(x, y, z, combinedFrom, to);
   }

   @Override
   public Iterator<Change> backwardIterator() {
      return this.parent.backwardIterator();
   }

   @Override
   public Iterator<Change> forwardIterator() {
      return this.parent.forwardIterator();
   }

   @Override
   public void addTileCreate(final FaweCompoundTag tag) {
      this.parent.addTileCreate(tag);
   }

   @Override
   public void addTileRemove(final FaweCompoundTag tag) {
      this.parent.addTileRemove(tag);
   }

   @Override
   public void addEntityRemove(final FaweCompoundTag tag) {
      this.parent.addEntityRemove(tag);
   }

   @Override
   public void addEntityCreate(final FaweCompoundTag tag) {
      this.parent.addEntityCreate(tag);
   }

   @Override
   public void close() throws IOException {
      this.parent.close();
   }

   @Override
   public boolean isEmpty() {
      return this.parent.isEmpty();
   }

   @Override
   public Future<?> addWriteTask(Runnable writeTask) {
      return this.parent.addWriteTask(writeTask);
   }

   @Override
   public Future<?> addWriteTask(Runnable writeTask, boolean completeNow) {
      return this.parent.addWriteTask(writeTask, completeNow);
   }

   @Override
   public boolean isRecordingChanges() {
      return this.parent.isRecordingChanges();
   }

   @Override
   public void setRecordChanges(boolean recordChanges) {
      this.parent.setRecordChanges(recordChanges);
   }

   @Override
   public int size() {
      return this.parent.size();
   }

   @Override
   public long longSize() {
      return this.parent.longSize();
   }

   @Override
   public void delete() {
      this.parent.delete();
   }

   @Override
   public ChangeSetSummary summarize(Region region, boolean shallow) {
      return this.parent.summarize(region, shallow);
   }
}
