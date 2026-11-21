package org.jdbi.v3.core.argument.internal;

import java.util.Objects;
import org.jdbi.v3.core.qualifier.QualifiedType;

public final class TypedValue {
   private final QualifiedType<?> type;
   private final Object value;

   public TypedValue(QualifiedType<?> qualifiedType, Object value) {
      this.type = Objects.requireNonNull(qualifiedType, "qualifiedType is null");
      this.value = value;
   }

   public QualifiedType<?> getType() {
      return this.type;
   }

   public Object getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return "(" + this.type + ") " + this.value;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TypedValue that = (TypedValue)o;
         return this.type.equals(that.type) && Objects.equals(this.value, that.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.value);
   }
}
