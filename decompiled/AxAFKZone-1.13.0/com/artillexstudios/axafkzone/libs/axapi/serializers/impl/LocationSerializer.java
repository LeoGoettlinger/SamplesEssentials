package com.artillexstudios.axafkzone.libs.axapi.serializers.impl;

import com.artillexstudios.axafkzone.libs.axapi.serializers.Serializer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerializer implements Serializer<Location, String> {
   private static final Pattern SPLIT_PATTERN = Pattern.compile(";");

   public String serialize(Location object) {
      return object.getWorld().getName() + ";" + object.getX() + ";" + object.getY() + ";" + object.getZ() + ";" + object.getYaw() + ";" + object.getPitch();
   }

   public Location deserialize(String value) {
      String[] split = SPLIT_PATTERN.split(value);
      return new Location(
         Bukkit.getWorld(split[0]),
         Double.parseDouble(split[1]),
         Double.parseDouble(split[2]),
         Double.parseDouble(split[3]),
         Float.parseFloat(split[4]),
         Float.parseFloat(split[5])
      );
   }
}
