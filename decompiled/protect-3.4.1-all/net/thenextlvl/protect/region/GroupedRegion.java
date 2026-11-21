package net.thenextlvl.protect.region;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.MultiFuture;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public final class GroupedRegion extends AbstractRegion {
   private final Map<String, Region> regions = new HashMap<>();

   public GroupedRegion(Map<String, Region> regions) {
      this(null, regions);
   }

   public GroupedRegion(@Nullable World world, Map<String, Region> regions) {
      super(world);
      Preconditions.checkArgument(!regions.isEmpty(), "empty region map is not supported");
      this.regions.putAll(regions);
   }

   @Contract(
      pure = true
   )
   @Unmodifiable
   public Map<String, Region> getRegions() {
      return Map.copyOf(this.regions);
   }

   @Contract(
      pure = true
   )
   @Nullable
   public Region getRegion(String name) {
      return this.regions.get(name);
   }

   @Contract(
      mutates = "this"
   )
   public boolean addRegion(String name, Region region) {
      return this.regions.putIfAbsent(name, region.clone()) == null;
   }

   @Contract(
      pure = true
   )
   public boolean hasRegion(String name) {
      return this.regions.containsKey(name);
   }

   @Contract(
      pure = true
   )
   public boolean hasRegion(Region region) {
      return this.regions.containsValue(region);
   }

   @Contract(
      mutates = "this"
   )
   public boolean removeRegion(String name) {
      return this.regions.size() > 1 && this.regions.remove(name) != null;
   }

   @Contract(
      mutates = "this"
   )
   public void setRegion(String name, Region region) {
      this.regions.put(name, region.clone());
   }

   @Contract(
      pure = true
   )
   public BlockVector3 getMinimumPoint() {
      Region[] regions = this.regions.values().toArray(new Region[0]);
      BlockVector3 maximum = regions[0].getMinimumPoint();

      for (int i = 1; i < regions.length; i++) {
         maximum = regions[i].getMinimumPoint().getMinimum(maximum);
      }

      return maximum;
   }

   @Contract(
      pure = true
   )
   public BlockVector3 getMaximumPoint() {
      Region[] regions = this.regions.values().toArray(new Region[0]);
      BlockVector3 maximum = regions[0].getMaximumPoint();

      for (int i = 1; i < regions.length; i++) {
         maximum = regions[i].getMaximumPoint().getMaximum(maximum);
      }

      return maximum;
   }

   @Contract("_ -> fail")
   public void expand(BlockVector3... changes) throws RegionOperationException {
      throw new RegionOperationException(Caption.of("worldedit.selection.intersection.error.cannot-expand", new Object[0]));
   }

   @Contract("_ -> fail")
   public void contract(BlockVector3... changes) throws RegionOperationException {
      throw new RegionOperationException(Caption.of("worldedit.selection.intersection.error.cannot-contract", new Object[0]));
   }

   @Contract(
      pure = true
   )
   public boolean contains(BlockVector3 position) {
      return this.regions.values().stream().anyMatch(region -> region.contains(position));
   }

   @Contract(
      pure = true
   )
   public Iterator<BlockVector3> iterator() {
      return Iterators.concat(Iterators.transform(this.regions.values().iterator(), Iterable::iterator));
   }

   @Contract(
      pure = true
   )
   public boolean containsEntireCuboid(int bx, int tx, int by, int ty, int bz, int tz) {
      return this.regions.values().stream().anyMatch(region -> region.containsEntireCuboid(bx, tx, by, ty, bz, tz));
   }

   @Nullable
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      int bx = chunk.getX() << 4;
      int bz = chunk.getZ() << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      List<Region> intersecting = new ArrayList<>(2);

      for (Region region : this.regions.values()) {
         BlockVector3 regMin = region.getMinimumPoint();
         BlockVector3 regMax = region.getMaximumPoint();
         if (tx >= regMin.x() && bx <= regMax.x() && tz >= regMin.z() && bz <= regMax.z()) {
            intersecting.add(region);
         }
      }

      if (intersecting.isEmpty()) {
         return null;
      } else {
         return intersecting.size() == 1 ? intersecting.getFirst().processSet(chunk, get, set) : super.processSet(chunk, get, set);
      }
   }

   public Future<?> postProcessSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return new MultiFuture(this.regions.values().stream().map(region -> region.postProcessSet(chunk, get, set)).toList());
   }

   @Nullable
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set, boolean asBlacklist) {
      if (!asBlacklist) {
         return this.processSet(chunk, get, set);
      } else {
         int bx = chunk.getX() << 4;
         int bz = chunk.getZ() << 4;
         int tx = bx + 15;
         int tz = bz + 15;

         for (Region region : this.regions.values()) {
            BlockVector3 regMin = region.getMinimumPoint();
            BlockVector3 regMax = region.getMaximumPoint();
            if (tx >= regMin.x() && bx <= regMax.x() && tz >= regMin.z() && bz <= regMax.z()) {
               set = region.processSet(chunk, get, set, true);
            }
         }

         return set;
      }
   }

   @Contract(
      pure = true
   )
   public Set<BlockVector2> getChunks() {
      return this.regions.values().stream().flatMap(region -> region.getChunks().stream()).collect(Collectors.toSet());
   }

   @Contract(
      pure = true
   )
   public Set<BlockVector3> getChunkCubes() {
      return this.regions.values().stream().flatMap(region -> region.getChunkCubes().stream()).collect(Collectors.toSet());
   }

   @Contract(
      pure = true
   )
   public boolean containsChunk(int chunkX, int chunkZ) {
      return this.regions.values().stream().anyMatch(region -> region.containsChunk(chunkX, chunkZ));
   }

   @Contract(
      pure = true
   )
   public boolean contains(int x, int z) {
      return this.regions.values().stream().anyMatch(region -> region.contains(x, z));
   }

   @Contract(
      pure = true
   )
   public boolean contains(int x, int y, int z) {
      return this.regions.values().stream().anyMatch(region -> region.contains(x, y, z));
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   public GroupedRegion clone() {
      GroupedRegion clone = (GroupedRegion)super.clone();
      clone.regions.putAll(this.regions);
      return clone;
   }
}
