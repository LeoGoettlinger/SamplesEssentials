package org.enginehub.piston.impl;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.enginehub.piston.part.CommandPart;

final class AutoValue_ArgBindingImpl extends ArgBindingImpl {
   private final String input;
   private final ImmutableMap<CommandPart, Boolean> partsMap;

   private AutoValue_ArgBindingImpl(String input, ImmutableMap<CommandPart, Boolean> partsMap) {
      this.input = input;
      this.partsMap = partsMap;
   }

   @Override
   public String getInput() {
      return this.input;
   }

   @Override
   ImmutableMap<CommandPart, Boolean> getPartsMap() {
      return this.partsMap;
   }

   @Override
   public String toString() {
      return "ArgBindingImpl{input=" + this.input + ", partsMap=" + this.partsMap + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof ArgBindingImpl that) ? false : this.input.equals(that.getInput()) && this.partsMap.equals(that.getPartsMap());
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.input.hashCode();
      h$ *= 1000003;
      return h$ ^ this.partsMap.hashCode();
   }

   static final class Builder implements ArgBindingImpl.Builder {
      private String input;
      private ImmutableMap<CommandPart, Boolean> partsMap;

      @Override
      public ArgBindingImpl.Builder input(String input) {
         if (input == null) {
            throw new NullPointerException("Null input");
         } else {
            this.input = input;
            return this;
         }
      }

      @Override
      public ArgBindingImpl.Builder partsMap(Map<CommandPart, Boolean> partsMap) {
         this.partsMap = ImmutableMap.copyOf(partsMap);
         return this;
      }

      @Override
      public ArgBindingImpl build() {
         if (this.input != null && this.partsMap != null) {
            return new AutoValue_ArgBindingImpl(this.input, this.partsMap);
         } else {
            StringBuilder missing = new StringBuilder();
            if (this.input == null) {
               missing.append(" input");
            }

            if (this.partsMap == null) {
               missing.append(" partsMap");
            }

            throw new IllegalStateException("Missing required properties:" + missing);
         }
      }
   }
}
