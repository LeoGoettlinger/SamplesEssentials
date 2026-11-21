package com.fastasyncworldedit.core.regions.selector;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extent.PassthroughExtent;
import com.fastasyncworldedit.core.regions.FuzzyRegion;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

public class FuzzyRegionSelector extends PassthroughExtent implements RegionSelector {
   private final Actor actor;
   private FuzzyRegion region;
   private final ArrayList<BlockVector3> positions;

   public FuzzyRegionSelector(Actor actor, @Nullable World world, Mask mask) {
      super(WorldEdit.getInstance().newEditSessionBuilder().world(world).actor(actor).changeSetNull().checkMemory(false).build());
      this.actor = actor;
      this.region = new FuzzyRegion(world, this.getExtent(), mask);
      this.positions = new ArrayList<>();
      new MaskTraverser(mask).reset(this.getExtent());
   }

   @Nullable
   @Override
   public World getWorld() {
      return this.region.getWorld();
   }

   @Override
   public void setWorld(@Nullable World world) {
      EditSession extent = WorldEdit.getInstance().newEditSessionBuilder().world(world).actor(this.actor).changeSetNull().checkMemory(false).build();
      new ExtentTraverser<>(this).setNext(extent);
      this.region.setWorld(world);
      this.region.setExtent(extent);
      new MaskTraverser(this.getMask()).reset(extent);
   }

   public Mask getMask() {
      return this.region.getMask();
   }

   @Override
   public boolean selectPrimary(BlockVector3 position, SelectorLimits limits) {
      this.setWorld(this.getWorld());
      new MaskTraverser(this.getMask()).reset(this.getExtent());
      this.positions.clear();
      this.positions.add(position);
      this.region = new FuzzyRegion(this.getWorld(), this.getExtent(), this.getMask());
      this.region.select(position);
      return true;
   }

   @Override
   public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
      this.positions.add(position);
      new MaskTraverser(this.getMask()).reset(this.getExtent());
      this.region.select(position);
      return true;
   }

   @Override
   public void explainPrimarySelection(Actor actor, LocalSession session, BlockVector3 position) {
      actor.print(Caption.of("fawe.worldedit.selector.selector.fuzzy.pos1", position, "(" + this.region.getVolume() + ")"));
   }

   @Override
   public void explainSecondarySelection(Actor actor, LocalSession session, BlockVector3 position) {
      actor.print(Caption.of("fawe.worldedit.selector.selector.fuzzy.pos2", position, "(" + this.region.getVolume() + ")"));
   }

   @Override
   public void explainRegionAdjust(Actor actor, LocalSession session) {
   }

   @Override
   public BlockVector3 getPrimaryPosition() throws IncompleteRegionException {
      if (this.positions.isEmpty()) {
         throw new IncompleteRegionException();
      } else {
         return this.positions.get(0);
      }
   }

   @Override
   public Region getRegion() throws IncompleteRegionException {
      return this.region;
   }

   @Override
   public Region getIncompleteRegion() {
      return this.region;
   }

   @Override
   public boolean isDefined() {
      return true;
   }

   @Override
   public int getArea() {
      return this.region.getArea();
   }

   @Override
   public void learnChanges() {
   }

   @Override
   public void clear() {
      this.positions.clear();
      this.region = new FuzzyRegion(this.getWorld(), this.getExtent(), this.getMask());
   }

   @Override
   public String getTypeName() {
      return "fuzzy";
   }

   @Override
   public List<String> getInformationLines() {
      return IntStream.range(0, this.positions.size()).mapToObj(i -> "Position " + i + ": " + this.positions.get(i)).collect(Collectors.toList());
   }

   @Override
   public List<BlockVector3> getVertices() {
      return this.positions;
   }
}
