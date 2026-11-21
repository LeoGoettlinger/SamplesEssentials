package ac.grim.grimac.shaded.kyori.adventure.text.serializer.gson;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.serializer.json.JSONOptions;
import ac.grim.grimac.shaded.kyori.adventure.util.Services;
import ac.grim.grimac.shaded.kyori.option.OptionState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

final class GsonComponentSerializerImpl implements GsonComponentSerializer {
   private static final Optional<GsonComponentSerializer.Provider> SERVICE = Services.service(GsonComponentSerializer.Provider.class);
   static final Consumer<GsonComponentSerializer.Builder> BUILDER = SERVICE.map(GsonComponentSerializer.Provider::builder).orElseGet(() -> builder -> {});
   private final Gson serializer;
   private final UnaryOperator<GsonBuilder> populator;
   @Nullable
   private final ac.grim.grimac.shaded.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer;
   private final OptionState flags;

   GsonComponentSerializerImpl(
      final OptionState flags, @Nullable final ac.grim.grimac.shaded.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer
   ) {
      this.flags = flags;
      this.legacyHoverSerializer = legacyHoverSerializer;
      this.populator = builder -> {
         builder.registerTypeAdapterFactory(new SerializerFactory(flags, legacyHoverSerializer));
         return builder;
      };
      this.serializer = this.populator.apply(new GsonBuilder().disableHtmlEscaping()).create();
   }

   @NotNull
   @Override
   public Gson serializer() {
      return this.serializer;
   }

   @NotNull
   @Override
   public UnaryOperator<GsonBuilder> populator() {
      return this.populator;
   }

   @NotNull
   public Component deserialize(@NotNull final String string) {
      Component component = (Component)this.serializer().fromJson(string, Component.class);
      if (component == null) {
         throw ComponentSerializerImpl.notSureHowToDeserialize(string);
      } else {
         return component;
      }
   }

   @Nullable
   public Component deserializeOr(@Nullable final String input, @Nullable final Component fallback) {
      if (input == null) {
         return fallback;
      } else {
         Component component = (Component)this.serializer().fromJson(input, Component.class);
         return component == null ? fallback : component;
      }
   }

   @NotNull
   public String serialize(@NotNull final Component component) {
      return this.serializer().toJson(component);
   }

   @NotNull
   @Override
   public Component deserializeFromTree(@NotNull final JsonElement input) {
      Component component = (Component)this.serializer().fromJson(input, Component.class);
      if (component == null) {
         throw ComponentSerializerImpl.notSureHowToDeserialize(input);
      } else {
         return component;
      }
   }

   @NotNull
   @Override
   public JsonElement serializeToTree(@NotNull final Component component) {
      return this.serializer().toJsonTree(component);
   }

   @NotNull
   public GsonComponentSerializer.Builder toBuilder() {
      return new GsonComponentSerializerImpl.BuilderImpl(this);
   }

   static final class BuilderImpl implements GsonComponentSerializer.Builder {
      private OptionState flags = JSONOptions.byDataVersion();
      @Nullable
      private ac.grim.grimac.shaded.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer;

      BuilderImpl() {
         GsonComponentSerializerImpl.BUILDER.accept(this);
      }

      BuilderImpl(final GsonComponentSerializerImpl serializer) {
         this();
         this.flags = serializer.flags;
         this.legacyHoverSerializer = serializer.legacyHoverSerializer;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder options(@NotNull final OptionState flags) {
         this.flags = Objects.requireNonNull(flags, "flags");
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder editOptions(@NotNull final Consumer<OptionState.Builder> optionEditor) {
         OptionState.Builder builder = JSONOptions.schema().stateBuilder().values(this.flags);
         Objects.requireNonNull(optionEditor, "flagEditor").accept(builder);
         this.flags = builder.build();
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder legacyHoverEventSerializer(
         @Nullable final ac.grim.grimac.shaded.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer serializer
      ) {
         this.legacyHoverSerializer = serializer;
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer build() {
         return new GsonComponentSerializerImpl(this.flags, this.legacyHoverSerializer);
      }
   }

   static final class Instances {
      static final GsonComponentSerializer INSTANCE = GsonComponentSerializerImpl.SERVICE
         .map(GsonComponentSerializer.Provider::gson)
         .orElseGet(() -> new GsonComponentSerializerImpl(JSONOptions.byDataVersion(), null));
      static final GsonComponentSerializer LEGACY_INSTANCE = GsonComponentSerializerImpl.SERVICE
         .map(GsonComponentSerializer.Provider::gsonLegacy)
         .orElseGet(() -> new GsonComponentSerializerImpl(JSONOptions.byDataVersion().at(2525), null));
   }
}
