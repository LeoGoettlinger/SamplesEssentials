package org.enginehub.piston.part;

import com.sk89q.worldedit.util.formatting.text.Component;

final class AutoValue_NoArgCommandFlag extends NoArgCommandFlag {
   private final Component description;
   private final char name;

   private AutoValue_NoArgCommandFlag(Component description, char name) {
      this.description = description;
      this.name = name;
   }

   @Override
   public Component getDescription() {
      return this.description;
   }

   @Override
   public char getName() {
      return this.name;
   }

   @Override
   public String toString() {
      return "NoArgCommandFlag{description=" + this.description + ", name=" + this.name + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof NoArgCommandFlag that) ? false : this.description.equals(that.getDescription()) && this.name == that.getName();
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.description.hashCode();
      h$ *= 1000003;
      return h$ ^ this.name;
   }

   static final class Builder extends NoArgCommandFlag.Builder {
      private Component description;
      private char name;
      private byte set$0;

      @Override
      NoArgCommandFlag.Builder description(Component description) {
         if (description == null) {
            throw new NullPointerException("Null description");
         } else {
            this.description = description;
            return this;
         }
      }

      @Override
      NoArgCommandFlag.Builder name(char name) {
         this.name = name;
         this.set$0 = (byte)(this.set$0 | 1);
         return this;
      }

      @Override
      public NoArgCommandFlag build() {
         if (this.set$0 == 1 && this.description != null) {
            return new AutoValue_NoArgCommandFlag(this.description, this.name);
         } else {
            StringBuilder missing = new StringBuilder();
            if (this.description == null) {
               missing.append(" description");
            }

            if ((this.set$0 & 1) == 0) {
               missing.append(" name");
            }

            throw new IllegalStateException("Missing required properties:" + missing);
         }
      }
   }
}
