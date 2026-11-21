package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import lombok.Generated;

public class OpenFileClickEvent extends ClickEvent {
   private String path;

   public OpenFileClickEvent(String path) {
      super(ClickEventAction.OPEN_FILE);
      this.path = path;
   }

   public String getPath() {
      return this.path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("path", this.path).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof OpenFileClickEvent)) {
         return false;
      } else {
         OpenFileClickEvent other = (OpenFileClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$path = this.getPath();
            Object other$path = other.getPath();
            return this$path == null ? other$path == null : this$path.equals(other$path);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof OpenFileClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $path = this.getPath();
      return result * 59 + ($path == null ? 43 : $path.hashCode());
   }
}
