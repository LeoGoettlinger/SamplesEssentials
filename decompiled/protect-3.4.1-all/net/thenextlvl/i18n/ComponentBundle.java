package net.thenextlvl.i18n;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Locale;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.title.Title.Times;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jspecify.annotations.Nullable;

public sealed interface ComponentBundle permits ComponentBundleImpl {
   @Contract(
      pure = true
   )
   MiniMessageTranslationStore translator();

   @Contract(
      mutates = "this"
   )
   ComponentBundle registerTranslations() throws IllegalStateException;

   @Contract(
      mutates = "this"
   )
   void unregisterTranslations() throws IllegalStateException;

   @CheckReturnValue
   @Nullable
   Component translate(@NonNls String var1, Audience var2, ComponentLike... var3);

   @CheckReturnValue
   @Nullable
   Component translate(@NonNls String var1, Audience var2);

   @CheckReturnValue
   @Nullable
   Component translate(@NonNls String var1, Locale var2, ComponentLike... var3);

   @CheckReturnValue
   @Nullable
   Component translate(@NonNls String var1, Locale var2);

   @CheckReturnValue
   @Nullable
   Component translate(TranslatableComponent var1, Audience var2);

   @CheckReturnValue
   @Nullable
   Component translate(TranslatableComponent var1, Locale var2);

   void sendMessage(Audience var1, @NonNls String var2);

   void sendMessage(Audience var1, @NonNls String var2, ComponentLike... var3);

   void sendMessage(Audience var1, @NonNls String var2, TagResolver... var3);

   void sendActionBar(Audience var1, @NonNls String var2, ComponentLike... var3);

   void showTitle(Audience var1, @NonNls @Nullable String var2, @NonNls @Nullable String var3, @Nullable Times var4, ComponentLike... var5);

   void showTitle(Audience var1, @NonNls @Nullable String var2, @NonNls @Nullable String var3, ComponentLike... var4);

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   Component component(@NonNls String var1, Audience var2);

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   Component component(@NonNls String var1, Audience var2, ComponentLike... var3);

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   Component component(@NonNls String var1, Audience var2, TagResolver... var3);

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   Component component(@NonNls String var1, Locale var2);

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   Component component(@NonNls String var1, Locale var2, ComponentLike... var3);

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   Component component(@NonNls String var1, Locale var2, TagResolver... var3);

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static ComponentBundle.Builder builder(Key name, Path path) {
      return new ComponentBundleImpl.Builder(name, path);
   }

   public sealed interface Builder permits ComponentBundleImpl.Builder {
      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder charset(Charset var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder fallback(Locale var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder migrator(@Nullable ResourceMigrator var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder miniMessage(MiniMessage var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder name(Key var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder path(Path var1);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder resource(String var1, Locale var2) throws IllegalStateException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder scope(ComponentBundle.Scope var1);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      ComponentBundle.Builder placeholder(@TagPattern String var1, @NonNls String var2);

      @Contract(
         value = "-> new",
         pure = true
      )
      ComponentBundle build() throws ResourceMigrationException;
   }

   public static enum Scope {
      FILTER_AND_FILL,
      FILTER,
      FILL,
      NONE;

      @Contract(
         pure = true
      )
      public boolean isFiltering() {
         return this.equals(FILTER) || this.equals(FILTER_AND_FILL);
      }

      @Contract(
         pure = true
      )
      public boolean isFilling() {
         return this.equals(FILL) || this.equals(FILTER_AND_FILL);
      }
   }
}
