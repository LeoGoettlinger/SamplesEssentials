package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import lombok.Generated;

public class StringComponent extends TextComponent {
   private String text;

   public StringComponent() {
      this("");
   }

   public StringComponent(String text) {
      this.text = text;
   }

   public String getText() {
      return this.text;
   }

   public StringComponent setText(String text) {
      this.text = text;
      return this;
   }

   @Override
   public String asSingleString() {
      return this.text;
   }

   @Override
   public TextComponent shallowCopy() {
      return new StringComponent(this.text).setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .add("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .add("style", this.getStyle(), style -> !style.isEmpty())
         .add("text", this.text)
         .toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof StringComponent)) {
         return false;
      } else {
         StringComponent other = (StringComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$text = this.getText();
            Object other$text = other.getText();
            return this$text == null ? other$text == null : this$text.equals(other$text);
         }
      }
   }

   @Generated
   @Override
   protected boolean canEqual(Object other) {
      return other instanceof StringComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $text = this.getText();
      return result * 59 + ($text == null ? 43 : $text.hashCode());
   }
}
