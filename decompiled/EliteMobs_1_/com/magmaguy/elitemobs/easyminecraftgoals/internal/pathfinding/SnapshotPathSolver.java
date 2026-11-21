package com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BooleanSupplier;

final class SnapshotPathSolver {
   private static final int[][] HORIZONTAL_DIRECTIONS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   private static final int[] GROUND_Y_OFFSETS = new int[]{0, 1, -1, -2, -3};

   private SnapshotPathSolver() {
   }

   static SnapshotPathSolver.Result solve(
      SnapshotPathSolver.Terrain terrain,
      SnapshotPathSolver.Point requestedStart,
      SnapshotPathSolver.Point requestedGoal,
      SnapshotPathSolver.BodyProfile body,
      int maximumVisitedNodes
   ) {
      return solve(terrain, requestedStart, requestedGoal, body, maximumVisitedNodes, () -> false);
   }

   static SnapshotPathSolver.Result solve(
      SnapshotPathSolver.Terrain terrain,
      SnapshotPathSolver.Point requestedStart,
      SnapshotPathSolver.Point requestedGoal,
      SnapshotPathSolver.BodyProfile body,
      int maximumVisitedNodes,
      BooleanSupplier cancelled
   ) {
      if (cancelled.getAsBoolean()) {
         return SnapshotPathSolver.Result.noPath(false);
      } else {
         SnapshotPathSolver.Point start = nearestValid(terrain, requestedStart, body, 2, 8, cancelled);
         if (start == null) {
            return SnapshotPathSolver.Result.noPath(false);
         } else {
            SnapshotPathSolver.Point goal = nearestValid(terrain, requestedGoal, body, 8, 48, cancelled);
            if (goal == null) {
               return SnapshotPathSolver.Result.noPath(false);
            } else if (start.equals(goal)) {
               return new SnapshotPathSolver.Result(List.of(start), false);
            } else {
               PriorityQueue<SnapshotPathSolver.SearchNode> open = new PriorityQueue<>(Comparator.comparingDouble(SnapshotPathSolver.SearchNode::score));
               Map<SnapshotPathSolver.Point, Double> distances = new HashMap<>();
               Map<SnapshotPathSolver.Point, SnapshotPathSolver.Point> previous = new HashMap<>();
               Set<SnapshotPathSolver.Point> closed = new HashSet<>();
               distances.put(start, 0.0);
               open.add(new SnapshotPathSolver.SearchNode(start, heuristic(start, goal)));
               int visited = 0;

               while (!open.isEmpty()) {
                  if (cancelled.getAsBoolean()) {
                     return SnapshotPathSolver.Result.noPath(false);
                  }

                  SnapshotPathSolver.SearchNode next = open.poll();
                  SnapshotPathSolver.Point current = next.point();
                  if (closed.add(current)) {
                     if (++visited > maximumVisitedNodes) {
                        return SnapshotPathSolver.Result.noPath(true);
                     }

                     if (current.equals(goal)) {
                        return new SnapshotPathSolver.Result(reconstruct(previous, current), false);
                     }

                     for (SnapshotPathSolver.Point neighbour : neighbours(terrain, current, body)) {
                        if (!closed.contains(neighbour)) {
                           double candidateDistance = distances.get(current) + movementCost(terrain, current, neighbour, body);
                           if (!(candidateDistance + 1.0E-9 >= distances.getOrDefault(neighbour, Double.POSITIVE_INFINITY))) {
                              previous.put(neighbour, current);
                              distances.put(neighbour, candidateDistance);
                              open.add(new SnapshotPathSolver.SearchNode(neighbour, candidateDistance + heuristic(neighbour, goal)));
                           }
                        }
                     }
                  }
               }

               return SnapshotPathSolver.Result.noPath(false);
            }
         }
      }
   }

   private static List<SnapshotPathSolver.Point> neighbours(
      SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point current, SnapshotPathSolver.BodyProfile body
   ) {
      return switch (body.mode()) {
         case GROUND -> groundNeighbours(terrain, current, body);
         case FLYING, AQUATIC -> volumeNeighbours(terrain, current, body);
      };
   }

