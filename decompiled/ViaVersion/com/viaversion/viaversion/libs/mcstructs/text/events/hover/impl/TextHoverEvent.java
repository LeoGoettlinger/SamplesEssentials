package com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import lombok.Generated;

public class TextHoverEvent extends HoverEvent {
   private TextComponent text;

   public TextHoverEvent(TextComponent text) {
      super(HoverEventAction.SHOW_TEXT);
      this.text = text;
   }

   public TextComponent getText() {
      return this.text;
   }

   public TextHoverEvent setText(TextComponent text) {
      this.text = text;
      return this;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("text", this.text).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof TextHoverEvent)) {
         return false;
      } else {
         TextHoverEvent other = (TextHoverEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$text = this.getText();
            Object other$text = other.getText();
            return this$text == null ? other$text == null : this$text.equals(other$text);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof TextHoverEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $text = this.getText();
      return result * 59 + ($text == null ? 43 : $text.hashCode());
   }
}
