package github.nighter.smartspawner.api.gui;

import java.util.Objects;
import lombok.Generated;

public class GuiButtonSoundData {
   private final String name;
   private final float volume;
   private final float pitch;

   public GuiButtonSoundData(String name, float volume, float pitch) {
      if (name == null || name.isBlank()) {
         throw new IllegalArgumentException("Sound name cannot be blank");
      } else if (!Float.isFinite(volume) || volume < 0.0F) {
         throw new IllegalArgumentException("Sound volume must be finite and non-negative");
      } else if (Float.isFinite(pitch) && !(pitch < 0.0F)) {
         this.name = name;
         this.volume = volume;
         this.pitch = pitch;
      } else {
         throw new IllegalArgumentException("Sound pitch must be finite and non-negative");
      }
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return !(object instanceof GuiButtonSoundData other)
            ? false
            : Float.compare(this.volume, other.volume) == 0 && Float.compare(this.pitch, other.pitch) == 0 && this.name.equals(other.name);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.volume, this.pitch);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public float getVolume() {
      return this.volume;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }
}
