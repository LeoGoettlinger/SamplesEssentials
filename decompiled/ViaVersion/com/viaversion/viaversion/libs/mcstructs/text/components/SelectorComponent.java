package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;

public class SelectorComponent extends TextComponent {
   private String selector;
   @Nullable
   private TextComponent separator;

   public SelectorComponent(String selector) {
      this(selector, null);
   }

   public SelectorComponent(String selector, @Nullable TextComponent separator) {
      this.selector = selector;
      this.separator = separator;
   }

   public String getSelector() {
      return this.selector;
   }

   public SelectorComponent setSelector(String selector) {
      this.selector = selector;
      return this;
   }

   @Nullable
   public TextComponent getSeparator() {
      return this.separator;
   }

   public SelectorComponent setSeparator(@Nullable TextComponent separator) {
      this.separator = separator;
      return this;
   }

   @Override
   public String asSingleString() {
      return this.selector;
   }

   @Override
   public TextComponent shallowCopy() {
      return this.separator == null
         ? new SelectorComponent(this.selector, null).setStyle(this.getStyle().copy())
         : new SelectorComponent(this.selector, this.separator.copy()).setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .add("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .add("style", this.getStyle(), style -> !style.isEmpty())
         .add("selector", this.selector)
         .add("separator", this.separator, Objects::nonNull)
         .toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SelectorComponent)) {
         return false;
      } else {
         SelectorComponent other = (SelectorComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$selector = this.getSelector();
            Object other$selector = other.getSelector();
            if (this$selector == null ? other$selector == null : this$selector.equals(other$selector)) {
               Object this$separator = this.getSeparator();
               Object other$separator = other.getSeparator();
               return this$separator == null ? other$separator == null : this$separator.equals(other$separator);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   @Override
   protected boolean canEqual(Object other) {
      return other instanceof SelectorComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $selector = this.getSelector();
      result = result * 59 + ($selector == null ? 43 : $selector.hashCode());
      Object $separator = this.getSeparator();
      return result * 59 + ($separator == null ? 43 : $separator.hashCode());
   }
}
