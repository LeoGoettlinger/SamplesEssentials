package com.viaversion.viaversion.libs.mcstructs.text.font;

import com.viaversion.viaversion.libs.mcstructs.converter.SerializedData;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import lombok.Generated;

public class PlayerSpriteFont implements FontDescription {
   private SerializedData<?> profile;
   private boolean hat;

   @Override
   public String toString() {
      return ToString.of(this).add("profile", this.profile).add("hat", this.hat).toString();
   }

   @Generated
   public SerializedData<?> getProfile() {
      return this.profile;
   }

   @Generated
   public boolean isHat() {
      return this.hat;
   }

   @Generated
   public void setProfile(SerializedData<?> profile) {
      this.profile = profile;
   }

   @Generated
   public void setHat(boolean hat) {
      this.hat = hat;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof PlayerSpriteFont)) {
         return false;
      } else {
         PlayerSpriteFont other = (PlayerSpriteFont)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.isHat() != other.isHat()) {
            return false;
         } else {
            Object this$profile = this.getProfile();
            Object other$profile = other.getProfile();
            return this$profile == null ? other$profile == null : this$profile.equals(other$profile);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof PlayerSpriteFont;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + (this.isHat() ? 79 : 97);
      Object $profile = this.getProfile();
      return result * 59 + ($profile == null ? 43 : $profile.hashCode());
   }

   @Generated
   public PlayerSpriteFont() {
   }

   @Generated
   public PlayerSpriteFont(SerializedData<?> profile, boolean hat) {
      this.profile = profile;
      this.hat = hat;
   }
}
