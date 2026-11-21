package com.magmaguy.elitemobs.pathfinding.patrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

public final class PatrolRoute {
   public static final String ROOT = "patrol";
   private final List<Vector> nodes;
   private final boolean relative;
   private final PatrolMode mode;
   private final double speedModifier;
   private final Double virtualSpeed;
   private final double startNodeWaitSeconds;

   private PatrolRoute(List<Vector> nodes, boolean relative, PatrolMode mode, double speedModifier, Double virtualSpeed, double startNodeWaitSeconds) {
      this.nodes = Collections.unmodifiableList(nodes.stream().map(Vector::clone).toList());
      this.relative = relative;
      this.mode = Objects.requireNonNull(mode, "mode");
      this.speedModifier = requirePositiveFinite(speedModifier, "patrol.speed");
      this.virtualSpeed = virtualSpeed == null ? null : requirePositiveFinite(virtualSpeed, "patrol.virtualSpeed");
      if (Double.isFinite(startNodeWaitSeconds) && !(startNodeWaitSeconds < 0.0)) {
         this.startNodeWaitSeconds = startNodeWaitSeconds;
         this.validateGeometry();
      } else {
         throw new IllegalArgumentException("patrol.startNodeWaitSeconds must be finite and not negative");
      }
   }

   public static PatrolRoute parse(FileConfiguration configuration) {
      Objects.requireNonNull(configuration, "configuration");
      if (!configuration.contains("patrol.nodes")) {
         return null;
      } else if (Boolean.FALSE.equals(setting(configuration, "enabled"))) {
         return null;
      } else {
         List<?> rawNodes = configuration.getList("patrol.nodes");
         if (rawNodes == null) {
            throw new IllegalArgumentException("patrol.nodes must be a list");
         } else {
            List<Vector> nodes = new ArrayList<>();

            for (int index = 0; index < rawNodes.size(); index++) {
               nodes.add(parseNode(rawNodes.get(index), index));
            }

            if (nodes.size() > 2 && nodes.getFirst().distanceSquared(nodes.getLast()) < 1.0E-8) {
               nodes.removeLast();
            }

            if (configuration.contains("patrol.relative") && !configuration.isBoolean("patrol.relative")) {
               throw new IllegalArgumentException("patrol.relative must be true or false");
            } else {
               boolean relative = !Boolean.FALSE.equals(setting(configuration, "relative"));
               Object rawMode = setting(configuration, "mode");
               PatrolMode mode = PatrolMode.parse(rawMode == null ? PatrolMode.LOOP.name() : rawMode.toString());
               double speed = numberSetting(setting(configuration, "speed"), 1.0);
               Double virtualSpeed = configuration.contains("patrol.virtualSpeed") ? configuration.getDouble("patrol.virtualSpeed") : null;
               double startNodeWaitSeconds = numberSetting(setting(configuration, "startNodeWaitSeconds"), 0.0);
               return new PatrolRoute(nodes, relative, mode, speed, virtualSpeed, startNodeWaitSeconds);
            }
         }
      }
   }

   public List<Vector> nodes() {
      return this.nodes.stream().<Vector>map(Vector::clone).toList();
   }

   public boolean relative() {
      return this.relative;
   }

   public PatrolMode mode() {
      return this.mode;
   }

   public double speedModifier() {
      return this.speedModifier;
   }

   public Double virtualSpeed() {
      return this.virtualSpeed;
   }

   public double startNodeWaitSeconds() {
      return this.startNodeWaitSeconds;
   }

   public int startNodeWaitTicks() {
      return (int)Math.min(2147483647L, Math.round(this.startNodeWaitSeconds * 20.0));
   }

   public int size() {
      return this.nodes.size();
   }

   public Location node(PatrolOrigin origin, int index) {
      Location base = requireResolvedOrigin(origin);
      Vector node = this.nodes.get(this.requireNode(index));
      if (this.relative) {
         return base.add(node);
      } else {
         base.setX(node.getX());
         base.setY(node.getY());
         base.setZ(node.getZ());
         return base;
      }
   }

   public PatrolRoute.Step next(int currentNode, int direction) {
      this.requireNode(currentNode);
      if (this.mode == PatrolMode.LOOP) {
         return new PatrolRoute.Step((currentNode + 1) % this.nodes.size(), 1);
      } else {
         int resolvedDirection = direction < 0 ? -1 : 1;
         if (currentNode == 0) {
            resolvedDirection = 1;
         } else if (currentNode == this.nodes.size() - 1) {
            resolvedDirection = -1;
         }

         return new PatrolRoute.Step(currentNode + resolvedDirection, resolvedDirection);
      }
   }

   public int nearestNode(PatrolOrigin origin, Location location) {
      if (!sameWorld(origin, location)) {
         return 0;
      } else {
         int nearest = 0;
         double nearestDistance = Double.POSITIVE_INFINITY;

         for (int index = 0; index < this.nodes.size(); index++) {
            double distance = this.node(origin, index).distanceSquared(location);
            if (distance < nearestDistance) {
               nearest = index;
               nearestDistance = distance;
            }
         }

         return nearest;
      }
   }

   public Location closestPoint(PatrolOrigin origin, Location location) {
      if (!sameWorld(origin, location)) {
         return this.node(origin, 0);
      } else {
         Location closest = this.node(origin, 0);
         double closestDistance = closest.distanceSquared(location);
         int segmentCount = this.mode == PatrolMode.LOOP ? this.nodes.size() : this.nodes.size() - 1;

         for (int startIndex = 0; startIndex < segmentCount; startIndex++) {
            int endIndex = (startIndex + 1) % this.nodes.size();
            Location candidate = project(location, this.node(origin, startIndex), this.node(origin, endIndex));
            double distance = candidate.distanceSquared(location);
            if (distance < closestDistance) {
               closest = candidate;
               closestDistance = distance;
            }
         }

         return closest;
      }
   }

