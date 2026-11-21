package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.bukkitutil.sounds.SoundReceiver;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Play Sound")
@Description({"Plays a sound at given location for everyone or just for given players, or plays a sound to specified players. Both Minecraft sound names and <a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> are supported. Playing resource pack sounds are supported too. The sound category is 'master' by default. ", "", "When running 1.19+, playing a sound from an entity directly will result in the sound coming from said entity, even while moving.", "If the sound is custom, a location emitter will follow the entity. Do note that pitch and volume ", "are reflected based on the entity, and Minecraft may not use the values from this syntax.", "", "Minecraft sometimes has a set of sounds under one sound ID that will randomly play. To counter this, you can directly state which seed to use.", "", "Please note that sound names can get changed in any Minecraft or Spigot version, or even removed from Minecraft itself."})
@Example.Examples({@Example("play sound \"block.note_block.pling\""), @Example("play sound \"entity.experience_orb.pickup\" with volume 0.5 to the player"), @Example("play sound \"custom.music.1\" in jukebox category at {speakerBlock}"), @Example("play sound \"BLOCK_AMETHYST_BLOCK_RESONATE\" with seed 1 on target entity for the player")})
@Since({"2.2-dev28, 2.4 (sound categories), 2.9 (sound seed & entity emitter)"})
public class EffPlaySound extends Effect {
   private static final boolean SPIGOT_SOUND_SEED = Skript.methodExists(
      Player.class, "playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class, long.class
   );
   private static final boolean HAS_SEED = SoundReceiver.ADVENTURE_API || SPIGOT_SOUND_SEED;
   private static final boolean ENTITY_EMITTER_SOUND = Skript.methodExists(
      Player.class, "playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class
   );
   private static final boolean ENTITY_EMITTER_STRING = Skript.methodExists(
      Player.class, "playSound", Entity.class, String.class, SoundCategory.class, float.class, float.class
   );
   private static final boolean ENTITY_EMITTER = ENTITY_EMITTER_SOUND || ENTITY_EMITTER_STRING;
   private Expression<String> sounds;
   @Nullable
   private Expression<SoundCategory> category;
   @Nullable
   private Expression<Player> players;
   @Nullable
   private Expression<Number> volume;
   @Nullable
   private Expression<Number> pitch;
   @Nullable
   private Expression<Number> seed;
   @Nullable
   private Expression<?> emitters;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.sounds = (Expression<String>)exprs[0];
      int index = 1;
      if (HAS_SEED) {
         this.seed = (Expression<Number>)exprs[index++];
      }

      this.category = (Expression<SoundCategory>)exprs[index++];
      this.volume = (Expression<Number>)exprs[index++];
      this.pitch = (Expression<Number>)exprs[index++];
      if (matchedPattern == 0) {
         this.emitters = exprs[index++];
         this.players = (Expression<Player>)exprs[index];
      } else {
         this.players = (Expression<Player>)exprs[index++];
         this.emitters = exprs[index];
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      OptionalLong seed = OptionalLong.empty();
      if (this.seed != null) {
         Number number = this.seed.getSingle(event);
         if (number != null) {
            seed = OptionalLong.of(number.longValue());
         }
      }

      SoundCategory category = this.category == null ? SoundCategory.MASTER : this.category.getOptionalSingle(event).orElse(SoundCategory.MASTER);
      float volume = this.volume == null ? 1.0F : this.volume.getOptionalSingle(event).orElse(1).floatValue();
      float pitch = this.pitch == null ? 1.0F : this.pitch.getOptionalSingle(event).orElse(1).floatValue();
      List<NamespacedKey> validSounds = new ArrayList<>();

      for (String sound : this.sounds.getArray(event)) {
         NamespacedKey key = SoundUtils.getKey(sound);
         if (key != null) {
            validSounds.add(key);
         }
      }

      if (!validSounds.isEmpty()) {
         if (this.players != null) {
            if (this.emitters == null) {
               for (Player player : this.players.getArray(event)) {
                  SoundReceiver receiver = SoundReceiver.of(player);
                  Location emitter = player.getLocation();

                  for (NamespacedKey soundx : validSounds) {
                     receiver.playSound(emitter, soundx, category, volume, pitch, seed);
                  }
               }
            } else {
               for (Player player : this.players.getArray(event)) {
                  SoundReceiver receiver = SoundReceiver.of(player);

                  for (Object emitter : this.emitters.getArray(event)) {
                     if (emitter instanceof Location) {
                        for (NamespacedKey soundx : validSounds) {
                           receiver.playSound((Location)emitter, soundx, category, volume, pitch, seed);
                        }
                     } else if (emitter instanceof Entity) {
                        for (NamespacedKey soundx : validSounds) {
                           receiver.playSound((Entity)emitter, soundx, category, volume, pitch, seed);
                        }
                     }
                  }
               }
            }
         } else if (this.emitters != null) {
            for (Object emitterx : this.emitters.getArray(event)) {
               if (ENTITY_EMITTER && emitterx instanceof Entity entity) {
                  SoundReceiver receiver = SoundReceiver.of(entity.getWorld());

                  for (NamespacedKey soundx : validSounds) {
                     receiver.playSound((Entity)emitterx, soundx, category, volume, pitch, seed);
                  }
               } else if (emitterx instanceof Location location) {
                  SoundReceiver receiver = SoundReceiver.of(location.getWorld());

                  for (NamespacedKey soundx : validSounds) {
                     receiver.playSound((Location)emitterx, soundx, category, volume, pitch, seed);
                  }
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      StringBuilder builder = new StringBuilder().append("play sound ").append(this.sounds.toString(event, debug));
      if (this.seed != null) {
         builder.append(" with seed ").append(this.seed.toString(event, debug));
      }

      if (this.category != null) {
         builder.append(" in ").append(this.category.toString(event, debug));
      }

      if (this.volume != null) {
         builder.append(" with volume ").append(this.volume.toString(event, debug));
      }

      if (this.pitch != null) {
         builder.append(" with pitch ").append(this.pitch.toString(event, debug));
      }

      if (this.emitters != null) {
         builder.append(" from ").append(this.emitters.toString(event, debug));
      }

      if (this.players != null) {
         builder.append(" to ").append(this.players.toString(event, debug));
      }

      return builder.toString();
   }

   static {
      String seedOption = HAS_SEED ? "[[with] seed %-number%] " : "";
      String emitterTypes = "locations";
      if (ENTITY_EMITTER) {
         emitterTypes = emitterTypes + "/entities";
      }

      Skript.registerEffect(
         EffPlaySound.class,
         "play sound[s] %strings% "
            + seedOption
            + "[(in|from) %-soundcategory%] [(at|with) volume %-number%] [(and|at|with) pitch %-number%] (at|on|from) %"
            + emitterTypes
            + "% [(to|for) %-players%]",
         "play sound[s] %strings% "
            + seedOption
            + "[(in|from) %-soundcategory%] [(at|with) volume %-number%] [(and|at|with) pitch %-number%] [(to|for) %players%] [(at|on|from) %-"
            + emitterTypes
            + "%]"
      );
   }
}
