package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.geantyref.TypeToken;

final class AutoValue_RepresentationHint<V> extends RepresentationHint<V> {
   private final String identifier;
   private final TypeToken<V> valueType;
   @Nullable
   private final V defaultValue;
   private final boolean inheritable;

   private AutoValue_RepresentationHint(String identifier, TypeToken<V> valueType, @Nullable V defaultValue, boolean inheritable) {
      this.identifier = identifier;
      this.valueType = valueType;
      this.defaultValue = defaultValue;
      this.inheritable = inheritable;
   }

   @Override
   public String identifier() {
      return this.identifier;
   }

   @Override
   public TypeToken<V> valueType() {
      return this.valueType;
   }

   @Nullable
   @Override
   public V defaultValue() {
      return this.defaultValue;
   }

   @Override
   public boolean inheritable() {
      return this.inheritable;
   }

   @Override
   public String toString() {
      return "RepresentationHint{identifier="
         + this.identifier
         + ", valueType="
         + this.valueType
         + ", defaultValue="
         + this.defaultValue
         + ", inheritable="
         + this.inheritable
         + "}";
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RepresentationHint)) {
         return false;
      } else {
         RepresentationHint<?> that = (RepresentationHint<?>)o;
         return this.identifier.equals(that.identifier())
            && this.valueType.equals(that.valueType())
            && (this.defaultValue == null ? that.defaultValue() == null : this.defaultValue.equals(that.defaultValue()))
            && this.inheritable == that.inheritable();
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.identifier.hashCode();
      h$ *= 1000003;
      h$ ^= this.valueType.hashCode();
      h$ *= 1000003;
      h$ ^= this.defaultValue == null ? 0 : this.defaultValue.hashCode();
      h$ *= 1000003;
      return h$ ^ (this.inheritable ? 1231 : 1237);
   }

   static final class Builder<V> extends RepresentationHint.Builder<V> {
      @Nullable
      private String identifier;
      @Nullable
      private TypeToken<V> valueType;
      @Nullable
      private V defaultValue;
      private boolean inheritable;
      private byte set$0;

      @Override
      public RepresentationHint.Builder<V> identifier(String identifier) {
         if (identifier == null) {
            throw new NullPointerException("Null identifier");
         } else {
            this.identifier = identifier;
            return this;
         }
      }

      @Override
      public RepresentationHint.Builder<V> valueType(TypeToken<V> valueType) {
         if (valueType == null) {
            throw new NullPointerException("Null valueType");
         } else {
            this.valueType = valueType;
            return this;
         }
      }

      @Override
      public RepresentationHint.Builder<V> defaultValue(V defaultValue) {
         this.defaultValue = defaultValue;
         return this;
      }

      @Override
      public RepresentationHint.Builder<V> inheritable(boolean inheritable) {
         this.inheritable = inheritable;
         this.set$0 = (byte)(this.set$0 | 1);
         return this;
      }

      @Override
      public RepresentationHint<V> build() {
         if (this.set$0 == 1 && this.identifier != null && this.valueType != null) {
            return new AutoValue_RepresentationHint<>(this.identifier, this.valueType, this.defaultValue, this.inheritable);
         } else {
            StringBuilder missing = new StringBuilder();
            if (this.identifier == null) {
               missing.append(" identifier");
            }

            if (this.valueType == null) {
               missing.append(" valueType");
            }

            if ((this.set$0 & 1) == 0) {
               missing.append(" inheritable");
            }

            throw new IllegalStateException("Missing required properties:" + missing);
         }
      }
   }
}