   public double projectedFraction(PatrolOrigin origin, Location location, int startNode, int targetNode) {
      if (!sameWorld(origin, location)) {
         return 0.0;
      } else {
         Location start = this.node(origin, startNode);
         Location end = this.node(origin, targetNode);
         Vector leg = end.toVector().subtract(start.toVector());
         double lengthSquared = leg.lengthSquared();
         if (lengthSquared <= 1.0E-8) {
            return 1.0;
         } else {
            double projection = location.toVector().subtract(start.toVector()).dot(leg) / lengthSquared;
            return Math.max(0.0, Math.min(1.0, projection));
         }
      }
   }

   public double legLength(PatrolOrigin origin, int startNode, int targetNode) {
      return this.node(origin, startNode).distance(this.node(origin, targetNode));
   }

   public Location interpolate(PatrolOrigin origin, int startNode, int targetNode, double fraction) {
      Location start = this.node(origin, startNode);
      Vector delta = this.node(origin, targetNode).toVector().subtract(start.toVector());
      return start.add(delta.multiply(Math.max(0.0, Math.min(1.0, fraction))));
   }

   public String geometryKey() {
      return Integer.toHexString((this.relative + "|" + String.join(";", this.serializeNodes())).hashCode());
   }

   public List<String> serializeNodes() {
      return this.nodes.stream().map(node -> format(node.getX()) + "," + format(node.getY()) + "," + format(node.getZ())).toList();
   }

   private void validateGeometry() {
      if (this.nodes.size() < 2) {
         throw new IllegalArgumentException("patrol.nodes requires at least two distinct nodes");
      } else {
         for (int index = 0; index < this.nodes.size(); index++) {
            Vector node = this.nodes.get(index);
            if (!Double.isFinite(node.getX()) || !Double.isFinite(node.getY()) || !Double.isFinite(node.getZ())) {
               throw new IllegalArgumentException("patrol.nodes[" + index + "] contains a non-finite coordinate");
            }
         }

         int segmentCount = this.mode == PatrolMode.LOOP ? this.nodes.size() : this.nodes.size() - 1;

         for (int start = 0; start < segmentCount; start++) {
            int end = (start + 1) % this.nodes.size();
            double length = this.nodes.get(start).distance(this.nodes.get(end));
            if (length <= 1.0E-4) {
               throw new IllegalArgumentException("patrol leg " + start + " -> " + end + " has duplicate nodes");
            }
         }
      }
   }

   private int requireNode(int index) {
      if (index >= 0 && index < this.nodes.size()) {
         return index;
      } else {
         throw new IllegalArgumentException("Invalid patrol node " + index);
      }
   }

   private static Vector parseNode(Object rawNode, int index) {
      if (rawNode instanceof String value) {
         String[] parts = value.split(",");
         if (parts.length != 3) {
            throw new IllegalArgumentException("patrol.nodes[" + index + "] must be x,y,z");
         } else {
            try {
               return new Vector(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()), Double.parseDouble(parts[2].trim()));
            } catch (NumberFormatException var5) {
               throw new IllegalArgumentException("patrol.nodes[" + index + "] contains an invalid number", var5);
            }
         }
      } else if (rawNode instanceof ConfigurationSection section) {
         return new Vector(section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
      } else if (rawNode instanceof Map<?, ?> map) {
         return new Vector(number(map.get("x"), index), number(map.get("y"), index), number(map.get("z"), index));
      } else {
         throw new IllegalArgumentException("patrol.nodes[" + index + "] must be x,y,z or an x/y/z map");
      }
   }

   private static Object setting(FileConfiguration configuration, String key) {
      return configuration.get("patrol." + key);
   }

   private static double numberSetting(Object value, double fallback) {
      return value instanceof Number number ? number.doubleValue() : fallback;
   }

   private static double number(Object value, int index) {
      if (value instanceof Number number) {
         return number.doubleValue();
      } else {
         if (value instanceof String string) {
            try {
               return Double.parseDouble(string);
            } catch (NumberFormatException var4) {
            }
         }

         throw new IllegalArgumentException("patrol.nodes[" + index + "] has a missing or invalid coordinate");
      }
   }

   private static double requirePositiveFinite(double value, String path) {
      if (Double.isFinite(value) && !(value <= 0.0)) {
         return value;
      } else {
         throw new IllegalArgumentException(path + " must be finite and positive");
      }
   }

   private static boolean sameWorld(PatrolOrigin origin, Location location) {
      return location != null && location.getWorld() != null && origin.worldName().equals(location.getWorld().getName());
   }

   private static Location requireResolvedOrigin(PatrolOrigin origin) {
      Location location = origin.resolve();
      if (location == null) {
         throw new IllegalStateException("Patrol world is not loaded: " + origin.worldName());
      } else {
         return location;
      }
   }

   private static Location project(Location point, Location start, Location end) {
      Vector segment = end.toVector().subtract(start.toVector());
      double lengthSquared = segment.lengthSquared();
      if (lengthSquared <= 1.0E-8) {
         return start.clone();
      } else {
         double fraction = point.toVector().subtract(start.toVector()).dot(segment) / lengthSquared;
         return start.clone().add(segment.multiply(Math.max(0.0, Math.min(1.0, fraction))));
      }
   }

   private static String format(double value) {
      return Double.toString(value);
   }

   public record Step(int targetNode, int direction) {
   }
}
