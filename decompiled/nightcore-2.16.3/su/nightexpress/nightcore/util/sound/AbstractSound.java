package su.nightexpress.nightcore.util.sound;

import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.wrap.NightSound;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.NumberUtil;

public abstract class AbstractSound implements NightSound, Writeable {
   protected static final String DELIMITER = ";";
   protected static final float MIN_PITCH = 0.5F;
   protected static final float MAX_PITCH = 2.0F;
   protected static final float MIN_VOLUME = 0.0F;
   protected static final float MAX_VOLUME = 1.0F;
   protected static final float DEFAULT_VOLUME = 0.8F;
   protected static final float DEFAULT_PITCH = 1.0F;
   protected final float volume;
   protected final float pitch;

   public AbstractSound(float volume, float pitch) {
      this.volume = NumberUtil.clamp(volume, 0.0F, 1.0F);
      this.pitch = NumberUtil.clamp(pitch, 0.5F, 2.0F);
   }

   @NonNull
   public static NightSound deserialize(@NonNull String from) {
      String[] split = from.split(";");
      String name = split[0];
      float volume = split.length >= 2 ? (float)NumberUtil.getDoubleAbs(split[1], 1.0) : 0.8F;
      float pitch = split.length >= 3 ? (float)NumberUtil.getDoubleAbs(split[2], 1.0) : 1.0F;
      Sound bukkit = BukkitThing.getSound(name);
      return (NightSound)(bukkit == null ? CustomSound.of(name, volume, pitch) : VanillaSound.of(bukkit, volume, pitch));
   }

   @NonNull
   @Override
   public String serialize() {
      return this.getName() + ";" + this.volume + ";" + this.pitch;
   }

   @Nullable
   public static NightSound read(@NonNull FileConfig config, @NonNull String path) {
      String raw = config.getString(path);
      return raw == null ? null : deserialize(raw);
   }

   @Override
   public void write(@NonNull FileConfig config, @NonNull String path) {
      config.set(path, this.serialize());
   }

   @Override
   public boolean isSilent() {
      return this.volume <= 0.0F;
   }

   @Override
   public float getVolume() {
      return this.volume;
   }

   @Override
   public float getPitch() {
      return this.pitch;
   }

   @Override
   public String toString() {
      return "AbstractSound{name=" + this.getName() + ", volume=" + this.volume + ", pitch=" + this.pitch + "}";
   }
}