   private static List<SnapshotPathSolver.Point> groundNeighbours(
      SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point current, SnapshotPathSolver.BodyProfile body
   ) {
      List<SnapshotPathSolver.Point> neighbours = new ArrayList<>(10);

      for (int[] direction : HORIZONTAL_DIRECTIONS) {
         int dx = direction[0];
         int dz = direction[1];
         SnapshotPathSolver.Point candidate = groundTransition(terrain, current, dx, dz, body);
         if (candidate != null
            && (dx == 0 || dz == 0 || groundTransition(terrain, current, dx, 0, body) != null && groundTransition(terrain, current, 0, dz, body) != null)) {
            neighbours.add(candidate);
         }
      }

      SnapshotPathSolver.BlockProperties feet = terrain.blockProperties(current.x(), current.y(), current.z());
      if (isWater(feet)) {
         SnapshotPathSolver.Point above = current.add(0, 1, 0);
         SnapshotPathSolver.Point below = current.add(0, -1, 0);
         if (isValid(terrain, above, body)) {
            neighbours.add(above);
         }

         if (isValid(terrain, below, body)) {
            neighbours.add(below);
         }
      }

      return neighbours;
   }

   private static SnapshotPathSolver.Point groundTransition(
      SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point current, int dx, int dz, SnapshotPathSolver.BodyProfile body
   ) {
      for (int offset : GROUND_Y_OFFSETS) {
         SnapshotPathSolver.Point candidate = current.add(dx, offset, dz);
         if (isValid(terrain, candidate, body)) {
            return candidate;
         }
      }

      return null;
   }

