package com.sk89q.worldedit.util.formatting.text.renderer;

import com.sk89q.worldedit.util.formatting.text.BlockNbtComponent;
import com.sk89q.worldedit.util.formatting.text.BuildableComponent;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.ComponentBuilder;
import com.sk89q.worldedit.util.formatting.text.EntityNbtComponent;
import com.sk89q.worldedit.util.formatting.text.KeybindComponent;
import com.sk89q.worldedit.util.formatting.text.NbtComponent;
import com.sk89q.worldedit.util.formatting.text.NbtComponentBuilder;
import com.sk89q.worldedit.util.formatting.text.ScoreComponent;
import com.sk89q.worldedit.util.formatting.text.SelectorComponent;
import com.sk89q.worldedit.util.formatting.text.StorageNbtComponent;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.text.AttributedCharacterIterator;
import java.text.MessageFormat;
import java.text.MessageFormat.Field;
import java.util.List;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class TranslatableComponentRenderer<C> extends AbstractComponentRenderer<C> {
   @NonNull
   public static <C> TranslatableComponentRenderer<C> from(@NonNull final BiFunction<C, String, MessageFormat> translations) {
      return new TranslatableComponentRenderer<C>() {
         @Nullable
         @Override
         protected MessageFormat translation(@NonNull final C context, @NonNull final String key) {
            return translations.apply(context, key);
         }
      };
   }

   @NonNull
   @Override
   protected Component renderBlockNbt(@NonNull final BlockNbtComponent component, @NonNull final C context) {
      BlockNbtComponent.Builder builder = ((BlockNbtComponent.Builder)nbt(BlockNbtComponent.builder(), (C)component)).pos(component.pos());
      return this.deepRender(component, builder, context);
   }

   @NonNull
   @Override
   protected Component renderEntityNbt(@NonNull final EntityNbtComponent component, @NonNull final C context) {
      EntityNbtComponent.Builder builder = ((EntityNbtComponent.Builder)nbt(EntityNbtComponent.builder(), (C)component)).selector(component.selector());
      return this.deepRender(component, builder, context);
   }

   @NonNull
   @Override
   protected Component renderStorageNbt(@NonNull final StorageNbtComponent component, @NonNull final C context) {
      StorageNbtComponent.Builder builder = ((StorageNbtComponent.Builder)nbt(StorageNbtComponent.builder(), (C)component)).storage(component.storage());
      return this.deepRender(component, builder, context);
   }

   private static <C extends NbtComponent<C, B>, B extends NbtComponentBuilder<C, B>> B nbt(final B builder, final C oldComponent) {
      return builder.nbtPath(oldComponent.nbtPath()).interpret(oldComponent.interpret());
   }

   @NonNull
   @Override
   protected Component renderKeybind(@NonNull final KeybindComponent component, @NonNull final C context) {
      KeybindComponent.Builder builder = KeybindComponent.builder(component.keybind());
      return this.deepRender(component, builder, context);
   }

   @NonNull
   @Override
   protected Component renderScore(@NonNull final ScoreComponent component, @NonNull final C context) {
      ScoreComponent.Builder builder = ScoreComponent.builder().name(component.name()).objective(component.objective()).value(component.value());
      return this.deepRender(component, builder, context);
   }

   @NonNull
   @Override
   protected Component renderSelector(@NonNull final SelectorComponent component, @NonNull final C context) {
      SelectorComponent.Builder builder = SelectorComponent.builder(component.pattern());
      return this.deepRender(component, builder, context);
   }

   @NonNull
   @Override
   protected Component renderText(@NonNull final TextComponent component, @NonNull final C context) {
      TextComponent.Builder builder = TextComponent.builder(component.content());
      return this.deepRender(component, builder, context);
   }

   @NonNull
   @Override
   protected Component renderTranslatable(@NonNull final TranslatableComponent component, @NonNull final C context) {
      MessageFormat format = this.translation(context, component.key());
      if (format == null) {
         return component;
      } else {
         List<Component> args = component.args();
         TextComponent.Builder builder = TextComponent.builder();
         this.mergeStyle(component, builder, context);
         if (args.isEmpty()) {
            return builder.content(format.format(null, new StringBuffer(), null).toString()).build();
         } else {
            Object[] nulls = new Object[args.size()];
            StringBuffer sb = format.format(nulls, new StringBuffer(), null);
            AttributedCharacterIterator it = format.formatToCharacterIterator(nulls);

            while (it.getIndex() < it.getEndIndex()) {
               int end = it.getRunLimit();
               Integer index = (Integer)it.getAttribute(Field.ARGUMENT);
               if (index != null) {
                  builder.append(this.render(args.get(index), context));
               } else {
                  builder.append(TextComponent.of(sb.substring(it.getIndex(), end)));
               }

               it.setIndex(end);
            }

            return this.deepRender(component, builder, context);
         }
      }
   }

   private <O extends BuildableComponent<O, B>, B extends ComponentBuilder<O, B>> O deepRender(final Component component, final B builder, final C context) {
      this.mergeStyle(component, builder, context);
      component.children().forEach(child -> builder.append(this.render(child, context)));
      return builder.build();
   }

   private <B extends ComponentBuilder<?, ?>> void mergeStyle(final Component component, final B builder, final C context) {
      builder.mergeStyle(component, Style.Merge.colorAndDecorations());
      builder.clickEvent(component.clickEvent());
      HoverEvent hoverEvent = component.hoverEvent();
      if (hoverEvent != null) {
         builder.hoverEvent(HoverEvent.of(hoverEvent.action(), this.render(hoverEvent.value(), context)));
      }
   }

   @Nullable
   protected abstract MessageFormat translation(@NonNull final C context, @NonNull final String key);
}
