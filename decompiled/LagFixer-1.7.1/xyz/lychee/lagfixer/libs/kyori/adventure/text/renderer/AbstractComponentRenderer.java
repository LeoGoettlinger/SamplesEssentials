package xyz.lychee.lagfixer.libs.kyori.adventure.text.renderer;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.BlockNBTComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.EntityNBTComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.KeybindComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.NBTComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.ScoreComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.SelectorComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.StorageNBTComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TextComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TranslatableComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.VirtualComponent;

public abstract class AbstractComponentRenderer<C> implements ComponentRenderer<C> {
   @NotNull
   @Override
   public Component render(@NotNull Component component, @NotNull final C context) {
      if (component instanceof VirtualComponent) {
         component = this.renderVirtual((VirtualComponent)component, context);
      }

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
         if (component instanceof NBTComponent) {
            if (component instanceof BlockNBTComponent) {
               return this.renderBlockNbt((BlockNBTComponent)component, context);
            }

            if (component instanceof EntityNBTComponent) {
               return this.renderEntityNbt((EntityNBTComponent)component, context);
            }

            if (component instanceof StorageNBTComponent) {
               return this.renderStorageNbt((StorageNBTComponent)component, context);
            }
         }

         return component;
      }
   }

   @NotNull
   protected abstract Component renderBlockNbt(@NotNull final BlockNBTComponent component, @NotNull final C context);

   @NotNull
   protected abstract Component renderEntityNbt(@NotNull final EntityNBTComponent component, @NotNull final C context);

   @NotNull
   protected abstract Component renderStorageNbt(@NotNull final StorageNBTComponent component, @NotNull final C context);

   @NotNull
   protected abstract Component renderKeybind(@NotNull final KeybindComponent component, @NotNull final C context);

   @NotNull
   protected abstract Component renderScore(@NotNull final ScoreComponent component, @NotNull final C context);

   @NotNull
   protected abstract Component renderSelector(@NotNull final SelectorComponent component, @NotNull final C context);

   @NotNull
   protected abstract Component renderText(@NotNull final TextComponent component, @NotNull final C context);

   @NotNull
   protected Component renderVirtual(@NotNull final VirtualComponent component, @NotNull final C context) {
      return component;
   }

   @NotNull
   protected abstract Component renderTranslatable(@NotNull final TranslatableComponent component, @NotNull final C context);
}
