package com.sk89q.worldedit.regions.selector;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class ConvexPolyhedralRegionSelector implements RegionSelector, CUIRegion {
   private final transient ConvexPolyhedralRegion region;
   private transient BlockVector3 pos1;

   public ConvexPolyhedralRegionSelector() {
      this((World)null);
   }

   public ConvexPolyhedralRegionSelector(@Nullable World world) {
      this.region = new ConvexPolyhedralRegion(world);
   }

   public ConvexPolyhedralRegionSelector(ConvexPolyhedralRegion region) {
      Preconditions.checkNotNull(region);
      this.region = region;
   }

   public ConvexPolyhedralRegionSelector(RegionSelector oldSelector) {
      Preconditions.checkNotNull(oldSelector);
      if (oldSelector instanceof ConvexPolyhedralRegionSelector convexPolyhedralRegionSelector) {
         this.pos1 = convexPolyhedralRegionSelector.pos1;
         this.region = new ConvexPolyhedralRegion(convexPolyhedralRegionSelector.region);
      } else {
         Region oldRegion;
         try {
            oldRegion = oldSelector.getRegion();
         } catch (IncompleteRegionException var7) {
            this.region = new ConvexPolyhedralRegion(oldSelector.getIncompleteRegion().getWorld());
            return;
         }

         int minY = oldRegion.getMinimumPoint().y();
         int maxY = oldRegion.getMaximumPoint().y();
         this.region = new ConvexPolyhedralRegion(oldRegion.getWorld());

         for (BlockVector2 pt : new ArrayList<>(oldRegion.polygonize(Integer.MAX_VALUE))) {
            this.region.addVertex(pt.toBlockVector3(minY));
            this.region.addVertex(pt.toBlockVector3(maxY));
         }

         this.learnChanges();
      }
   }

   @Nullable
   @Override
   public World getWorld() {
      return this.region.getWorld();
   }

   @Override
   public void setWorld(@Nullable World world) {
      this.region.setWorld(world);
   }

   @Override
   public boolean selectPrimary(BlockVector3 position, SelectorLimits limits) {
      Preconditions.checkNotNull(position);
      this.clear();
      this.pos1 = position;
      return this.region.addVertex(position);
   }

   @Override
   public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
      Preconditions.checkNotNull(position);
      Optional<Integer> vertexLimit = limits.getPolyhedronVertexLimit();
      return vertexLimit.isPresent() && this.region.getVertices().size() > vertexLimit.get() ? false : this.region.addVertex(position);
   }

   @Override
   public BlockVector3 getPrimaryPosition() throws IncompleteRegionException {
      return this.pos1;
   }

   @Override
   public Region getRegion() throws IncompleteRegionException {
      if (!this.region.isDefined()) {
         throw new IncompleteRegionException();
      } else {
         return this.region;
      }
   }

   @Override
   public Region getIncompleteRegion() {
      return this.region;
   }

   @Override
   public boolean isDefined() {
      return this.region.isDefined();
   }

   @Override
   public long getVolume() {
      return this.region.getVolume();
   }

   @Override
   public void learnChanges() {
      this.pos1 = this.region.getVertices().iterator().next();
   }

   @Override
   public void clear() {
      this.region.clear();
   }

   @Override
   public String getTypeName() {
      return "Convex Polyhedron";
   }

   @Override
   public List<Component> getSelectionInfoLines() {
      List<Component> ret = new ArrayList<>();
      ret.add(Caption.of("worldedit.selection.convex.info.vertices", TextComponent.of(this.region.getVertices().size())));
      ret.add(Caption.of("worldedit.selection.convex.info.triangles", TextComponent.of(this.region.getTriangles().size())));
      return ret;
   }

   @Override
   public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      Preconditions.checkNotNull(pos);
      session.dispatchCUIEvent(player, new SelectionShapeEvent(this.getTypeID()));
      session.describeCUI(player);
      player.print(Caption.of("worldedit.selection.convex.explain.primary", TextComponent.of(pos.toString())));
   }

   @Override
   public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      Preconditions.checkNotNull(pos);
      session.describeCUI(player);
      player.print(Caption.of("worldedit.selection.convex.explain.secondary", TextComponent.of(pos.toString())));
   }

   @Override
   public void explainRegionAdjust(Actor player, LocalSession session) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      session.dispatchCUIEvent(player, new SelectionShapeEvent(this.getTypeID()));
      session.describeCUI(player);
   }

   @Override
   public int getProtocolVersion() {
      return 3;
   }

   @Override
   public String getTypeID() {
      return "polyhedron";
   }

   @Override
   public void describeCUI(LocalSession session, Actor player) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      Collection<BlockVector3> vertices = this.region.getVertices();
      Collection<Triangle> triangles = this.region.getTriangles();
      Map<BlockVector3, Integer> vertexIds = new HashMap<>(vertices.size());
      int lastVertexId = -1;

      for (BlockVector3 vertex : vertices) {
         vertexIds.put(vertex, ++lastVertexId);
         session.dispatchCUIEvent(player, new SelectionPointEvent(lastVertexId, vertex, this.getVolume()));
      }

      for (Triangle triangle : triangles) {
         int[] v = new int[3];

         for (int i = 0; i < 3; i++) {
            v[i] = vertexIds.get(triangle.getVertex(i).toBlockPoint());
         }

         session.dispatchCUIEvent(player, new SelectionPolygonEvent(v));
      }
   }

   @Override
   public String getLegacyTypeID() {
      return "cuboid";
   }

   @Override
   public void describeLegacyCUI(LocalSession session, Actor player) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      if (this.isDefined()) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.region.getMinimumPoint(), this.getVolume()));
         session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.region.getMaximumPoint(), this.getVolume()));
      }
   }

   @Override
   public List<BlockVector3> getVertices() {
      return new ArrayList<>(this.region.getVertices());
   }
}