   private static List<SnapshotPathSolver.Point> volumeNeighbours(
      SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point current, SnapshotPathSolver.BodyProfile body
   ) {
      List<SnapshotPathSolver.Point> neighbours = new ArrayList<>(26);

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  SnapshotPathSolver.Point candidate = current.add(dx, dy, dz);
                  if (isValid(terrain, candidate, body)) {
                     neighbours.add(candidate);
                  }
               }
            }
         }
      }

      return neighbours;
   }

   private static SnapshotPathSolver.Point nearestValid(
      SnapshotPathSolver.Terrain terrain,
      SnapshotPathSolver.Point requested,
      SnapshotPathSolver.BodyProfile body,
      int horizontalRadius,
      int verticalRadius,
      BooleanSupplier cancelled
   ) {
      if (isValid(terrain, requested, body)) {
         return requested;
      } else {
         SnapshotPathSolver.Point best = null;
         double bestDistance = Double.POSITIVE_INFINITY;

         for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
               if (cancelled.getAsBoolean()) {
                  return null;
               }

               for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                  SnapshotPathSolver.Point candidate = requested.add(dx, dy, dz);
                  double distance = dx * dx + dz * dz + dy * dy * 0.25;
                  if (!(distance >= bestDistance) && isValid(terrain, candidate, body)) {
                     best = candidate;
                     bestDistance = distance;
                  }
               }
            }
         }

         return best;
      }
   }

   private static boolean isValid(SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point point, SnapshotPathSolver.BodyProfile body) {
      if (point.y() >= terrain.minimumY() && point.y() + body.heightBlocks() <= terrain.maximumY()) {
         return switch (body.mode()) {
            case GROUND -> hasGroundClearance(terrain, point, body);
            case FLYING -> hasVolumeClearance(terrain, point, body, false);
            case AQUATIC -> hasVolumeClearance(terrain, point, body, true);
         };
      } else {
         return false;
      }
   }

   private static boolean hasGroundClearance(SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point point, SnapshotPathSolver.BodyProfile body) {
      boolean inWater = false;

      for (int dx = -body.horizontalRadius(); dx <= body.horizontalRadius(); dx++) {
         for (int dz = -body.horizontalRadius(); dz <= body.horizontalRadius(); dz++) {
            SnapshotPathSolver.BlockProperties feet = terrain.blockProperties(point.x() + dx, point.y(), point.z() + dz);
            if (!isPassable(feet)) {
               return false;
            }

            inWater |= isWater(feet);

            for (int height = 1; height < body.heightBlocks(); height++) {
               if (!isPassable(terrain.blockProperties(point.x() + dx, point.y() + height, point.z() + dz))) {
                  return false;
               }
            }

            SnapshotPathSolver.BlockProperties support = terrain.blockProperties(point.x() + dx, point.y() - 1, point.z() + dz);
            if (!isSupport(support) && !isWater(feet)) {
               return false;
            }
         }
      }

      return inWater || !isHazard(terrain.blockProperties(point.x(), point.y() - 1, point.z()));
   }

   private static boolean hasVolumeClearance(
      SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point point, SnapshotPathSolver.BodyProfile body, boolean requireWater
   ) {
      for (int dx = -body.horizontalRadius(); dx <= body.horizontalRadius(); dx++) {
         for (int dz = -body.horizontalRadius(); dz <= body.horizontalRadius(); dz++) {
            for (int height = 0; height < body.heightBlocks(); height++) {
               SnapshotPathSolver.BlockProperties block = terrain.blockProperties(point.x() + dx, point.y() + height, point.z() + dz);
               if (requireWater ? !isWater(block) : !isPassable(block)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static double movementCost(
      SnapshotPathSolver.Terrain terrain, SnapshotPathSolver.Point from, SnapshotPathSolver.Point to, SnapshotPathSolver.BodyProfile body
   ) {
      int dx = Math.abs(to.x() - from.x());
      int dy = Math.abs(to.y() - from.y());
      int dz = Math.abs(to.z() - from.z());
      double cost = Math.sqrt(dx * dx + dy * dy + dz * dz);
      SnapshotPathSolver.BlockProperties feet = terrain.blockProperties(to.x(), to.y(), to.z());
      if (isWater(feet) && body.mode() == SnapshotPathSolver.MovementMode.GROUND) {
         cost += 3.0;
      }

      if (isWoodenDoor(feet)) {
         cost += 2.0;
      }

      if (to.y() > from.y()) {
         cost += 0.35;
      }

      return cost;
   }

   private static double heuristic(SnapshotPathSolver.Point from, SnapshotPathSolver.Point to) {
      int dx = to.x() - from.x();
      int dy = to.y() - from.y();
      int dz = to.z() - from.z();
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private static List<SnapshotPathSolver.Point> reconstruct(Map<SnapshotPathSolver.Point, SnapshotPathSolver.Point> previous, SnapshotPathSolver.Point end) {
      List<SnapshotPathSolver.Point> reversed = new ArrayList<>();
      SnapshotPathSolver.Point current = end;

      while (current != null) {
         reversed.add(current);
         current = previous.get(current);
      }

      Collections.reverse(reversed);
      return List.copyOf(reversed);
   }

   private static boolean isPassable(SnapshotPathSolver.BlockProperties block) {
      return block != null && block.passable();
   }

   private static boolean isSupport(SnapshotPathSolver.BlockProperties block) {
      return block != null && block.support();
   }

   private static boolean isWater(SnapshotPathSolver.BlockProperties block) {
      return block != null && block.water();
   }

   private static boolean isWoodenDoor(SnapshotPathSolver.BlockProperties block) {
      return block != null && block.woodenDoor();
   }

   private static boolean isHazard(SnapshotPathSolver.BlockProperties block) {
      return block == null || block.hazard();
   }

   record BlockProperties(boolean passable, boolean support, boolean water, boolean woodenDoor, boolean hazard) {
      static final SnapshotPathSolver.BlockProperties UNKNOWN = new SnapshotPathSolver.BlockProperties(false, false, false, false, true);
   }

   record BodyProfile(SnapshotPathSolver.MovementMode mode, int horizontalRadius, int heightBlocks) {
      BodyProfile(SnapshotPathSolver.MovementMode mode, int horizontalRadius, int heightBlocks) {
         if (horizontalRadius < 0) {
            throw new IllegalArgumentException("horizontalRadius must not be negative");
         } else if (heightBlocks < 1) {
            throw new IllegalArgumentException("heightBlocks must be positive");
         } else {
            this.mode = mode;
            this.horizontalRadius = horizontalRadius;
            this.heightBlocks = heightBlocks;
         }
      }
   }

   static enum MovementMode {
      GROUND,
      FLYING,
      AQUATIC;
   }

   record Point(int x, int y, int z) {
      SnapshotPathSolver.Point add(int dx, int dy, int dz) {
         return new SnapshotPathSolver.Point(this.x + dx, this.y + dy, this.z + dz);
      }
   }

   record Result(List<SnapshotPathSolver.Point> points, boolean exhaustedNodeBudget) {
      Result(List<SnapshotPathSolver.Point> points, boolean exhaustedNodeBudget) {
         points = List.copyOf(points);
         this.points = points;
         this.exhaustedNodeBudget = exhaustedNodeBudget;
      }

      static SnapshotPathSolver.Result noPath(boolean exhaustedNodeBudget) {
         return new SnapshotPathSolver.Result(List.of(), exhaustedNodeBudget);
      }

      boolean found() {
         return !this.points.isEmpty();
      }
   }

   private record SearchNode(SnapshotPathSolver.Point point, double score) {
   }

   interface Terrain {
      SnapshotPathSolver.BlockProperties blockProperties(int var1, int var2, int var3);

      int minimumY();

      int maximumY();
   }
}
