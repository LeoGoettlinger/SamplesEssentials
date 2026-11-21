package io.github.retrooper.packetevents.adventure.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.retrooper.packetevents.adventure.option.OptionState;
import io.github.retrooper.packetevents.adventure.serializer.json.JSONOptions;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Services;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GsonComponentSerializerImpl implements GsonComponentSerializer {
   private static final Optional<GsonComponentSerializer.Provider> SERVICE = Services.service(GsonComponentSerializer.Provider.class);
   static final Consumer<GsonComponentSerializer.Builder> BUILDER = SERVICE.map(GsonComponentSerializer.Provider::builder).orElseGet(() -> builder -> {});
   private final Gson serializer;
   private final UnaryOperator<GsonBuilder> populator;
   @Nullable
   private final io.github.retrooper.packetevents.adventure.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer;
   @Nullable
   private final BackwardCompatUtil.ShowAchievementToComponent compatShowAchievement;
   private final OptionState flags;

   GsonComponentSerializerImpl(
      OptionState flags,
      @Nullable io.github.retrooper.packetevents.adventure.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer,
      @Nullable BackwardCompatUtil.ShowAchievementToComponent compatShowAchievement
   ) {
      this.flags = flags;
      this.legacyHoverSerializer = legacyHoverSerializer;
      this.compatShowAchievement = compatShowAchievement;
      this.populator = builder -> {
         builder.registerTypeAdapterFactory(new SerializerFactory(flags, legacyHoverSerializer, compatShowAchievement));
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
   public Component deserialize(@NotNull String string) {
      return (Component)this.serializer().fromJson(string, Component.class);
   }

   @Nullable
   public Component deserializeOr(@Nullable String input, @Nullable Component fallback) {
      if (input == null) {
         return fallback;
      } else {
         Component component = (Component)this.serializer().fromJson(input, Component.class);
         return component == null ? fallback : component;
      }
   }

   @NotNull
   public String serialize(@NotNull Component component) {
      return this.serializer().toJson(component);
   }

   @NotNull
   @Override
   public Component deserializeFromTree(@NotNull JsonElement input) {
      return (Component)this.serializer().fromJson(input, Component.class);
   }

   @NotNull
   @Override
   public JsonElement serializeToTree(@NotNull Component component) {
      return this.serializer().toJsonTree(component);
   }

   @NotNull
   public GsonComponentSerializer.Builder toBuilder() {
      return new GsonComponentSerializerImpl.BuilderImpl(this);
   }

   static final class BuilderImpl implements GsonComponentSerializer.Builder {
      private OptionState flags = JSONOptions.byDataVersion();
      @Nullable
      private io.github.retrooper.packetevents.adventure.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer;
      @Nullable
      private BackwardCompatUtil.ShowAchievementToComponent compatShowAchievement;

      BuilderImpl() {
         GsonComponentSerializerImpl.BUILDER.accept(this);
      }

      BuilderImpl(GsonComponentSerializerImpl serializer) {
         this();
         this.flags = serializer.flags;
         this.legacyHoverSerializer = serializer.legacyHoverSerializer;
         this.compatShowAchievement = serializer.compatShowAchievement;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder options(@NotNull OptionState flags) {
         this.flags = Objects.requireNonNull(flags, "flags");
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder editOptions(@NotNull Consumer<OptionState.Builder> optionEditor) {
         OptionState.Builder builder = JSONOptions.schema().stateBuilder().values(this.flags);
         Objects.requireNonNull(optionEditor, "flagEditor").accept(builder);
         this.flags = builder.build();
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder legacyHoverEventSerializer(
         @Nullable io.github.retrooper.packetevents.adventure.serializer.json.LegacyHoverEventSerializer serializer
      ) {
         this.legacyHoverSerializer = serializer;
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer.Builder showAchievementToComponent(@Nullable BackwardCompatUtil.ShowAchievementToComponent compatShowAchievement) {
         this.compatShowAchievement = compatShowAchievement;
         return this;
      }

      @NotNull
      @Override
      public GsonComponentSerializer build() {
         return new GsonComponentSerializerImpl(this.flags, this.legacyHoverSerializer, this.compatShowAchievement);
      }
   }

   static final class Instances {
      static final GsonComponentSerializer INSTANCE = GsonComponentSerializerImpl.SERVICE
         .map(GsonComponentSerializer.Provider::gson)
         .orElseGet(() -> new GsonComponentSerializerImpl(JSONOptions.byDataVersion(), null, null));
      static final GsonComponentSerializer LEGACY_INSTANCE = GsonComponentSerializerImpl.SERVICE
         .map(GsonComponentSerializer.Provider::gsonLegacy)
         .orElseGet(() -> new GsonComponentSerializerImpl(JSONOptions.byDataVersion().at(2525), null, null));
   }
}
