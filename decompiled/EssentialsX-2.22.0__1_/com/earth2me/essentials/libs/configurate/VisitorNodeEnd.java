package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Objects;

final class VisitorNodeEnd {
   private final ConfigurationNode end;
   private final boolean isMap;

   VisitorNodeEnd(final ConfigurationNode end, final boolean isMap) {
      this.end = end;
      this.isMap = isMap;
   }

   ConfigurationNode end() {
      return this.end;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof VisitorNodeEnd)) {
         return false;
      } else {
         VisitorNodeEnd that = (VisitorNodeEnd)o;
         return this.end().equals(that.end());
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.end());
   }

   @Nullable
   static <A extends AbstractConfigurationNode<?, A>, S, E extends Exception> A popFromVisitor(
      final Object unknown, final ConfigurationVisitor<S, ?, E> visitor, final S state
   ) throws E {
      if (unknown instanceof VisitorNodeEnd) {
         ConfigurationNode node = ((VisitorNodeEnd)unknown).end();
         if (((VisitorNodeEnd)unknown).isMap) {
            visitor.exitMappingNode(node, state);
         } else {
            visitor.exitListNode(node, state);
         }

         return null;
      } else if (unknown instanceof AbstractConfigurationNode) {
         return (A)unknown;
      } else {
         throw new IllegalStateException("Unknown value type " + unknown);
      }
   }
}
