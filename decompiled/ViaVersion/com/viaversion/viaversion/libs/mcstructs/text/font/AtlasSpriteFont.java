package com.viaversion.viaversion.libs.mcstructs.text.font;

import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import lombok.Generated;

public class AtlasSpriteFont implements FontDescription {
   private Identifier atlasId;
   private Identifier spriteId;

   @Override
   public String toString() {
      return ToString.of(this).add("atlasId", this.atlasId).add("spriteId", this.spriteId).toString();
   }

   @Generated
   public Identifier getAtlasId() {
      return this.atlasId;
   }

   @Generated
   public Identifier getSpriteId() {
      return this.spriteId;
   }

   @Generated
   public void setAtlasId(Identifier atlasId) {
      this.atlasId = atlasId;
   }

   @Generated
   public void setSpriteId(Identifier spriteId) {
      this.spriteId = spriteId;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof AtlasSpriteFont)) {
         return false;
      } else {
         AtlasSpriteFont other = (AtlasSpriteFont)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$atlasId = this.getAtlasId();
            Object other$atlasId = other.getAtlasId();
            if (this$atlasId == null ? other$atlasId == null : this$atlasId.equals(other$atlasId)) {
               Object this$spriteId = this.getSpriteId();
               Object other$spriteId = other.getSpriteId();
               return this$spriteId == null ? other$spriteId == null : this$spriteId.equals(other$spriteId);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof AtlasSpriteFont;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $atlasId = this.getAtlasId();
      result = result * 59 + ($atlasId == null ? 43 : $atlasId.hashCode());
      Object $spriteId = this.getSpriteId();
      return result * 59 + ($spriteId == null ? 43 : $spriteId.hashCode());
   }

   @Generated
   public AtlasSpriteFont() {
   }

   @Generated
   public AtlasSpriteFont(Identifier atlasId, Identifier spriteId) {
      this.atlasId = atlasId;
      this.spriteId = spriteId;
   }
}
