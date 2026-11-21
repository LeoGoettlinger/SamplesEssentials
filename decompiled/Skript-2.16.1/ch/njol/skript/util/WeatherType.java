package ch.njol.skript.util;

import ch.njol.skript.localization.Language;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.jetbrains.annotations.Nullable;

public enum WeatherType {
   CLEAR(),
   RAIN(),
   THUNDER();

   String[] names;
   @Nullable
   String adjective;
   static final Map<String, WeatherType> byName = new HashMap<>();

   private WeatherType(final String... names) {
      this.names = names;
   }

   @Nullable
   public static WeatherType parse(String s) {
      return byName.get(s);
   }

   public static WeatherType fromWorld(World world) {
      assert world != null;

      if (world.isThundering() && world.hasStorm()) {
         return THUNDER;
      } else {
         return world.hasStorm() ? RAIN : CLEAR;
      }
   }

   public static WeatherType fromEvent(WeatherEvent e) {
      if (e instanceof WeatherChangeEvent) {
         return fromEvent((WeatherChangeEvent)e);
      } else if (e instanceof ThunderChangeEvent) {
         return fromEvent((ThunderChangeEvent)e);
      } else {
         assert false;

         return CLEAR;
      }
   }

   public static WeatherType fromEvent(WeatherChangeEvent e) {
      assert e != null;

      if (!e.toWeatherState()) {
         return CLEAR;
      } else {
         return e.getWorld().isThundering() ? THUNDER : RAIN;
      }
   }

   public static WeatherType fromEvent(ThunderChangeEvent e) {
      assert e != null;

      if (e.toThunderState()) {
         return THUNDER;
      } else {
         return e.getWorld().hasStorm() ? RAIN : CLEAR;
      }
   }

   @Nullable
   public static WeatherType fromPlayer(Player player) {
      org.bukkit.WeatherType weather = player.getPlayerWeather();
      if (weather == null) {
         return null;
      } else {
         switch (weather) {
            case DOWNFALL:
               return RAIN;
            case CLEAR:
               return CLEAR;
            default:
               return null;
         }
      }
   }

   public void setWeather(Player player) {
      switch (this) {
         case CLEAR:
            player.setPlayerWeather(org.bukkit.WeatherType.CLEAR);
            break;
         case RAIN:
         case THUNDER:
            player.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL);
            break;
         default:
            player.resetPlayerWeather();
      }
   }

   @Override
   public String toString() {
      return this.names[0];
   }

   public String toString(int flags) {
      return this.names[0];
   }

   @Nullable
   public String adjective() {
      return this.adjective;
   }

   public boolean isWeather(World w) {
      return this.isWeather(w.hasStorm(), w.isThundering());
   }

   public boolean isWeather(boolean rain, boolean thunder) {
      switch (this) {
         case CLEAR:
            return !thunder && !rain;
         case RAIN:
            return !thunder && rain;
         case THUNDER:
            return thunder && rain;
         default:
            assert false;

            return false;
      }
   }

   public void setWeather(World w) {
      if (w.isThundering() != (this == THUNDER)) {
         w.setThundering(this == THUNDER);
      }

      if (w.hasStorm() == (this == CLEAR)) {
         w.setStorm(this != CLEAR);
      }
   }

   static {
      Language.addListener(() -> {
         byName.clear();

         for (WeatherType t : values()) {
            t.names = Language.getList("weather." + t.name() + ".name");
            t.adjective = Language.get("weather." + t.name() + ".adjective");

            for (String name : t.names) {
               byName.put(name, t);
            }
         }
      });
   }
}
