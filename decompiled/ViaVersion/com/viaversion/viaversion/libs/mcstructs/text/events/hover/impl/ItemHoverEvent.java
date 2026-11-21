package com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;

public class ItemHoverEvent extends HoverEvent {
   private ItemHoverEvent.DataHolder data;

   public ItemHoverEvent(ItemHoverEvent.DataHolder data) {
      super(HoverEventAction.SHOW_ITEM);
      this.data = data;
   }

   public ItemHoverEvent(String legacyData) {
      super(HoverEventAction.SHOW_ITEM);
      this.data = new ItemHoverEvent.LegacyHolder(legacyData);
   }

   public ItemHoverEvent(Identifier id, int count, @Nullable CompoundTag tag) {
      super(HoverEventAction.SHOW_ITEM);
      this.data = new ItemHoverEvent.ModernHolder(id, count, tag);
   }

   public ItemHoverEvent.DataHolder getData() {
      return this.data;
   }

   public ItemHoverEvent setData(String data) {
      this.data = new ItemHoverEvent.LegacyHolder(data);
      return this;
   }

   public boolean isLegacy() {
      return this.data instanceof ItemHoverEvent.LegacyHolder;
   }

   public boolean isModern() {
      return this.data instanceof ItemHoverEvent.ModernHolder;
   }

   public ItemHoverEvent.LegacyHolder asLegacy() {
      if (this.data instanceof ItemHoverEvent.LegacyHolder) {
         return (ItemHoverEvent.LegacyHolder)this.data;
      } else {
         throw new UnsupportedOperationException("Data holder is not a legacy raw holder: " + this.data);
      }
   }

   public ItemHoverEvent.ModernHolder asModern() {
      if (this.data instanceof ItemHoverEvent.ModernHolder) {
         return (ItemHoverEvent.ModernHolder)this.data;
      } else {
         throw new UnsupportedOperationException("Data holder is not a modern holder: " + this.data);
      }
   }

   public ItemHoverEvent setLegacyData(String data) {
      this.data = new ItemHoverEvent.LegacyHolder(data);
      return this;
   }

   public ItemHoverEvent setModernData(Identifier id, int count, @Nullable CompoundTag tag) {
      this.data = new ItemHoverEvent.ModernHolder(id, count, tag);
      return this;
   }

   @Override
   public String toString() {
      return ToString.of(this).put("action", this.action).put("data", this.data).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ItemHoverEvent)) {
         return false;
      } else {
         ItemHoverEvent other = (ItemHoverEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$data = this.getData();
            Object other$data = other.getData();
            return this$data == null ? other$data == null : this$data.equals(other$data);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ItemHoverEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $data = this.getData();
      return result * 59 + ($data == null ? 43 : $data.hashCode());
   }

   public interface DataHolder {
   }

   public static class LegacyHolder implements ItemHoverEvent.DataHolder {
      private String data;

      @Override
      public String toString() {
         return ToString.of(this).put("data", this.data).toString();
      }

      @Generated
      public String getData() {
         return this.data;
      }

      @Generated
      public void setData(String data) {
         this.data = data;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ItemHoverEvent.LegacyHolder)) {
            return false;
         } else {
            ItemHoverEvent.LegacyHolder other = (ItemHoverEvent.LegacyHolder)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$data = this.getData();
               Object other$data = other.getData();
               return this$data == null ? other$data == null : this$data.equals(other$data);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ItemHoverEvent.LegacyHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $data = this.getData();
         return result * 59 + ($data == null ? 43 : $data.hashCode());
      }

      @Generated
      public LegacyHolder(String data) {
         this.data = data;
      }
   }

   public static class ModernHolder implements ItemHoverEvent.DataHolder {
      private Identifier id;
      private int count;
      @Nullable
      private CompoundTag tag;

      @Override
      public String toString() {
         return ToString.of(this).put("id", this.id).put("count", this.count, c -> c != 1).put("tag", this.tag, Objects::nonNull).toString();
      }

      @Generated
      public Identifier getId() {
         return this.id;
      }

      @Generated
      public int getCount() {
         return this.count;
      }

      @Nullable
      @Generated
      public CompoundTag getTag() {
         return this.tag;
      }

      @Generated
      public void setId(Identifier id) {
         this.id = id;
      }

      @Generated
      public void setCount(int count) {
         this.count = count;
      }

      @Generated
      public void setTag(@Nullable CompoundTag tag) {
         this.tag = tag;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ItemHoverEvent.ModernHolder)) {
            return false;
         } else {
            ItemHoverEvent.ModernHolder other = (ItemHoverEvent.ModernHolder)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.getCount() != other.getCount()) {
               return false;
            } else {
               Object this$id = this.getId();
               Object other$id = other.getId();
               if (this$id == null ? other$id == null : this$id.equals(other$id)) {
                  Object this$tag = this.getTag();
                  Object other$tag = other.getTag();
                  return this$tag == null ? other$tag == null : this$tag.equals(other$tag);
               } else {
                  return false;
               }
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ItemHoverEvent.ModernHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getCount();
         Object $id = this.getId();
         result = result * 59 + ($id == null ? 43 : $id.hashCode());
         Object $tag = this.getTag();
         return result * 59 + ($tag == null ? 43 : $tag.hashCode());
      }

      @Generated
      public ModernHolder(Identifier id, int count, @Nullable CompoundTag tag) {
         this.id = id;
         this.count = count;
         this.tag = tag;
      }
   }
}
