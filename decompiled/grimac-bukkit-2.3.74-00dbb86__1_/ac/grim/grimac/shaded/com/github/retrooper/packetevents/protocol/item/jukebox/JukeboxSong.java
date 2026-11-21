package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.jukebox;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class JukeboxSong extends AbstractMappedEntity implements IJukeboxSong {
   private Sound sound;
   private Component description;
   private float lengthInSeconds;
   private int comparatorOutput;

   public JukeboxSong(Sound sound, Component description, float lengthInSeconds, int comparatorOutput) {
      this(null, sound, description, lengthInSeconds, comparatorOutput);
   }

   @ApiStatus.Internal
   public JukeboxSong(@Nullable TypesBuilderData data, Sound sound, Component description, float lengthInSeconds, int comparatorOutput) {
      super(data);
      this.sound = sound;
      this.description = description;
      this.lengthInSeconds = lengthInSeconds;
      this.comparatorOutput = comparatorOutput;
   }

   @Deprecated
   public static JukeboxSong read(PacketWrapper<?> wrapper) {
      return (JukeboxSong)IJukeboxSong.read(wrapper);
   }

   @Deprecated
   public static void write(PacketWrapper<?> wrapper, JukeboxSong song) {
      IJukeboxSong.write(wrapper, song);
   }

   public IJukeboxSong copy(@Nullable TypesBuilderData newData) {
      return new JukeboxSong(newData, this.sound, this.description, this.lengthInSeconds, this.comparatorOutput);
   }

   @Override
   public Sound getSound() {
      return this.sound;
   }

   @Deprecated
   public void setSound(Sound sound) {
      this.sound = sound;
   }

   @Override
   public Component getDescription() {
      return this.description;
   }

   @Deprecated
   public void setDescription(Component description) {
      this.description = description;
   }

   @Override
   public float getLengthInSeconds() {
      return this.lengthInSeconds;
   }

   @Deprecated
   public void setLengthInSeconds(float lengthInSeconds) {
      this.lengthInSeconds = lengthInSeconds;
   }

   @Override
   public int getComparatorOutput() {
      return this.comparatorOutput;
   }

   @Deprecated
   public void setComparatorOutput(int comparatorOutput) {
      this.comparatorOutput = comparatorOutput;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof JukeboxSong)) {
         return false;
      } else {
         JukeboxSong that = (JukeboxSong)obj;
         if (Float.compare(that.lengthInSeconds, this.lengthInSeconds) != 0) {
            return false;
         } else if (this.comparatorOutput != that.comparatorOutput) {
            return false;
         } else {
            return !this.sound.equals(that.sound) ? false : this.description.equals(that.description);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.sound, this.description, this.lengthInSeconds, this.comparatorOutput);
   }

   @Override
   public String toString() {
      return "JukeboxSong{sound="
         + this.sound
         + ", description="
         + this.description
         + ", lengthInSeconds="
         + this.lengthInSeconds
         + ", comparatorOutput="
         + this.comparatorOutput
         + '}';
   }
}
