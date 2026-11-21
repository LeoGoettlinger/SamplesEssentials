package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.jetbrains.annotations.Nullable;

@Name("Weather")
@Description({"The weather of a world or player.", "Clearing or resetting the weather of a player will make the player's weather match the weather of the world.", "Clearing or resetting the weather of a world will make the weather clear."})
@Example.Examples({@Example("set weather to clear"), @Example("weather in \"world\" is rainy"), @Example("reset custom weather of player"), @Example("set weather of player to clear")})
@Since({"1.0"})
@Events({"weather change"})
public class ExprWeather extends PropertyExpression<Object, WeatherType> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.setExpr((Expression<? extends Object>)exprs[0]);
      return true;
   }

   @Nullable
   protected WeatherType[] get(Event event, Object[] source) {
      World eventWorld = event instanceof WeatherEvent weatherEvent ? weatherEvent.getWorld() : null;
      return this.get(
         source,
         object -> {
            if (object instanceof Player player) {
               return WeatherType.fromPlayer(player);
            } else if (!(object instanceof World world)) {
               return null;
            } else {
               return eventWorld == null
                     || !world.equals(eventWorld)
                     || this.getTime() < 0
                     || event instanceof Cancellable cancellable && cancellable.isCancelled()
                  ? WeatherType.fromWorld(world)
                  : WeatherType.fromEvent((WeatherEvent)event);
            }
         }
      );
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.DELETE && mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET
         ? null
         : CollectionUtils.array(WeatherType.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      WeatherType playerWeather = delta != null ? (WeatherType)delta[0] : null;
      WeatherType worldWeather = playerWeather != null ? playerWeather : WeatherType.CLEAR;
      World eventWorld = event instanceof WeatherEvent weatherEvent ? weatherEvent.getWorld() : null;

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof Player player) {
            if (playerWeather != null) {
               playerWeather.setWeather(player);
            } else {
               player.resetPlayerWeather();
            }
         } else if (object instanceof World world) {
            if (eventWorld == null || !world.equals(eventWorld) || this.getTime() < 0) {
               worldWeather.setWeather(world);
            } else if (event instanceof WeatherChangeEvent weatherChangeEvent) {
               if (weatherChangeEvent.toWeatherState() && worldWeather == WeatherType.CLEAR) {
                  weatherChangeEvent.setCancelled(true);
               } else if (!weatherChangeEvent.toWeatherState() && worldWeather == WeatherType.RAIN) {
                  eventWorld.setStorm(true);
               }

               if (eventWorld.isThundering() != (worldWeather == WeatherType.THUNDER)) {
                  eventWorld.setThundering(worldWeather == WeatherType.THUNDER);
               }
            } else if (event instanceof ThunderChangeEvent thunderChangeEvent) {
               if (thunderChangeEvent.toThunderState() && worldWeather != WeatherType.THUNDER) {
                  thunderChangeEvent.setCancelled(true);
               } else if (!thunderChangeEvent.toThunderState() && worldWeather == WeatherType.THUNDER) {
                  eventWorld.setThundering(true);
               }

               if (eventWorld.hasStorm() == (worldWeather == WeatherType.CLEAR)) {
                  eventWorld.setStorm(worldWeather != WeatherType.CLEAR);
               }
            }
         }
      }
   }

   @Override
   public Class<WeatherType> getReturnType() {
      return WeatherType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "weather of " + this.getExpr().toString(event, debug);
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, this.getExpr(), new Class[]{WeatherChangeEvent.class, ThunderChangeEvent.class});
   }

   static {
      Skript.registerExpression(
         ExprWeather.class,
         WeatherType.class,
         ExpressionType.PROPERTY,
         "[the] weather [(in|of) %players/worlds%]",
         "[the] (custom|client) weather [of %players%]",
         "%players/worlds%'[s] weather",
         "%players%'[s] (custom|client) weather"
      );
   }
}
