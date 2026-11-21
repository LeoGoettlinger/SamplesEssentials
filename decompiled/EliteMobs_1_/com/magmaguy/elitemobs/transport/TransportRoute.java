package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.magmacore.ai.route.CurvedRoute;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public record TransportRoute(
   String id, String name, String world, String transportEntity, double speed, double acceleration, int countdown, float arrivalYaw, List<Vector> points
) {
   public TransportRoute(
      String id, String name, String world, String transportEntity, double speed, double acceleration, int countdown, float arrivalYaw, List<Vector> points
   ) {
      if (!id.matches("[a-z0-9_-]{1,64}")) {
         throw new IllegalArgumentException("Invalid route id");
      } else if (name == null || name.isBlank() || world == null || world.isBlank()) {
         throw new IllegalArgumentException("Route needs a name and authored world");
      } else if (transportEntity == null || transportEntity.isBlank() || !transportEntity.endsWith(".yml")) {
         throw new IllegalArgumentException("transportEntity must name an enabled customboss .yml file");
      } else if (!Double.isFinite(speed) || speed <= 0.0 || !Double.isFinite(acceleration) || acceleration <= 0.0) {
         throw new IllegalArgumentException("Speed and acceleration must be finite and positive");
      } else if (countdown >= 0 && Float.isFinite(arrivalYaw)) {
         points = points.stream().<Vector>map(Vector::clone).toList();
         new CurvedRoute(points);
         this.id = id;
         this.name = name;
         this.world = world;
         this.transportEntity = transportEntity;
         this.speed = speed;
         this.acceleration = acceleration;
         this.countdown = countdown;
         this.arrivalYaw = arrivalYaw;
         this.points = points;
      } else {
         throw new IllegalArgumentException("Invalid countdown or arrival yaw");
      }
   }

   public List<Vector> points() {
      return this.points.stream().<Vector>map(Vector::clone).toList();
   }

   public CurvedRoute curve() {
      return new CurvedRoute(this.points);
   }

   public static TransportRoute read(String id, FileConfiguration yaml) {
      if (!yaml.contains("carrier") && !yaml.contains("customModel") && !yaml.contains("flightAnimation")) {
         List<Vector> nodes = yaml.getStringList("waypoints").stream().map(line -> {
            String[] values = line.split(",");
            if (values.length != 3) {
               throw new IllegalArgumentException("Waypoint must be x,y,z");
            } else {
               return new Vector(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
            }
         }).toList();
         return new TransportRoute(
            id,
            yaml.getString("name", id),
            yaml.getString("world"),
            yaml.getString("transportEntity"),
            yaml.getDouble("speed", 12.0),
            yaml.getDouble("acceleration", 8.0),
            yaml.getInt("countdownTicks", 60),
            (float)yaml.getDouble("arrivalYaw", 0.0),
            nodes
         );
      } else {
         throw new IllegalArgumentException(
            "Obsolete route entity fields. Set transportEntity to an ordinary customboss .yml file; configure entityType, customModel, disguise and powers there. This file has not been changed."
         );
      }
   }

   void write(YamlConfiguration yaml, boolean create) {
      if (create) {
         yaml.set("isEnabled", true);
         yaml.set("name", this.name);
         yaml.set("world", this.world);
         yaml.set("transportEntity", this.transportEntity);
         yaml.set("speed", this.speed);
         yaml.set("acceleration", this.acceleration);
         yaml.set("countdownTicks", this.countdown);
      }

      yaml.set("arrivalYaw", this.arrivalYaw);
      yaml.set("waypoints", this.points.stream().map(p -> p.getX() + "," + p.getY() + "," + p.getZ()).toList());
   }
}
