package com.sk89q.worldedit.util.formatting.text.renderer;

import com.sk89q.worldedit.util.formatting.text.BlockNbtComponent;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.EntityNbtComponent;
import com.sk89q.worldedit.util.formatting.text.KeybindComponent;
import com.sk89q.worldedit.util.formatting.text.NbtComponent;
import com.sk89q.worldedit.util.formatting.text.ScoreComponent;
import com.sk89q.worldedit.util.formatting.text.SelectorComponent;
import com.sk89q.worldedit.util.formatting.text.StorageNbtComponent;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class AbstractComponentRenderer<C> implements ComponentRenderer<C> {
   @NonNull
   @Override
   public Component render(@NonNull final Component component, @NonNull final C context) {
      if (component instanceof TextComponent) {
         return this.renderText((TextComponent)component, context);
      } else if (component instanceof TranslatableComponent) {
         return this.renderTranslatable((TranslatableComponent)component, context);
      } else if (component instanceof KeybindComponent) {
         return this.renderKeybind((KeybindComponent)component, context);
      } else if (component instanceof ScoreComponent) {
         return this.renderScore((ScoreComponent)component, context);
      } else if (component instanceof SelectorComponent) {
         return this.renderSelector((SelectorComponent)component, context);
      } else {
         if (component instanceof NbtComponent) {
            if (component instanceof BlockNbtComponent) {
               return this.renderBlockNbt((BlockNbtComponent)component, context);
            }

            if (component instanceof EntityNbtComponent) {
               return this.renderEntityNbt((EntityNbtComponent)component, context);
            }

            if (component instanceof StorageNbtComponent) {
               return this.renderStorageNbt((StorageNbtComponent)component, context);
            }
         }

         return component;
      }
   }

   @NonNull
   protected abstract Component renderBlockNbt(@NonNull final BlockNbtComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderEntityNbt(@NonNull final EntityNbtComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderStorageNbt(@NonNull final StorageNbtComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderKeybind(@NonNull final KeybindComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderScore(@NonNull final ScoreComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderSelector(@NonNull final SelectorComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderText(@NonNull final TextComponent component, @NonNull final C context);

   @NonNull
   protected abstract Component renderTranslatable(@NonNull final TranslatableComponent component, @NonNull final C context);
}
