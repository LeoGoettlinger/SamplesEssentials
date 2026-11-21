package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializerCollection;
import com.earth2me.essentials.libs.configurate.util.MapFactories;
import com.earth2me.essentials.libs.configurate.util.MapFactory;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.google.auto.value.AutoValue;
import com.google.errorprone.annotations.CheckReturnValue;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@CheckReturnValue
@AutoValue
public abstract class ConfigurationOptions {
   ConfigurationOptions() {
   }

   public static ConfigurationOptions defaults() {
      return ConfigurationOptions.Lazy.DEFAULTS;
   }

   public abstract MapFactory mapFactory();

   public ConfigurationOptions mapFactory(final MapFactory mapFactory) {
      Objects.requireNonNull(mapFactory, "mapFactory");
      return (ConfigurationOptions)(this.mapFactory() == mapFactory
         ? this
         : new AutoValue_ConfigurationOptions(
            mapFactory, this.header(), this.serializers(), this.nativeTypes(), this.shouldCopyDefaults(), this.implicitInitialization()
         ));
   }

   @Nullable
   public abstract String header();

   public ConfigurationOptions header(@Nullable final String header) {
      return (ConfigurationOptions)(Objects.equals(this.header(), header)
         ? this
         : new AutoValue_ConfigurationOptions(
            this.mapFactory(), header, this.serializers(), this.nativeTypes(), this.shouldCopyDefaults(), this.implicitInitialization()
         ));
   }

   public abstract TypeSerializerCollection serializers();

   public ConfigurationOptions serializers(final TypeSerializerCollection serializers) {
      Objects.requireNonNull(serializers, "serializers");
      return (ConfigurationOptions)(this.serializers().equals(serializers)
         ? this
         : new AutoValue_ConfigurationOptions(
            this.mapFactory(), this.header(), serializers, this.nativeTypes(), this.shouldCopyDefaults(), this.implicitInitialization()
         ));
   }

   public final ConfigurationOptions serializers(final Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
      Objects.requireNonNull(serializerBuilder, "serializerBuilder");
      TypeSerializerCollection.Builder builder = this.serializers().childBuilder();
      serializerBuilder.accept(builder);
      return this.serializers(builder.build());
   }

   @Nullable
   abstract Set<Class<?>> nativeTypes();

   public ConfigurationOptions nativeTypes(@Nullable final Set<Class<?>> nativeTypes) {
      return (ConfigurationOptions)(Objects.equals(this.nativeTypes(), nativeTypes)
         ? this
         : new AutoValue_ConfigurationOptions(
            this.mapFactory(),
            this.header(),
            this.serializers(),
            nativeTypes == null ? null : UnmodifiableCollections.copyOf(nativeTypes),
            this.shouldCopyDefaults(),
            this.implicitInitialization()
         ));
   }

   public final boolean acceptsType(final Class<?> type) {
      Objects.requireNonNull(type, "type");
      Set<Class<?>> nativeTypes = this.nativeTypes();
      if (nativeTypes == null) {
         return true;
      } else if (nativeTypes.contains(type)) {
         return true;
      } else if (type.isPrimitive() && nativeTypes.contains(Types.box(type))) {
         return true;
      } else {
         Type unboxed = Types.unbox(type);
         if (unboxed != type && nativeTypes.contains(unboxed)) {
            return true;
         } else {
            for (Class<?> clazz : nativeTypes) {
               if (clazz.isAssignableFrom(type)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public abstract boolean shouldCopyDefaults();

   public ConfigurationOptions shouldCopyDefaults(final boolean shouldCopyDefaults) {
      return (ConfigurationOptions)(this.shouldCopyDefaults() == shouldCopyDefaults
         ? this
         : new AutoValue_ConfigurationOptions(
            this.mapFactory(), this.header(), this.serializers(), this.nativeTypes(), shouldCopyDefaults, this.implicitInitialization()
         ));
   }

   public abstract boolean implicitInitialization();

   public ConfigurationOptions implicitInitialization(final boolean implicitInitialization) {
      return (ConfigurationOptions)(this.implicitInitialization() == implicitInitialization
         ? this
         : new AutoValue_ConfigurationOptions(
            this.mapFactory(), this.header(), this.serializers(), this.nativeTypes(), this.shouldCopyDefaults(), implicitInitialization
         ));
   }

   static class Lazy {
      static final ConfigurationOptions DEFAULTS = new AutoValue_ConfigurationOptions(
         MapFactories.insertionOrdered(), null, TypeSerializerCollection.defaults(), null, true, true
      );
   }
}
