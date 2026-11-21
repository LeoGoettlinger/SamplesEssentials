package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import javax.annotation.Nullable;
import lombok.Generated;

public class CustomClickEvent extends ClickEvent {
   private Identifier id;
   @Nullable
   private Tag payload;

   public CustomClickEvent(Identifier id, @Nullable Tag payload) {
      super(ClickEventAction.CUSTOM);
      this.id = id;
      this.payload = payload;
   }

   @Override
   public String toString() {
      return "";
   }

   @Generated
   public Identifier getId() {
      return this.id;
   }

   @Nullable
   @Generated
   public Tag getPayload() {
      return this.payload;
   }

   @Generated
   public void setId(Identifier id) {
      this.id = id;
   }

   @Generated
   public void setPayload(@Nullable Tag payload) {
      this.payload = payload;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof CustomClickEvent)) {
         return false;
      } else {
         CustomClickEvent other = (CustomClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$id = this.getId();
            Object other$id = other.getId();
            if (this$id == null ? other$id == null : this$id.equals(other$id)) {
               Object this$payload = this.getPayload();
               Object other$payload = other.getPayload();
               return this$payload == null ? other$payload == null : this$payload.equals(other$payload);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof CustomClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $payload = this.getPayload();
      return result * 59 + ($payload == null ? 43 : $payload.hashCode());
   }
}
