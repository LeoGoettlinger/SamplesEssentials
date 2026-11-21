package com.sk89q.worldedit.util.formatting.text.renderer;

import com.sk89q.worldedit.util.formatting.text.Component;
import java.text.MessageFormat;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Deprecated
public abstract class FriendlyComponentRenderer<C> implements ComponentRenderer<C> {
   private final TranslatableComponentRenderer<C> renderer = new TranslatableComponentRenderer<C>() {
      @Nullable
      @Override
      protected MessageFormat translation(@NonNull final C context, @NonNull final String key) {
         return FriendlyComponentRenderer.this.translation(context, key);
      }
   };

   @NonNull
   public static <C> FriendlyComponentRenderer<C> from(@NonNull final BiFunction<C, String, MessageFormat> translations) {
      return new FriendlyComponentRenderer<C>() {
         @NonNull
         @Override
         protected MessageFormat translation(@NonNull final C context, @NonNull final String key) {
            return translations.apply(context, key);
         }
      };
   }

   @NonNull
   @Override
   public Component render(@NonNull final Component component, @NonNull final C context) {
      return this.renderer.render(component, context);
   }

   @Nullable
   protected abstract MessageFormat translation(@NonNull final C context, @NonNull final String key);
}
