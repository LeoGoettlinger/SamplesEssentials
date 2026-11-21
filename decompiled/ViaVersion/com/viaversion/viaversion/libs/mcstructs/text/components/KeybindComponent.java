package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.translation.Translator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Generated;

public class KeybindComponent extends TextComponent {
   private String keybind;
   private Translator translator;

   public KeybindComponent(String keybind) {
      this(keybind, Translator.GLOBAL);
   }

   public KeybindComponent(String keybind, @Nonnull Translator translator) {
      this.keybind = keybind;
      this.translator = translator;
   }

   public String getKeybind() {
      return this.keybind;
   }

   public KeybindComponent setKeybind(String keybind) {
      this.keybind = keybind;
      return this;
   }

   public KeybindComponent setTranslator(@Nullable Translator translator) {
      if (translator == null) {
         this.translator = Translator.GLOBAL;
      } else {
         this.translator = translator;
      }

      return this;
   }

   @Override
   public String asSingleString() {
      return this.translator.translateOrKey(this.keybind);
   }

   @Override
   public TextComponent shallowCopy() {
      KeybindComponent copy = new KeybindComponent(this.keybind);
      copy.translator = this.translator;
      return copy.setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .add("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .add("style", this.getStyle(), style -> !style.isEmpty())
         .add("keybind", this.keybind)
         .add("translator", this.translator, translator -> translator != Translator.GLOBAL)
         .toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof KeybindComponent)) {
         return false;
      } else {
         KeybindComponent other = (KeybindComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$keybind = this.getKeybind();
            Object other$keybind = other.getKeybind();
            if (this$keybind == null ? other$keybind == null : this$keybind.equals(other$keybind)) {
               Object this$translator = this.translator;
               Object other$translator = other.translator;
               return this$translator == null ? other$translator == null : this$translator.equals(other$translator);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   @Override
   protected boolean canEqual(Object other) {
      return other instanceof KeybindComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $keybind = this.getKeybind();
      result = result * 59 + ($keybind == null ? 43 : $keybind.hashCode());
      Object $translator = this.translator;
      return result * 59 + ($translator == null ? 43 : $translator.hashCode());
   }
}
