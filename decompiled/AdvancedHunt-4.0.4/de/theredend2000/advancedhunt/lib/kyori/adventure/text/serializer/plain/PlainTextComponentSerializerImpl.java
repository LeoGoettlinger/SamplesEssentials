package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.plain;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener.ComponentFlattener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Services;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

final class PlainTextComponentSerializerImpl implements PlainTextComponentSerializer {
   private static final ComponentFlattener DEFAULT_FLATTENER = ComponentFlattener.basic().toBuilder().unknownMapper(component -> {
      throw new UnsupportedOperationException("Don't know how to turn " + component.getClass().getSimpleName() + " into a string");
   }).build();
   private static final Optional<PlainTextComponentSerializer.Provider> SERVICE = Services.service(
      ServiceLoader.load(PlainTextComponentSerializer.Provider.class, PlainTextComponentSerializer.Provider.class.getClassLoader()),
      PlainTextComponentSerializer.Provider.class
   );
   static final Consumer<PlainTextComponentSerializer.Builder> BUILDER = SERVICE.map(PlainTextComponentSerializer.Provider::plainText)
      .orElseGet(() -> builder -> {});
   final ComponentFlattener flattener;

   PlainTextComponentSerializerImpl(final ComponentFlattener flattener) {
      this.flattener = flattener;
   }

   @Override
   public void serialize(final StringBuilder sb, final Component component) {
      this.flattener.flatten(Objects.requireNonNull(component, "component"), sb::append);
   }

   @Override
   public PlainTextComponentSerializer.Builder toBuilder() {
      return new PlainTextComponentSerializerImpl.BuilderImpl(this);
   }

   static final class BuilderImpl implements PlainTextComponentSerializer.Builder {
      private ComponentFlattener flattener = PlainTextComponentSerializerImpl.DEFAULT_FLATTENER;

      BuilderImpl() {
         PlainTextComponentSerializerImpl.BUILDER.accept(this);
      }

      BuilderImpl(final PlainTextComponentSerializerImpl serializer) {
         this();
         this.flattener = serializer.flattener;
      }

      @Override
      public PlainTextComponentSerializer.Builder flattener(final ComponentFlattener flattener) {
         this.flattener = Objects.requireNonNull(flattener, "flattener");
         return this;
      }

      public PlainTextComponentSerializer build() {
         return new PlainTextComponentSerializerImpl(this.flattener);
      }
   }

   static final class Instances {
      static final PlainTextComponentSerializer INSTANCE = PlainTextComponentSerializerImpl.SERVICE
         .map(PlainTextComponentSerializer.Provider::plainTextSimple)
         .orElseGet(() -> new PlainTextComponentSerializerImpl(PlainTextComponentSerializerImpl.DEFAULT_FLATTENER));
   }
}
