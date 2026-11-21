package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;

public class ScoreComponent extends TextComponent {
   private String name;
   private String objective;
   @Nullable
   private String value;

   public ScoreComponent(String name, String objective) {
      this(name, objective, null);
   }

   public ScoreComponent(String name, String objective, @Nullable String value) {
      this.name = name;
      this.objective = objective;
      this.value = value;
   }

   public String getName() {
      return this.name;
   }

   public ScoreComponent setName(String name) {
      this.name = name;
      return this;
   }

   public String getObjective() {
      return this.objective;
   }

   public ScoreComponent setObjective(@Nullable String objective) {
      this.objective = objective;
      return this;
   }

   @Nullable
   public String getValue() {
      return this.value;
   }

   public ScoreComponent setValue(@Nullable String value) {
      this.value = value;
      return this;
   }

   @Override
   public String asSingleString() {
      return this.value;
   }

   @Override
   public TextComponent shallowCopy() {
      ScoreComponent copy = new ScoreComponent(this.name, this.objective, this.value);
      return copy.setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .add("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .add("style", this.getStyle(), style -> !style.isEmpty())
         .add("name", this.name)
         .add("objective", this.objective)
         .add("value", this.value, Objects::nonNull)
         .toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ScoreComponent)) {
         return false;
      } else {
         ScoreComponent other = (ScoreComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null ? other$name == null : this$name.equals(other$name)) {
               Object this$objective = this.getObjective();
               Object other$objective = other.getObjective();
               if (this$objective == null ? other$objective == null : this$objective.equals(other$objective)) {
                  Object this$value = this.getValue();
                  Object other$value = other.getValue();
                  return this$value == null ? other$value == null : this$value.equals(other$value);
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
      return other instanceof ScoreComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $objective = this.getObjective();
      result = result * 59 + ($objective == null ? 43 : $objective.hashCode());
      Object $value = this.getValue();
      return result * 59 + ($value == null ? 43 : $value.hashCode());
   }
}
