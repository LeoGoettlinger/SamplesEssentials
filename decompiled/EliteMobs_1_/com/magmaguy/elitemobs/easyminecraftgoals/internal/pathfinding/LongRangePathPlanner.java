package com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.util.Workload;
import com.magmaguy.elitemobs.magmacore.util.WorkloadRunnable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Flying;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WaterMob;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class LongRangePathPlanner {
   static final double NATIVE_LEG_DISTANCE = 8.0;
   static final double PLANNING_HORIZON = 96.0;
   private static final double CORRIDOR_RADIUS = 48.0;
   private static final int MAX_CAPTURED_CHUNKS = 160;
   private static final int MAX_VISITED_NODES = 180000;
   private static volatile long generation;

   private LongRangePathPlanner() {
   }

   static void shutdown() {
      generation++;
      LongRangePathPlanner.AsyncSearchQueue.shutdown();
      LongRangePathPlanner.SnapshotWorkQueue.shutdown();
   }

   static LongRangePathPlanner.Request plan(LivingEntity entity, Location finalTarget, Consumer<LongRangePathPlanner.PlanResult> callback) {
      Objects.requireNonNull(entity, "entity");
      Objects.requireNonNull(finalTarget, "finalTarget");
      Objects.requireNonNull(callback, "callback");
      Location start = entity.getLocation().clone();
      if (start.getWorld() != null && start.getWorld() == finalTarget.getWorld()) {
         boolean reachesFinal = start.distance(finalTarget) <= 96.0;
         Location planningTarget = reachesFinal ? finalTarget.clone() : horizonTarget(start, finalTarget);
         SnapshotPathSolver.BodyProfile body = bodyProfile(entity);
         LongRangePathPlanner.Request request = new LongRangePathPlanner.Request(start, planningTarget, finalTarget.clone(), reachesFinal, body, callback);
         request.start();
         return request;
      } else {
         callback.accept(LongRangePathPlanner.PlanResult.noPath(false));
         return LongRangePathPlanner.Request.cancelled();
      }
   }

   private static Location horizonTarget(Location start, Location finalTarget) {
      Vector delta = finalTarget.toVector().subtract(start.toVector());
      return delta.lengthSquared() <= 1.0E-8 ? finalTarget.clone() : start.clone().add(delta.normalize().multiply(96.0));
   }

   private static SnapshotPathSolver.BodyProfile bodyProfile(LivingEntity entity) {
      SnapshotPathSolver.MovementMode mode = entity instanceof WaterMob
         ? SnapshotPathSolver.MovementMode.AQUATIC
         : (entity instanceof Flying ? SnapshotPathSolver.MovementMode.FLYING : SnapshotPathSolver.MovementMode.GROUND);
      BoundingBox bounds = entity.getBoundingBox();
      double width = Math.max(bounds.getWidthX(), bounds.getWidthZ());
      int horizontalRadius = Math.max(0, (int)Math.ceil((width - 1.0) / 2.0));
      int heightBlocks = Math.max(1, (int)Math.ceil(bounds.getHeight()));
      return new SnapshotPathSolver.BodyProfile(mode, horizontalRadius, heightBlocks);
   }

   private static List<LongRangePathPlanner.ChunkCoordinate> corridorChunks(Location start, Location target) {
      int minimumChunkX = (int)Math.floor(Math.min(start.getX(), target.getX()) - 48.0) >> 4;
      int maximumChunkX = (int)Math.floor(Math.max(start.getX(), target.getX()) + 48.0) >> 4;
      int minimumChunkZ = (int)Math.floor(Math.min(start.getZ(), target.getZ()) - 48.0) >> 4;
      int maximumChunkZ = (int)Math.floor(Math.max(start.getZ(), target.getZ()) + 48.0) >> 4;
      double acceptedRadius = 60.0;
      double acceptedRadiusSquared = acceptedRadius * acceptedRadius;
      List<LongRangePathPlanner.ChunkCoordinate> chunks = new ArrayList<>();

      for (int chunkX = minimumChunkX; chunkX <= maximumChunkX; chunkX++) {
         for (int chunkZ = minimumChunkZ; chunkZ <= maximumChunkZ; chunkZ++) {
            double centerX = chunkX * 16.0 + 8.0;
            double centerZ = chunkZ * 16.0 + 8.0;
            if (distanceSquaredToSegment(centerX, centerZ, start.getX(), start.getZ(), target.getX(), target.getZ()) <= acceptedRadiusSquared) {
               chunks.add(new LongRangePathPlanner.ChunkCoordinate(chunkX, chunkZ));
            }
         }
      }

      chunks.sort(Comparator.comparingDouble(chunk -> chunk.distanceSquared(start.getX(), start.getZ())));
      return chunks.size() <= 160 ? List.copyOf(chunks) : List.copyOf(chunks.subList(0, 160));
   }

   private static double distanceSquaredToSegment(double pointX, double pointZ, double startX, double startZ, double endX, double endZ) {
      double dx = endX - startX;
      double dz = endZ - startZ;
      double lengthSquared = dx * dx + dz * dz;
      if (lengthSquared <= 1.0E-8) {
         double offsetX = pointX - startX;
         double offsetZ = pointZ - startZ;
         return offsetX * offsetX + offsetZ * offsetZ;
      } else {
         double fraction = ((pointX - startX) * dx + (pointZ - startZ) * dz) / lengthSquared;
         fraction = Math.max(0.0, Math.min(1.0, fraction));
         double closestX = startX + fraction * dx;
         double closestZ = startZ + fraction * dz;
         double offsetX = pointX - closestX;
         double offsetZ = pointZ - closestZ;
         return offsetX * offsetX + offsetZ * offsetZ;
      }
   }

   private static List<SnapshotPathSolver.Point> navigationWaypoints(List<SnapshotPathSolver.Point> path) {
      if (path.size() <= 1) {
         return List.of();
      } else {
         List<SnapshotPathSolver.Point> waypoints = new ArrayList<>();
         SnapshotPathSolver.Point previous = path.getFirst();
         double accumulated = 0.0;
         int previousDx = 0;
         int previousDz = 0;

         for (int index = 1; index < path.size(); index++) {
            SnapshotPathSolver.Point current = path.get(index);
            int dx = Integer.compare(current.x(), previous.x());
            int dz = Integer.compare(current.z(), previous.z());
            accumulated += distance(previous, current);
            boolean sharpTurn = index > 1 && (dx != previousDx || dz != previousDz) && dx * previousDx + dz * previousDz <= 0;
            if (accumulated >= 8.0 || sharpTurn || index == path.size() - 1) {
               if (waypoints.isEmpty() || !waypoints.getLast().equals(current)) {
                  waypoints.add(current);
               }

               accumulated = 0.0;
            }

            previousDx = dx;
            previousDz = dz;
            previous = current;
         }

         return List.copyOf(waypoints);
      }
   }

   private static double distance(SnapshotPathSolver.Point first, SnapshotPathSolver.Point second) {
      int dx = second.x() - first.x();
      int dy = second.y() - first.y();
      int dz = second.z() - first.z();
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private static Location location(World world, SnapshotPathSolver.Point point) {
      return new Location(world, point.x() + 0.5, point.y(), point.z() + 0.5);
   }

   private static final class AsyncSearchQueue {
      private static final int MAX_CONCURRENT_SEARCHES = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 4));
      private static final Deque<LongRangePathPlanner.AsyncSearchQueue.SearchWork> PENDING = new ArrayDeque<>();
      private static final Map<LongRangePathPlanner.Request, LongRangePathPlanner.AsyncSearchQueue.SearchWork> ACTIVE = new HashMap<>();

      private static synchronized void submit(Plugin plugin, LongRangePathPlanner.Request request) {
         PENDING.addLast(new LongRangePathPlanner.AsyncSearchQueue.SearchWork(plugin, request));
         dispatch();
      }

      private static synchronized void dispatch() {
         while (ACTIVE.size() < MAX_CONCURRENT_SEARCHES && !PENDING.isEmpty()) {
            LongRangePathPlanner.AsyncSearchQueue.SearchWork work = PENDING.removeFirst();
            if (work.request.isCancelled()) {
               work.request.snapshots.clear();
            } else {
               ACTIVE.put(work.request, work);

               try {
                  work.task = work.plugin.getServer().getScheduler().runTaskAsynchronously(work.plugin, () -> run(work));
               } catch (RuntimeException var2) {
                  ACTIVE.remove(work.request, work);
                  work.request.cancelled.set(true);
                  work.request.snapshots.clear();
                  if (work.plugin.isEnabled()) {
                     work.plugin.getLogger().warning("Could not schedule path search: " + var2.getMessage());
                  }
               }
            }
         }
      }

      private static void run(LongRangePathPlanner.AsyncSearchQueue.SearchWork work) {
         synchronized (LongRangePathPlanner.AsyncSearchQueue.class) {
            if (ACTIVE.get(work.request) != work) {
               return;
            }

            work.started = true;
         }

         try {
            if (work.request.isCancelled()) {
               return;
            }

            LongRangePathPlanner.PlanResult result = work.request.search();
            if (!work.request.isCancelled()) {
               work.plugin.getServer().getScheduler().runTask(work.plugin, () -> {
                  if (!work.request.isCancelled()) {
                     work.request.callback.accept(result);
                  }
               });
               return;
            }
         } catch (RuntimeException var19) {
            work.request.cancelled.set(true);
            if (work.plugin.isEnabled()) {
               work.plugin.getLogger().warning("Path search failed: " + var19.getMessage());
            }

            return;
         } finally {
            work.request.snapshots.clear();
            synchronized (LongRangePathPlanner.AsyncSearchQueue.class) {
               ACTIVE.remove(work.request, work);
               dispatch();
            }
         }
      }

      private static synchronized void cancel(LongRangePathPlanner.Request request) {
         PENDING.removeIf(work -> work.request == request);
         LongRangePathPlanner.AsyncSearchQueue.SearchWork active = ACTIVE.get(request);
         if (active == null || !active.started) {
            if (active != null) {
               if (active.task != null) {
                  active.task.cancel();
               }

               ACTIVE.remove(request, active);
            }

            request.snapshots.clear();
            dispatch();
         }
      }

      private static synchronized void shutdown() {
         List<LongRangePathPlanner.Request> requests = new ArrayList<>(ACTIVE.keySet());

         for (LongRangePathPlanner.AsyncSearchQueue.SearchWork work : PENDING) {
            requests.add(work.request);
         }

         for (LongRangePathPlanner.Request request : requests) {
            request.cancelled.set(true);
         }

         for (LongRangePathPlanner.Request request : requests) {
            cancel(request);
         }
      }

      private static final class SearchWork {
         private final Plugin plugin;
         private final LongRangePathPlanner.Request request;
         private BukkitTask task;
         private boolean started;

         private SearchWork(Plugin plugin, LongRangePathPlanner.Request request) {
            this.plugin = plugin;
            this.request = request;
         }
      }
   }

   private record ChunkCoordinate(int x, int z) {
      long key() {
         return (long)this.x << 32 ^ this.z & 4294967295L;
      }

      double distanceSquared(double blockX, double blockZ) {
         double dx = this.x * 16.0 + 8.0 - blockX;
         double dz = this.z * 16.0 + 8.0 - blockZ;
         return dx * dx + dz * dz;
      }
   }

   private static final class MaterialProperties {
      private static volatile SnapshotPathSolver.BlockProperties[] cached;

      private static SnapshotPathSolver.BlockProperties[] snapshot() {
         SnapshotPathSolver.BlockProperties[] existing = cached;
         if (existing != null) {
            return existing;
         } else {
            Material[] materials = Material.values();
            SnapshotPathSolver.BlockProperties[] captured = new SnapshotPathSolver.BlockProperties[materials.length];

            for (Material material : materials) {
               captured[material.ordinal()] = classify(material);
            }

            cached = captured;
            return captured;
         }
      }

      private static SnapshotPathSolver.BlockProperties classify(Material material) {
         String name = material.name();
         if (!name.startsWith("LEGACY_") && material.isBlock()) {
            boolean hazard = material == Material.LAVA
               || name.equals("FIRE")
               || name.equals("SOUL_FIRE")
               || name.equals("CACTUS")
               || name.equals("SWEET_BERRY_BUSH")
               || name.equals("POWDER_SNOW")
               || name.equals("WITHER_ROSE")
               || name.equals("MAGMA_BLOCK")
               || name.endsWith("CAMPFIRE");
            boolean water = material == Material.WATER || name.equals("BUBBLE_COLUMN");
            boolean woodenDoor = name.endsWith("_DOOR") && material != Material.IRON_DOOR;
            boolean solid = material.isSolid();
            boolean support = solid
               && !hazard
               && !name.endsWith("_FENCE")
               && !name.endsWith("_WALL")
               && !name.endsWith("_FENCE_GATE")
               && !name.endsWith("_DOOR")
               && !name.endsWith("_TRAPDOOR")
               && !name.endsWith("_CAULDRON");
            return new SnapshotPathSolver.BlockProperties(!hazard && (!solid || woodenDoor || water), support, water, woodenDoor, hazard);
         } else {
            return SnapshotPathSolver.BlockProperties.UNKNOWN;
         }
      }
   }

   static enum Outcome {
      FOUND,
      NO_PATH,
      TERRAIN_UNAVAILABLE;
   }

   record PlanResult(LongRangePathPlanner.Outcome outcome, List<Location> waypoints, List<Location> preview, boolean reachesFinal, boolean exhaustedNodeBudget) {
      PlanResult(LongRangePathPlanner.Outcome outcome, List<Location> waypoints, List<Location> preview, boolean reachesFinal, boolean exhaustedNodeBudget) {
         waypoints = cloneLocations(waypoints);
         preview = cloneLocations(preview);
         this.outcome = outcome;
         this.waypoints = waypoints;
         this.preview = preview;
         this.reachesFinal = reachesFinal;
         this.exhaustedNodeBudget = exhaustedNodeBudget;
      }

      static LongRangePathPlanner.PlanResult found(List<Location> waypoints, List<Location> preview, boolean reachesFinal, boolean exhaustedNodeBudget) {
         return new LongRangePathPlanner.PlanResult(LongRangePathPlanner.Outcome.FOUND, waypoints, preview, reachesFinal, exhaustedNodeBudget);
      }

      static LongRangePathPlanner.PlanResult noPath(boolean exhaustedNodeBudget) {
         return new LongRangePathPlanner.PlanResult(LongRangePathPlanner.Outcome.NO_PATH, List.of(), List.of(), false, exhaustedNodeBudget);
      }

      static LongRangePathPlanner.PlanResult terrainUnavailable(boolean exhaustedNodeBudget) {
         return new LongRangePathPlanner.PlanResult(LongRangePathPlanner.Outcome.TERRAIN_UNAVAILABLE, List.of(), List.of(), false, exhaustedNodeBudget);
      }

      private static List<Location> cloneLocations(List<Location> locations) {
         return locations.stream().<Location>map(Location::clone).toList();
      }
   }

   static final class Request implements AutoCloseable, Workload {
      private final Location start;
      private final Location planningTarget;
      private final Location finalTarget;
      private final boolean reachesFinal;
      private final SnapshotPathSolver.BodyProfile body;
      private final Consumer<LongRangePathPlanner.PlanResult> callback;
      private final List<LongRangePathPlanner.ChunkCoordinate> chunks;
      private final int minimumY;
      private final int maximumY;
      private final Map<Long, ChunkSnapshot> snapshots = new HashMap<>();
      private final AtomicBoolean cancelled = new AtomicBoolean();
      private final long requestGeneration = LongRangePathPlanner.generation;
      private SnapshotPathSolver.BlockProperties[] materialProperties;
      private int nextChunk;
      private boolean missingTerrain;

      private Request(
         Location start,
         Location planningTarget,
         Location finalTarget,
         boolean reachesFinal,
         SnapshotPathSolver.BodyProfile body,
         Consumer<LongRangePathPlanner.PlanResult> callback
      ) {
         this.start = start;
         this.planningTarget = planningTarget;
         this.finalTarget = finalTarget;
         this.reachesFinal = reachesFinal;
         this.body = body;
         this.callback = callback;
         this.chunks = LongRangePathPlanner.corridorChunks(start, planningTarget);
         World world = start.getWorld();
         this.minimumY = world == null ? -64 : world.getMinHeight();
         this.maximumY = world == null ? 320 : world.getMaxHeight();
      }

      private static LongRangePathPlanner.Request cancelled() {
         LongRangePathPlanner.Request request = new LongRangePathPlanner.Request(
            new Location(null, 0.0, 0.0, 0.0),
            new Location(null, 0.0, 0.0, 0.0),
            new Location(null, 0.0, 0.0, 0.0),
            true,
            new SnapshotPathSolver.BodyProfile(SnapshotPathSolver.MovementMode.GROUND, 0, 1),
            ignored -> {}
         );
         request.cancelled.set(true);
         return request;
      }

      private void start() {
         if (this.chunks.isEmpty()) {
            this.dispatchSearch();
         } else {
            LongRangePathPlanner.SnapshotWorkQueue.enqueue(this);
         }
      }

      @Override
      public void compute() {
         if (!this.isCancelled()) {
            if (this.materialProperties == null) {
               this.materialProperties = LongRangePathPlanner.MaterialProperties.snapshot();
            }

            World world = this.start.getWorld();
            if (world != null && this.nextChunk < this.chunks.size()) {
               LongRangePathPlanner.ChunkCoordinate coordinate = this.chunks.get(this.nextChunk++);
               if (!world.isChunkLoaded(coordinate.x(), coordinate.z())) {
                  this.missingTerrain = true;
               } else {
                  Chunk chunk = world.getChunkAt(coordinate.x(), coordinate.z());
                  this.snapshots.put(coordinate.key(), chunk.getChunkSnapshot(false, false, false));
               }

               if (this.nextChunk < this.chunks.size()) {
                  LongRangePathPlanner.SnapshotWorkQueue.enqueue(this);
               } else {
                  this.dispatchSearch();
               }
            } else {
               this.dispatchSearch();
            }
         }
      }

      private void dispatchSearch() {
         if (!this.isCancelled()) {
            Plugin plugin = MagmaCore.getInstance().getRequestingPlugin();
            LongRangePathPlanner.AsyncSearchQueue.submit(plugin, this);
         }
      }

      private LongRangePathPlanner.PlanResult search() {
         World world = this.start.getWorld();
         if (world == null) {
            return LongRangePathPlanner.PlanResult.noPath(false);
         } else {
            LongRangePathPlanner.SnapshotTerrain terrain = new LongRangePathPlanner.SnapshotTerrain(
               this.snapshots, this.materialProperties, this.minimumY, this.maximumY
            );
            SnapshotPathSolver.Result solved = SnapshotPathSolver.solve(
               terrain,
               new SnapshotPathSolver.Point(this.start.getBlockX(), this.start.getBlockY(), this.start.getBlockZ()),
               new SnapshotPathSolver.Point(this.planningTarget.getBlockX(), this.planningTarget.getBlockY(), this.planningTarget.getBlockZ()),
               this.body,
               180000,
               this::isCancelled
            );
            if (!solved.found()) {
               return this.missingTerrain
                  ? LongRangePathPlanner.PlanResult.terrainUnavailable(solved.exhaustedNodeBudget())
                  : LongRangePathPlanner.PlanResult.noPath(solved.exhaustedNodeBudget());
            } else {
               List<Location> preview = solved.points().stream().map(point -> LongRangePathPlanner.location(world, point)).toList();
               List<Location> waypoints = new ArrayList<>(
                  LongRangePathPlanner.navigationWaypoints(solved.points()).stream().map(point -> LongRangePathPlanner.location(world, point)).toList()
               );
               if (this.reachesFinal) {
                  if (!waypoints.isEmpty() && !(waypoints.getLast().distanceSquared(this.finalTarget) > 1.0E-8)) {
                     waypoints.set(waypoints.size() - 1, this.finalTarget.clone());
                  } else {
                     waypoints.add(this.finalTarget.clone());
                  }

                  if (preview.isEmpty() || preview.getLast().distanceSquared(this.finalTarget) > 1.0E-8) {
                     preview = new ArrayList<>(preview);
                     preview.add(this.finalTarget.clone());
                  }
               }

               return LongRangePathPlanner.PlanResult.found(waypoints, preview, this.reachesFinal, solved.exhaustedNodeBudget());
            }
         }
      }

      @Override
      public void close() {
         this.cancelled.set(true);
         LongRangePathPlanner.SnapshotWorkQueue.discard(this);
         LongRangePathPlanner.AsyncSearchQueue.cancel(this);
      }

      boolean isCancelled() {
         return this.cancelled.get() || this.requestGeneration != LongRangePathPlanner.generation;
      }
   }

   private record SnapshotTerrain(Map<Long, ChunkSnapshot> snapshots, SnapshotPathSolver.BlockProperties[] materialProperties, int minimumY, int maximumY)
      implements SnapshotPathSolver.Terrain {
      @Override
      public SnapshotPathSolver.BlockProperties blockProperties(int x, int y, int z) {
         if (y >= this.minimumY && y < this.maximumY) {
            int chunkX = Math.floorDiv(x, 16);
            int chunkZ = Math.floorDiv(z, 16);
            ChunkSnapshot snapshot = this.snapshots.get((long)chunkX << 32 ^ chunkZ & 4294967295L);
            if (snapshot == null) {
               return SnapshotPathSolver.BlockProperties.UNKNOWN;
            } else {
               Material material = snapshot.getBlockType(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
               return this.materialProperties[material.ordinal()];
            }
         } else {
            return SnapshotPathSolver.BlockProperties.UNKNOWN;
         }
      }
   }

   private static final class SnapshotWorkQueue {
      private static WorkloadRunnable runner;
      private static final Set<LongRangePathPlanner.Request> pending = new HashSet<>();

      private static void enqueue(LongRangePathPlanner.Request request) {
         pending.add(request);
         Workload workload = () -> {
            pending.remove(request);
            request.compute();
         };
         if (runner == null) {
            runner = new WorkloadRunnable(0.04, LongRangePathPlanner.SnapshotWorkQueue::finished);
            runner.addWorkload(workload);
            runner.startSync();
         } else {
            runner.addWorkload(workload);
         }
      }

      private static void finished() {
         runner = null;
      }

      private static void discard(LongRangePathPlanner.Request request) {
         pending.remove(request);
      }

      private static void shutdown() {
         if (runner != null) {
            runner.cancel();
         }

         runner = null;

         for (LongRangePathPlanner.Request request : List.copyOf(pending)) {
            request.close();
         }

         pending.clear();
      }
   }
}
