package com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl;

import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Generated;

public class EntityHoverEvent extends HoverEvent {
   private EntityHoverEvent.DataHolder data;

   public EntityHoverEvent(EntityHoverEvent.DataHolder data) {
      super(HoverEventAction.SHOW_ENTITY);
      this.data = data;
   }

   public EntityHoverEvent(TextComponent legacyData) {
      super(HoverEventAction.SHOW_ENTITY);
      this.data = new EntityHoverEvent.LegacyHolder(legacyData);
   }

   public EntityHoverEvent(Identifier type, UUID uuid, @Nullable TextComponent name) {
      super(HoverEventAction.SHOW_ENTITY);
      this.data = new EntityHoverEvent.ModernHolder(type, uuid, name);
   }

   public EntityHoverEvent.DataHolder getData() {
      return this.data;
   }

   public EntityHoverEvent setData(EntityHoverEvent.DataHolder data) {
      this.data = data;
      return this;
   }

   public boolean isLegacy() {
      return this.data instanceof EntityHoverEvent.LegacyHolder;
   }

   public boolean isModern() {
      return this.data instanceof EntityHoverEvent.ModernHolder;
   }

   public EntityHoverEvent.LegacyHolder asLegacy() {
      if (this.data instanceof EntityHoverEvent.LegacyHolder) {
         return (EntityHoverEvent.LegacyHolder)this.data;
      } else {
         throw new UnsupportedOperationException("Data holder is not a legacy string holder: " + this.data);
      }
   }

   public EntityHoverEvent.ModernHolder asModern() {
      if (this.data instanceof EntityHoverEvent.ModernHolder) {
         return (EntityHoverEvent.ModernHolder)this.data;
      } else {
         throw new UnsupportedOperationException("Data holder is not a modern holder: " + this.data);
      }
   }

   public EntityHoverEvent setLegacyData(TextComponent data) {
      this.data = new EntityHoverEvent.LegacyHolder(data);
      return this;
   }

   public EntityHoverEvent setModernData(Identifier type, UUID uuid, @Nullable TextComponent name) {
      this.data = new EntityHoverEvent.ModernHolder(type, uuid, name);
      return this;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("data", this.data).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof EntityHoverEvent)) {
         return false;
      } else {
         EntityHoverEvent other = (EntityHoverEvent)o;
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
      return other instanceof EntityHoverEvent;
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

   public static class LegacyHolder implements EntityHoverEvent.DataHolder {
      private TextComponent data;

      @Override
      public String toString() {
         return ToString.of(this).add("data", this.data).toString();
      }

      @Generated
      public TextComponent getData() {
         return this.data;
      }

      @Generated
      public void setData(TextComponent data) {
         this.data = data;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof EntityHoverEvent.LegacyHolder)) {
            return false;
         } else {
            EntityHoverEvent.LegacyHolder other = (EntityHoverEvent.LegacyHolder)o;
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
         return other instanceof EntityHoverEvent.LegacyHolder;
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
      public LegacyHolder(TextComponent data) {
         this.data = data;
      }
   }

   public static class ModernHolder implements EntityHoverEvent.DataHolder {
      private Identifier type;
      private UUID uuid;
      @Nullable
      private TextComponent name;

      @Override
      public String toString() {
         return ToString.of(this).add("type", this.type).add("uuid", this.uuid).add("name", this.name, Objects::nonNull).toString();
      }

      @Generated
      public Identifier getType() {
         return this.type;
      }

      @Generated
      public UUID getUuid() {
         return this.uuid;
      }

      @Nullable
      @Generated
      public TextComponent getName() {
         return this.name;
      }

      @Generated
      public void setType(Identifier type) {
         this.type = type;
      }

      @Generated
      public void setUuid(UUID uuid) {
         this.uuid = uuid;
      }

      @Generated
      public void setName(@Nullable TextComponent name) {
         this.name = name;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof EntityHoverEvent.ModernHolder)) {
            return false;
         } else {
            EntityHoverEvent.ModernHolder other = (EntityHoverEvent.ModernHolder)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$type = this.getType();
               Object other$type = other.getType();
               if (this$type == null ? other$type == null : this$type.equals(other$type)) {
                  Object this$uuid = this.getUuid();
                  Object other$uuid = other.getUuid();
                  if (this$uuid == null ? other$uuid == null : this$uuid.equals(other$uuid)) {
                     Object this$name = this.getName();
                     Object other$name = other.getName();
                     return this$name == null ? other$name == null : this$name.equals(other$name);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof EntityHoverEvent.ModernHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $type = this.getType();
         result = result * 59 + ($type == null ? 43 : $type.hashCode());
         Object $uuid = this.getUuid();
         result = result * 59 + ($uuid == null ? 43 : $uuid.hashCode());
         Object $name = this.getName();
         return result * 59 + ($name == null ? 43 : $name.hashCode());
      }

      @Generated
      public ModernHolder(Identifier type, UUID uuid, @Nullable TextComponent name) {
         this.type = type;
         this.uuid = uuid;
         this.name = name;
      }
   }
}
