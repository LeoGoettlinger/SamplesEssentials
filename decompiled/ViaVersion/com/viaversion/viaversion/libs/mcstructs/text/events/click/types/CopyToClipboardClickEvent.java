package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import lombok.Generated;

public class CopyToClipboardClickEvent extends ClickEvent {
   private String value;

   public CopyToClipboardClickEvent(String value) {
      super(ClickEventAction.COPY_TO_CLIPBOARD);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("value", this.value).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof CopyToClipboardClickEvent)) {
         return false;
      } else {
         CopyToClipboardClickEvent other = (CopyToClipboardClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$value = this.getValue();
            Object other$value = other.getValue();
            return this$value == null ? other$value == null : this$value.equals(other$value);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof CopyToClipboardClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $value = this.getValue();
      return result * 59 + ($value == null ? 43 : $value.hashCode());
   }
}
