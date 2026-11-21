package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import lombok.Generated;

public class SuggestCommandClickEvent extends ClickEvent {
   private String command;

   public SuggestCommandClickEvent(String command) {
      super(ClickEventAction.SUGGEST_COMMAND);
      this.command = command;
   }

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String command) {
      this.command = command;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("command", this.command).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SuggestCommandClickEvent)) {
         return false;
      } else {
         SuggestCommandClickEvent other = (SuggestCommandClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$command = this.getCommand();
            Object other$command = other.getCommand();
            return this$command == null ? other$command == null : this$command.equals(other$command);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof SuggestCommandClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $command = this.getCommand();
      return result * 59 + ($command == null ? 43 : $command.hashCode());
   }
}
