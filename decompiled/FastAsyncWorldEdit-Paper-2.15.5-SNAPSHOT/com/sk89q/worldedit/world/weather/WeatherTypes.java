package com.sk89q.worldedit.world.weather;

import javax.annotation.Nullable;

public final class WeatherTypes {
   public static final WeatherType CLEAR = register("clear");
   public static final WeatherType RAIN = register("rain");
   public static final WeatherType THUNDER_STORM = register("thunder_storm");

   private WeatherTypes() {
   }

   private static WeatherType register(String id) {
      return register(new WeatherType(id));
   }

   public static WeatherType register(WeatherType weather) {
      return WeatherType.REGISTRY.register(weather.id(), weather);
   }

   @Nullable
   public static WeatherType get(final String id) {
      return WeatherType.REGISTRY.get(id);
   }
}
