package su.nightexpress.nightcore.util.sound;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.common.NightKey;

public class CustomSound extends AbstractSound {
   private final String sound;

   public CustomSound(@NonNull String sound, float volume, float pitch) {
      super(volume, pitch);
      this.sound = NightKey.parse(sound).map(NightKey::toString).orElse("null");
   }

   @NonNull
   public static CustomSound of(@NonNull String sound) {
      return of(sound, 0.8F);
   }

   @NonNull
   public static CustomSound of(@NonNull String sound, float volume) {
      return of(sound, volume, 1.0F);
   }

   @NonNull
   public static CustomSound of(@NonNull String sound, float volume, float pitch) {
      return new CustomSound(sound, volume, pitch);
   }

   @Override
   public void play(@NonNull Player player) {
      if (!this.isSilent()) {
         player.playSound(player, this.sound, this.volume, this.pitch);
      }
   }

   @Override
   public void play(@NonNull Location location) {
      if (!this.isSilent()) {
         World world = location.getWorld();
         if (world != null) {
            world.playSound(location, this.sound, this.volume, this.pitch);
         }
      }
   }

   @NonNull
   @Override
   public String getName() {
      return this.sound;
   }
}
