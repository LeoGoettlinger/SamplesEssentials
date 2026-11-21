package com.viaversion.viaversion.libs.mcstructs.text.font;

import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import lombok.Generated;

public class ResourceFont implements FontDescription {
   public Identifier id;

   @Override
   public String toString() {
      return ToString.of(this).add("id", this.id).toString();
   }

   @Generated
   public Identifier getId() {
      return this.id;
   }

   @Generated
   public void setId(Identifier id) {
      this.id = id;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ResourceFont)) {
         return false;
      } else {
         ResourceFont other = (ResourceFont)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$id = this.getId();
            Object other$id = other.getId();
            return this$id == null ? other$id == null : this$id.equals(other$id);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ResourceFont;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      return result * 59 + ($id == null ? 43 : $id.hashCode());
   }

   @Generated
   public ResourceFont() {
   }

   @Generated
   public ResourceFont(Identifier id) {
      this.id = id;
   }
}
