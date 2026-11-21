package net.luckperms.api.query;

import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

final class SimpleOptionKey<T> implements OptionKey<T> {
   private final String name;
   private final Class<T> type;
   private final int hashCode;

   SimpleOptionKey(String name, Class<T> type) {
      this.name = name.toLowerCase(Locale.ROOT);
      this.type = type;
      this.hashCode = Objects.hash(this.name, this.type);
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
      return "OptionKey(name=" + this.name + ", type=" + this.type.getName() + ")";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SimpleOptionKey<?> that = (SimpleOptionKey<?>)o;
         return this.name.equals(that.name) && this.type.equals(that.type);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }
}
