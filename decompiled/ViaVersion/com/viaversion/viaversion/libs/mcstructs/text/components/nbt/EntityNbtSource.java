package com.viaversion.viaversion.libs.mcstructs.text.components.nbt;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import lombok.Generated;

public class EntityNbtSource implements NbtDataSource {
   private String selector;

   public EntityNbtSource(String selector) {
      this.selector = selector;
   }

   public String getSelector() {
      return this.selector;
   }

   public EntityNbtSource setSelector(String selector) {
      this.selector = selector;
      return this;
   }

   public EntityNbtSource copy() {
      return new EntityNbtSource(this.selector);
   }

   @Override
   public String toString() {
      return ToString.of(this).add("selector", this.selector).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof EntityNbtSource)) {
         return false;
      } else {
         EntityNbtSource other = (EntityNbtSource)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$selector = this.getSelector();
            Object other$selector = other.getSelector();
            return this$selector == null ? other$selector == null : this$selector.equals(other$selector);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof EntityNbtSource;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $selector = this.getSelector();
      return result * 59 + ($selector == null ? 43 : $selector.hashCode());
   }
}
