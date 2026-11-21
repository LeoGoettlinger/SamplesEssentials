package com.fastasyncworldedit.core.regions.selector;

import com.fastasyncworldedit.core.regions.PolyhedralRegion;
import com.fastasyncworldedit.core.regions.Triangle;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class PolyhedralRegionSelector implements RegionSelector, CUIRegion {
   private final transient PolyhedralRegion region;
   private transient BlockVector3 pos1;

   public PolyhedralRegionSelector() {
      this(null);
   }

   public PolyhedralRegionSelector(@Nullable World world) {
      this.region = new PolyhedralRegion(world);
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
   public int getArea() {
      return this.region.getArea();
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
      return "Polyhedron";
   }

   @Override
   public List<String> getInformationLines() {
      List<String> ret = new ArrayList<>();
      ret.add("Vertices: " + this.region.getVertices().size());
      ret.add("Triangles: " + this.region.getTriangles().size());
      return ret;
   }

   @Override
   public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      Preconditions.checkNotNull(pos);
      session.describeCUI(player);
      player.print(TextComponent.of("Started new selection with vertex " + pos + "."));
   }

   @Override
   public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
      Preconditions.checkNotNull(pos);
      session.describeCUI(player);
      player.print(TextComponent.of("Added vertex " + pos + " to the selection."));
   }

   @Override
   public void explainRegionAdjust(Actor player, LocalSession session) {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(session);
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
         session.dispatchCUIEvent(player, new SelectionPointEvent(lastVertexId, vertex, this.getArea()));
      }

      for (Triangle triangle : triangles) {
         int[] v = new int[3];

         for (int i = 0; i < 3; i++) {
            v[i] = vertexIds.get(triangle.getVertex(i));
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
         session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.region.getMinimumPoint(), this.getArea()));
         session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.region.getMaximumPoint(), this.getArea()));
      }
   }

   @Override
   public List<BlockVector3> getVertices() {
      return new ArrayList<>(this.region.getVertices());
   }
}
