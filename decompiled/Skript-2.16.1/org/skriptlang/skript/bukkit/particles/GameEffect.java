package org.skriptlang.skript.bukkit.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.Locale;
import java.util.Map.Entry;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameEffect {
   private static final GameEffect.GameEffectParser ENUM_PARSER = new GameEffect.GameEffectParser();
   private final Effect effect;
   @Nullable
   private Object data;

   public GameEffect(Effect effect) {
      this.effect = effect;
   }

   public static GameEffect parse(String input) {
      Effect effect = ENUM_PARSER.parse(input.toLowerCase(Locale.ENGLISH), ParseContext.DEFAULT);
      if (effect == null) {
         return null;
      } else if (effect.getData() != null) {
         Skript.error("The effect " + Classes.toString(effect) + " requires data and cannot be parsed directly. Use the Game Effect expression instead.");
         return null;
      } else {
         return new GameEffect(effect);
      }
   }

   public Effect getEffect() {
      return this.effect;
   }

   @Nullable
   public Object getData() {
      return this.data;
   }

   public boolean setData(Object data) {
      if (this.effect.getData() != null && this.effect.getData().isInstance(data)) {
         this.data = data;
         return true;
      } else if (this.effect == Effect.ELECTRIC_SPARK && data == null) {
         this.data = null;
         return true;
      } else {
         return false;
      }
   }

   public void draw(@NotNull Location location, @Nullable Number radius) {
      if (this.effect.getData() == null || this.data != null) {
         World world = location.getWorld();
         if (world != null) {
            if (radius == null) {
               location.getWorld().playEffect(location, this.effect, this.data);
            } else {
               location.getWorld().playEffect(location, this.effect, this.data, radius.intValue());
            }
         }
      }
   }

   public void drawForPlayer(Location location, @NotNull Player player) {
      player.playEffect(location, this.effect, this.data);
   }

   public String toString(int flags) {
      return ENUM_PARSER.toString(this.getEffect(), flags);
   }

   @Override
   public String toString() {
      return this.toString(0);
   }

   public static String[] getAllNamesWithoutData() {
      return ENUM_PARSER.getPatternsWithoutData();
   }

   private static class GameEffectParser extends EnumParser<Effect> {
      public GameEffectParser() {
         super(Effect.class, "game effect");
      }

      @NotNull
      public String[] getPatternsWithoutData() {
         return this.parseMap.entrySet().stream().filter(entry -> {
            Effect effect = entry.getValue();
            return effect.getData() == null;
         }).map(Entry::getKey).toArray(String[]::new);
      }
   }
}
