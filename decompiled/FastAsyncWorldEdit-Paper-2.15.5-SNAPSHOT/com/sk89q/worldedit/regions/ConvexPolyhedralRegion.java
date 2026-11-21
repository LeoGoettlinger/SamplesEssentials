package com.sk89q.worldedit.regions;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.polyhedron.Edge;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class ConvexPolyhedralRegion extends AbstractRegion {
   private final Set<BlockVector3> vertices = new LinkedHashSet<>();
   private final List<Triangle> triangles = new ArrayList<>();
   private final Set<BlockVector3> vertexBacklog = new LinkedHashSet<>();
   private BlockVector3 minimumPoint;
   private BlockVector3 maximumPoint;
   private BlockVector3 centerAccum = BlockVector3.ZERO;
   private Triangle lastTriangle;

   public ConvexPolyhedralRegion(@Nullable World world) {
      super(world);
   }

   public ConvexPolyhedralRegion(ConvexPolyhedralRegion region) {
      this(region.world);
      this.vertices.addAll(region.vertices);
      region.triangles.forEach(triangle -> this.triangles.add(triangle.clone()));
      this.vertexBacklog.addAll(region.vertexBacklog);
      this.minimumPoint = region.minimumPoint;
      this.maximumPoint = region.maximumPoint;
      this.centerAccum = region.centerAccum;
      this.lastTriangle = this.lastTriangle == null ? null : region.lastTriangle.clone();
   }

   @Override
   public void clear() {
      this.vertices.clear();
      this.triangles.clear();
      this.vertexBacklog.clear();
      this.minimumPoint = null;
      this.maximumPoint = null;
      this.centerAccum = BlockVector3.ZERO;
      this.lastTriangle = null;
   }

   public boolean addVertex(BlockVector3 vertex) {
      Preconditions.checkNotNull(vertex);
      this.lastTriangle = null;
      if (this.vertices.contains(vertex)) {
         return false;
      } else {
         Vector3 vertexD = vertex.toVector3();
         if (this.vertices.size() == 3) {
            if (this.vertexBacklog.contains(vertex)) {
               return false;
            }

            if (this.containsRaw(vertexD)) {
               return this.vertexBacklog.add(vertex);
            }
         }

         this.vertices.add(vertex);
         this.centerAccum = this.centerAccum.add(vertex);
         if (this.minimumPoint == null) {
            this.minimumPoint = this.maximumPoint = vertex;
         } else {
            this.minimumPoint = this.minimumPoint.getMinimum(vertex);
            this.maximumPoint = this.maximumPoint.getMaximum(vertex);
         }

         switch (this.vertices.size()) {
            case 0:
            case 1:
            case 2:
               return true;
            case 3:
               BlockVector3[] v = this.vertices.toArray(new BlockVector3[0]);
               this.triangles.add(new Triangle(v[0].toVector3(), v[1].toVector3(), v[2].toVector3()));
               this.triangles.add(new Triangle(v[0].toVector3(), v[2].toVector3(), v[1].toVector3()));
               return true;
            default:
               Set<Edge> borderEdges = new LinkedHashSet<>();
               Iterator<Triangle> it = this.triangles.iterator();

               while (it.hasNext()) {
                  Triangle triangle = it.next();
                  if (triangle.above(vertexD)) {
                     it.remove();

                     for (int i = 0; i < 3; i++) {
                        Edge edge = triangle.getEdge(i);
                        if (!borderEdges.remove(edge)) {
                           borderEdges.add(edge);
                        }
                     }
                  }
               }

               for (Edge edge : borderEdges) {
                  this.triangles.add(edge.createTriangle(vertexD));
               }

               if (!this.vertexBacklog.isEmpty()) {
                  this.vertices.remove(vertex);
                  List<BlockVector3> vertexBacklog2 = new ArrayList<>(this.vertexBacklog);
                  this.vertexBacklog.clear();

                  for (BlockVector3 vertex2 : vertexBacklog2) {
                     this.addVertex(vertex2);
                  }

                  this.vertices.add(vertex);
               }

               return true;
         }
      }
   }

   public boolean isDefined() {
      return !this.triangles.isEmpty();
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.minimumPoint;
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.maximumPoint;
   }

   @Override
   public Vector3 getCenter() {
      return this.centerAccum.toVector3().divide(this.vertices.size());
   }

   @Override
   public void expand(BlockVector3... changes) throws RegionOperationException {
   }

   @Override
   public void contract(BlockVector3... changes) throws RegionOperationException {
   }

   @Override
   public void shift(BlockVector3 change) throws RegionOperationException {
      Vector3 vec = change.toVector3();
      shiftCollection(this.vertices, change);
      shiftCollection(this.vertexBacklog, change);

      for (int i = 0; i < this.triangles.size(); i++) {
         Triangle triangle = this.triangles.get(i);
         Vector3 v0 = vec.add(triangle.getVertex(0));
         Vector3 v1 = vec.add(triangle.getVertex(1));
         Vector3 v2 = vec.add(triangle.getVertex(2));
         this.triangles.set(i, new Triangle(v0, v1, v2));
      }

      this.minimumPoint = change.add(this.minimumPoint);
      this.maximumPoint = change.add(this.maximumPoint);
      this.centerAccum = change.multiply(this.vertices.size()).add(this.centerAccum);
      this.lastTriangle = null;
   }

   private static void shiftCollection(Collection<BlockVector3> collection, BlockVector3 change) {
      List<BlockVector3> tmp = new ArrayList<>(collection);
      collection.clear();

      for (BlockVector3 vertex : tmp) {
         collection.add(change.add(vertex));
      }
   }

   @Override
   public boolean contains(BlockVector3 position) {
      if (!this.isDefined()) {
         return false;
      } else {
         BlockVector3 min = this.getMinimumPoint();
         BlockVector3 max = this.getMaximumPoint();
         return !position.containedWithin(min, max) ? false : this.containsRaw(position.toVector3());
      }
   }

   private boolean containsRaw(Vector3 pt) {
      if (this.lastTriangle != null && this.lastTriangle.above(pt)) {
         return false;
      } else {
         for (Triangle triangle : this.triangles) {
            if (this.lastTriangle != triangle && triangle.above(pt)) {
               this.lastTriangle = triangle;
               return false;
            }
         }

         return true;
      }
   }

   public Collection<BlockVector3> getVertices() {
      if (this.vertexBacklog.isEmpty()) {
         return this.vertices;
      } else {
         List<BlockVector3> ret = new ArrayList<>(this.vertices);
         ret.addAll(this.vertexBacklog);
         return ret;
      }
   }

   public Collection<Triangle> getTriangles() {
      return this.triangles;
   }

   @Override
   public AbstractRegion clone() {
      return new ConvexPolyhedralRegion(this);
   }

   @Override
   public boolean containsEntireCuboid(int bx, int tx, int by, int ty, int bz, int tz) {
      return false;
   }
}
