package ac.grim.grimac.utils.data;

import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import lombok.Generated;

public final class RotationData {
   private final float yaw;
   private final float pitch;
   private final boolean relativeYaw;
   private final boolean relativePitch;
   private final int transaction;
   private boolean accepted;

   @Contract(
      mutates = "this"
   )
   public void accept() {
      this.accepted = true;
   }

   public boolean allowRotation(float yaw, float pitch) {
      return (this.relativeYaw || this.yaw == yaw) && (this.relativePitch || this.pitch == pitch);
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public boolean isRelativeYaw() {
      return this.relativeYaw;
   }

   @Generated
   public boolean isRelativePitch() {
      return this.relativePitch;
   }

   @Generated
   public int getTransaction() {
      return this.transaction;
   }

   @Generated
   public boolean isAccepted() {
      return this.accepted;
   }

   @Generated
   public RotationData(float yaw, float pitch, boolean relativeYaw, boolean relativePitch, int transaction) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.relativeYaw = relativeYaw;
      this.relativePitch = relativePitch;
      this.transaction = transaction;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RotationData other)) {
         return false;
      } else if (Float.compare(this.getYaw(), other.getYaw()) != 0) {
         return false;
      } else if (Float.compare(this.getPitch(), other.getPitch()) != 0) {
         return false;
      } else if (this.isRelativeYaw() != other.isRelativeYaw()) {
         return false;
      } else if (this.isRelativePitch() != other.isRelativePitch()) {
         return false;
      } else {
         return this.getTransaction() != other.getTransaction() ? false : this.isAccepted() == other.isAccepted();
      }
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getYaw());
      result = result * 59 + Float.floatToIntBits(this.getPitch());
      result = result * 59 + (this.isRelativeYaw() ? 79 : 97);
      result = result * 59 + (this.isRelativePitch() ? 79 : 97);
      result = result * 59 + this.getTransaction();
      return result * 59 + (this.isAccepted() ? 79 : 97);
   }

   @Generated
   @Override
   public String toString() {
      return "RotationData(yaw="
         + this.getYaw()
         + ", pitch="
         + this.getPitch()
         + ", relativeYaw="
         + this.isRelativeYaw()
         + ", relativePitch="
         + this.isRelativePitch()
         + ", transaction="
         + this.getTransaction()
         + ", accepted="
         + this.isAccepted()
         + ")";
   }
}
