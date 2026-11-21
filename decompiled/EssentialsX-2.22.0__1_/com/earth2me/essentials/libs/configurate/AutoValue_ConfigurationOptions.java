package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializerCollection;
import com.earth2me.essentials.libs.configurate.util.MapFactory;
import java.util.Set;

final class AutoValue_ConfigurationOptions extends ConfigurationOptions {
   private final MapFactory mapFactory;
   @Nullable
   private final String header;
   private final TypeSerializerCollection serializers;
   @Nullable
   private final Set<Class<?>> nativeTypes;
   private final boolean shouldCopyDefaults;
   private final boolean implicitInitialization;

   AutoValue_ConfigurationOptions(
      MapFactory mapFactory,
      @Nullable String header,
      TypeSerializerCollection serializers,
      @Nullable Set<Class<?>> nativeTypes,
      boolean shouldCopyDefaults,
      boolean implicitInitialization
   ) {
      if (mapFactory == null) {
         throw new NullPointerException("Null mapFactory");
      } else {
         this.mapFactory = mapFactory;
         this.header = header;
         if (serializers == null) {
            throw new NullPointerException("Null serializers");
         } else {
            this.serializers = serializers;
            this.nativeTypes = nativeTypes;
            this.shouldCopyDefaults = shouldCopyDefaults;
            this.implicitInitialization = implicitInitialization;
         }
      }
   }

   @Override
   public MapFactory mapFactory() {
      return this.mapFactory;
   }

   @Nullable
   @Override
   public String header() {
      return this.header;
   }

   @Override
   public TypeSerializerCollection serializers() {
      return this.serializers;
   }

   @Nullable
   @Override
   Set<Class<?>> nativeTypes() {
      return this.nativeTypes;
   }

   @Override
   public boolean shouldCopyDefaults() {
      return this.shouldCopyDefaults;
   }

   @Override
   public boolean implicitInitialization() {
      return this.implicitInitialization;
   }

   @Override
   public String toString() {
      return "ConfigurationOptions{mapFactory="
         + this.mapFactory
         + ", header="
         + this.header
         + ", serializers="
         + this.serializers
         + ", nativeTypes="
         + this.nativeTypes
         + ", shouldCopyDefaults="
         + this.shouldCopyDefaults
         + ", implicitInitialization="
         + this.implicitInitialization
         + "}";
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ConfigurationOptions)) {
         return false;
      } else {
         ConfigurationOptions that = (ConfigurationOptions)o;
         return this.mapFactory.equals(that.mapFactory())
            && (this.header == null ? that.header() == null : this.header.equals(that.header()))
            && this.serializers.equals(that.serializers())
            && (this.nativeTypes == null ? that.nativeTypes() == null : this.nativeTypes.equals(that.nativeTypes()))
            && this.shouldCopyDefaults == that.shouldCopyDefaults()
            && this.implicitInitialization == that.implicitInitialization();
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.mapFactory.hashCode();
      h$ *= 1000003;
      h$ ^= this.header == null ? 0 : this.header.hashCode();
      h$ *= 1000003;
      h$ ^= this.serializers.hashCode();
      h$ *= 1000003;
      h$ ^= this.nativeTypes == null ? 0 : this.nativeTypes.hashCode();
      h$ *= 1000003;
      h$ ^= this.shouldCopyDefaults ? 1231 : 1237;
      h$ *= 1000003;
      return h$ ^ (this.implicitInitialization ? 1231 : 1237);
   }
}
