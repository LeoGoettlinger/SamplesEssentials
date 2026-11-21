package com.viaversion.viaversion.libs.mcstructs.text.components.nbt;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import lombok.Generated;

public class BlockNbtSource implements NbtDataSource {
   private String pos;

   public BlockNbtSource(String pos) {
      this.pos = pos;
   }

   public String getPos() {
      return this.pos;
   }

   public BlockNbtSource setPos(String pos) {
      this.pos = pos;
      return this;
   }

   public BlockNbtSource copy() {
      return new BlockNbtSource(this.pos);
   }

   @Override
   public String toString() {
      return ToString.of(this).add("pos", this.pos).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BlockNbtSource)) {
         return false;
      } else {
         BlockNbtSource other = (BlockNbtSource)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$pos = this.getPos();
            Object other$pos = other.getPos();
            return this$pos == null ? other$pos == null : this$pos.equals(other$pos);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof BlockNbtSource;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $pos = this.getPos();
      return result * 59 + ($pos == null ? 43 : $pos.hashCode());
   }
}
