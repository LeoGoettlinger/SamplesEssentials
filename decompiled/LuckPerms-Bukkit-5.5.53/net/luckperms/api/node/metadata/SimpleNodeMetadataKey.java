package net.luckperms.api.node.metadata;

import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

final class SimpleNodeMetadataKey<T> implements NodeMetadataKey<T> {
   private final String name;
   private final Class<T> type;

   SimpleNodeMetadataKey(String name, Class<T> type) {
      this.name = name.toLowerCase(Locale.ROOT);
      this.type = type;
   }

   @NonNull
   @Override
   public String name() {
      return this.name;
   }

   @NonNull
   @Override
   public Class<T> type() {
      return this.type;
   }

   @Override
   public String toString() {
      return "NodeMetadataKey(name=" + this.name + ", type=" + this.type.getName() + ")";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SimpleNodeMetadataKey<?> that = (SimpleNodeMetadataKey<?>)o;
         return this.name.equals(that.name) && this.type.equals(that.type);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.type);
   }
}
