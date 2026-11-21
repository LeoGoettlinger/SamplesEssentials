package com.viaversion.viaversion.libs.mcstructs.core;

import lombok.Generated;

public class Identifier {
   public static final String DEFAULT_NAMESPACE = "minecraft";
   public static final String VALID_KEY_CHARS = "[_\\-a-z0-9.]*";
   public static final String VALID_VALUE_CHARS = "[_\\-a-z0-9/.]*";
   private final String key;
   private final String value;

   public static Identifier defaultNamespace(String value) {
      return of("minecraft", value);
   }

   public static Identifier of(String value) {
      int splitIndex = value.indexOf(58);
      String key = splitIndex <= 0 ? "minecraft" : value.substring(0, splitIndex);
      String val = splitIndex == -1 ? value : value.substring(splitIndex + 1);
      return of(key, val);
   }

   public static Identifier tryOf(String value) {
      try {
         return of(value);
      } catch (Throwable var2) {
         return null;
      }
   }

   public static Identifier of(String key, String value) {
      return new Identifier(key, value);
   }

   public static String stripDefaultNamespace(Identifier id) {
      return id.getKey().equals("minecraft") ? id.getValue() : id.get();
   }

   private Identifier(String key, String value) {
      if (!key.matches("[_\\-a-z0-9.]*")) {
         throw new IllegalArgumentException("Key contains illegal chars");
      } else if (!value.matches("[_\\-a-z0-9/.]*")) {
         throw new IllegalArgumentException("Value contains illegal chars");
      } else {
         this.key = key;
         this.value = value;
      }
   }

   public String get() {
      return this.key + ":" + this.value;
   }

   public String getKey() {
      return this.key;
   }

   public String getValue() {
      return this.value;
   }

   public boolean equals(String key, String value) {
      return this.key.equals(key) && this.value.equals(value);
   }

   @Override
   public String toString() {
      return this.get();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Identifier)) {
         return false;
      } else {
         Identifier other = (Identifier)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$key = this.getKey();
            Object other$key = other.getKey();
            if (this$key == null ? other$key == null : this$key.equals(other$key)) {
               Object this$value = this.getValue();
               Object other$value = other.getValue();
               return this$value == null ? other$value == null : this$value.equals(other$value);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Identifier;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $key = this.getKey();
      result = result * 59 + ($key == null ? 43 : $key.hashCode());
      Object $value = this.getValue();
      return result * 59 + ($value == null ? 43 : $value.hashCode());
   }
}
