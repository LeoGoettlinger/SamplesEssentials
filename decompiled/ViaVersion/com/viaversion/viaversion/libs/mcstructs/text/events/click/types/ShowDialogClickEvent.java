package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.converter.SerializedData;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import lombok.Generated;

public class ShowDialogClickEvent extends ClickEvent {
   private SerializedData<?> dialogData;

   public ShowDialogClickEvent(SerializedData<?> dialogData) {
      super(ClickEventAction.SHOW_DIALOG);
      this.dialogData = dialogData;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("dialogData", this.dialogData).toString();
   }

   @Generated
   public SerializedData<?> getDialogData() {
      return this.dialogData;
   }

   @Generated
   public void setDialogData(SerializedData<?> dialogData) {
      this.dialogData = dialogData;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ShowDialogClickEvent)) {
         return false;
      } else {
         ShowDialogClickEvent other = (ShowDialogClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$dialogData = this.getDialogData();
            Object other$dialogData = other.getDialogData();
            return this$dialogData == null ? other$dialogData == null : this$dialogData.equals(other$dialogData);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ShowDialogClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $dialogData = this.getDialogData();
      return result * 59 + ($dialogData == null ? 43 : $dialogData.hashCode());
   }
}
