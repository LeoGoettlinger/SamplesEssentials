package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.NbtDataSource;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;

public class NbtComponent extends TextComponent {
   private String component;
   private boolean resolve;
   private boolean plain;
   private TextComponent separator;
   private NbtDataSource dataSource;

   public NbtComponent(String component, boolean resolve, NbtDataSource dataSource) {
      this(component, resolve, null, dataSource);
   }

   public NbtComponent(String component, boolean resolve, boolean plain, NbtDataSource dataSource) {
      this(component, resolve, plain, null, dataSource);
   }

   public NbtComponent(String component, boolean resolve, @Nullable TextComponent separator, NbtDataSource dataSource) {
      this(component, resolve, false, separator, dataSource);
   }

   public NbtComponent(String component, boolean resolve, boolean plain, @Nullable TextComponent separator, NbtDataSource dataSource) {
      this.component = component;
      this.resolve = resolve;
      this.plain = plain;
      this.separator = separator;
      this.dataSource = dataSource;
   }

   @Override
   public String asSingleString() {
      return "";
   }

   @Override
   public TextComponent shallowCopy() {
      NbtComponent copy = new NbtComponent(this.component, this.resolve, this.separator, this.dataSource);
      return copy.setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .add("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .add("style", this.getStyle(), style -> !style.isEmpty())
         .add("component", this.component)
         .add("resolve", this.resolve)
         .add("separator", this.separator, Objects::nonNull)
         .toString();
   }

   @Generated
   public String getComponent() {
      return this.component;
   }

   @Generated
   public boolean isResolve() {
      return this.resolve;
   }

   @Generated
   public boolean isPlain() {
      return this.plain;
   }

   @Generated
   public TextComponent getSeparator() {
      return this.separator;
   }

   @Generated
   public NbtDataSource getDataSource() {
      return this.dataSource;
   }

   @Generated
   public NbtComponent setComponent(String component) {
      this.component = component;
      return this;
   }

   @Generated
   public NbtComponent setResolve(boolean resolve) {
      this.resolve = resolve;
      return this;
   }

   @Generated
   public NbtComponent setPlain(boolean plain) {
      this.plain = plain;
      return this;
   }

   @Generated
   public NbtComponent setSeparator(TextComponent separator) {
      this.separator = separator;
      return this;
   }

   @Generated
   public NbtComponent setDataSource(NbtDataSource dataSource) {
      this.dataSource = dataSource;
      return this;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof NbtComponent)) {
         return false;
      } else {
         NbtComponent other = (NbtComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else if (this.isResolve() != other.isResolve()) {
            return false;
         } else if (this.isPlain() != other.isPlain()) {
            return false;
         } else {
            Object this$component = this.getComponent();
            Object other$component = other.getComponent();
            if (this$component == null ? other$component == null : this$component.equals(other$component)) {
               Object this$separator = this.getSeparator();
               Object other$separator = other.getSeparator();
               if (this$separator == null ? other$separator == null : this$separator.equals(other$separator)) {
                  Object this$dataSource = this.getDataSource();
                  Object other$dataSource = other.getDataSource();
                  return this$dataSource == null ? other$dataSource == null : this$dataSource.equals(other$dataSource);
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
   @Override
   protected boolean canEqual(Object other) {
      return other instanceof NbtComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      result = result * 59 + (this.isResolve() ? 79 : 97);
      result = result * 59 + (this.isPlain() ? 79 : 97);
      Object $component = this.getComponent();
      result = result * 59 + ($component == null ? 43 : $component.hashCode());
      Object $separator = this.getSeparator();
      result = result * 59 + ($separator == null ? 43 : $separator.hashCode());
      Object $dataSource = this.getDataSource();
      return result * 59 + ($dataSource == null ? 43 : $dataSource.hashCode());
   }
}
