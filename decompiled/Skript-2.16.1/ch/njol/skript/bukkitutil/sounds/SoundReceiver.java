package ch.njol.skript.bukkitutil.sounds;

import ch.njol.skript.Skript;
import java.util.Locale;
import java.util.OptionalLong;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface SoundReceiver {
   boolean ADVENTURE_API = Skript.classExists("net.kyori.adventure.sound.Sound$Builder") && Skript.methodExists(SoundCategory.class, "soundSource");
   boolean SPIGOT_SOUND_SEED = Skript.methodExists(
      Player.class, "playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class, long.class
   );
   boolean ENTITY_EMITTER_SOUND = Skript.methodExists(Player.class, "playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class);
   boolean ENTITY_EMITTER_STRING = Skript.methodExists(Player.class, "playSound", Entity.class, String.class, SoundCategory.class, float.class, float.class);

   void playSound(Location var1, NamespacedKey var2, SoundCategory var3, float var4, float var5, OptionalLong var6);

   void playSound(Entity var1, NamespacedKey var2, SoundCategory var3, float var4, float var5, OptionalLong var6);

   static SoundReceiver of(Player player) {
      return new SoundReceiver.PlayerSoundReceiver(player);
   }

   static SoundReceiver of(World world) {
      return new SoundReceiver.WorldSoundReceiver(world);
   }

   public static class PlayerSoundReceiver implements SoundReceiver {
      private final Player player;

      protected PlayerSoundReceiver(Player player) {
         this.player = player;
      }

      @Override
      public void playSound(Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
         if (ADVENTURE_API) {
            AdventureSoundUtils.playSound(this.player, location, sound, category, volume, pitch, seed);
         } else if (SPIGOT_SOUND_SEED && !seed.isEmpty()) {
            this.player.playSound(location, sound.getKey(), category, volume, pitch, seed.getAsLong());
         } else {
            this.player.playSound(location, sound.getKey(), category, volume, pitch);
         }
      }

      private void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch) {
         if (ENTITY_EMITTER_STRING) {
            this.player.playSound(entity, sound, category, volume, pitch);
         } else if (ENTITY_EMITTER_SOUND) {
            Sound enumSound;
            try {
               enumSound = Sound.valueOf(sound.replace('.', '_').toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException var8) {
               return;
            }

            this.player.playSound(entity, enumSound, category, volume, pitch);
         } else {
            this.player.playSound(entity.getLocation(), sound, category, volume, pitch);
         }
      }

      @Override
      public void playSound(Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
         if (ADVENTURE_API) {
            AdventureSoundUtils.playSound(this.player, entity, sound, category, volume, pitch, seed);
         } else if (SPIGOT_SOUND_SEED && !seed.isEmpty()) {
            this.player.playSound(entity, sound.getKey(), category, volume, pitch, seed.getAsLong());
         } else {
            this.playSound(entity, sound.getKey(), category, volume, pitch);
         }
      }
   }

   public static class WorldSoundReceiver implements SoundReceiver {
      private final World world;

      protected WorldSoundReceiver(World world) {
         this.world = world;
      }

      @Override
      public void playSound(Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
         if (ADVENTURE_API) {
            AdventureSoundUtils.playSound(this.world, location, sound, category, volume, pitch, seed);
         } else if (SPIGOT_SOUND_SEED && !seed.isEmpty()) {
            this.world.playSound(location, sound.getKey(), category, volume, pitch, seed.getAsLong());
         } else {
            this.world.playSound(location, sound.getKey(), category, volume, pitch);
         }
      }

      private void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch) {
         if (ENTITY_EMITTER_STRING) {
            this.world.playSound(entity, sound, category, volume, pitch);
         } else if (ENTITY_EMITTER_SOUND) {
            Sound enumSound;
            try {
               enumSound = Sound.valueOf(sound.replace('.', '_').toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException var8) {
               return;
            }

            this.world.playSound(entity, enumSound, category, volume, pitch);
         } else {
            this.world.playSound(entity.getLocation(), sound, category, volume, pitch);
         }
      }

      @Override
      public void playSound(Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
         if (ADVENTURE_API) {
            AdventureSoundUtils.playSound(this.world, entity, sound, category, volume, pitch, seed);
         } else if (SPIGOT_SOUND_SEED && !seed.isEmpty()) {
            this.world.playSound(entity, sound.getKey(), category, volume, pitch, seed.getAsLong());
         } else {
            this.playSound(entity, sound.getKey(), category, volume, pitch);
         }
      }
   }
}
