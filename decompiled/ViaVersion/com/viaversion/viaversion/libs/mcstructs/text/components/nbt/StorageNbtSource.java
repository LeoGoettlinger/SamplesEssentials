package com.viaversion.viaversion.libs.mcstructs.text.components.nbt;

import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import lombok.Generated;

public class StorageNbtSource implements NbtDataSource {
   private Identifier id;

   public StorageNbtSource(Identifier id) {
      this.id = id;
   }

   public Identifier getId() {
      return this.id;
   }

   public StorageNbtSource setId(Identifier id) {
      this.id = id;
      return this;
   }

   public StorageNbtSource copy() {
      return new StorageNbtSource(this.id);
   }

   @Override
   public String toString() {
      return ToString.of(this).add("id", this.id).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof StorageNbtSource)) {
         return false;
      } else {
         StorageNbtSource other = (StorageNbtSource)o;
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
      return other instanceof StorageNbtSource;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      return result * 59 + ($id == null ? 43 : $id.hashCode());
   }
}
