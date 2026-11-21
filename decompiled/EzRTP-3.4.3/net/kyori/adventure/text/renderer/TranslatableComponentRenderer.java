package net.kyori.adventure.text.renderer;

import java.text.AttributedCharacterIterator;
import java.text.MessageFormat;
import java.text.MessageFormat.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.NBTComponentBuilder;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.Nullable;

public abstract class TranslatableComponentRenderer<C> extends AbstractComponentRenderer<C> {
   private static final Set<Style.Merge> MERGES;

   public static TranslatableComponentRenderer<Locale> usingTranslationSource(final Translator source) {
      Objects.requireNonNull(source, "source");
      return new TranslatableComponentRenderer<Locale>() {
         @Nullable
         protected MessageFormat translate(final String key, final Locale context) {
            return source.translate(key, context);
         }

         protected Component renderTranslatableInner(final TranslatableComponent component, final Locale context) {
            TriState anyTranslations = source.hasAnyTranslations();
            if (anyTranslations == TriState.FALSE) {
               return component;
            } else {
               Component translated;
               if (source.canTranslate(component.key(), context)) {
                  translated = source.translate(component, context);
               } else {
                  translated = null;
               }

               return translated != null ? this.render(translated, context) : super.renderTranslatableInner(component, context);
            }
         }
      };
   }

   @Nullable
   protected MessageFormat translate(final String key, final C context) {
      return null;
   }

   @Nullable
   protected MessageFormat translate(final String key, @Nullable final String fallback, final C context) {
      return this.translate(key, context);
   }

   @Override
   protected Component renderBlockNbt(final BlockNBTComponent component, final C context) {
      BlockNBTComponent.Builder builder = this.nbt(context, Component.blockNBT(), component).pos(component.pos());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderEntityNbt(final EntityNBTComponent component, final C context) {
      EntityNBTComponent.Builder builder = this.nbt(context, Component.entityNBT(), component).selector(component.selector());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderStorageNbt(final StorageNBTComponent component, final C context) {
      StorageNBTComponent.Builder builder = this.nbt(context, Component.storageNBT(), component).storage(component.storage());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   protected <O extends NBTComponent<O>, B extends NBTComponentBuilder<O, B>> B nbt(final C context, final B builder, final O oldComponent) {
      builder.nbtPath(oldComponent.nbtPath()).interpret(oldComponent.interpret());
      Component separator = oldComponent.separator();
      if (separator != null) {
         builder.separator(this.render(separator, context));
      }

      return builder;
   }

   @Override
   protected Component renderKeybind(final KeybindComponent component, final C context) {
      KeybindComponent.Builder builder = Component.keybind().keybind(component.keybind());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderScore(final ScoreComponent component, final C context) {
      ScoreComponent.Builder builder = Component.score().name(component.name()).objective(component.objective());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderSelector(final SelectorComponent component, final C context) {
      SelectorComponent.Builder builder = Component.selector().pattern(component.pattern());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderText(final TextComponent component, final C context) {
      TextComponent.Builder builder = Component.text().content(component.content());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderObject(final ObjectComponent component, final C context) {
      ObjectComponent.Builder builder = Component.object().contents(component.contents());
      return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
   }

   @Override
   protected Component renderTranslatable(TranslatableComponent component, final C context) {
      List<TranslationArgument> arguments = component.arguments();
      List<Component> children = component.children();
      if (!arguments.isEmpty() || !children.isEmpty()) {
         TranslatableComponent.Builder builder = component.toBuilder();
         if (!arguments.isEmpty()) {
            List<TranslationArgument> translatedArguments = new ArrayList<>(arguments);

            for (int i = 0; i < translatedArguments.size(); i++) {
               TranslationArgument arg = translatedArguments.get(i);
               if (arg.value() instanceof Component && !(arg.value() instanceof VirtualComponent)) {
                  translatedArguments.set(i, TranslationArgument.component(this.render((Component)arg.value(), context)));
               }
            }

            builder.arguments(translatedArguments);
         }

         component = builder.build();
      }

      return this.renderTranslatableInner(component, context);
   }

   protected Component renderTranslatableInner(final TranslatableComponent component, final C context) {
      MessageFormat format = this.translate(component.key(), component.fallback(), context);
      if (format == null) {
         return this.optionallyRenderChildrenAndStyle(component, context);
      } else {
         List<TranslationArgument> args = component.arguments();
         TextComponent.Builder builder = Component.text();
         this.mergeStyle(component, builder, context);
         if (args.isEmpty()) {
            builder.content(format.format(null, new StringBuffer(), null).toString());
            return this.optionallyRenderChildrenAppendAndBuild(component.children(), builder, context);
         } else {
            Object[] nulls = new Object[args.size()];
            StringBuffer sb = format.format(nulls, new StringBuffer(), null);
            AttributedCharacterIterator it = format.formatToCharacterIterator(nulls);

            while (it.getIndex() < it.getEndIndex()) {
               int end = it.getRunLimit();
               Integer index = (Integer)it.getAttribute(Field.ARGUMENT);
               if (index != null) {
                  TranslationArgument arg = args.get(index);
                  builder.append(arg.asComponent());
               } else {
                  builder.append(Component.text(sb.substring(it.getIndex(), end)));
               }

               it.setIndex(end);
            }

            return this.optionallyRenderChildrenAppendAndBuild(component.children(), builder, context);
         }
      }
   }

   protected Component optionallyRenderChildrenAndStyle(Component component, final C context) {
      HoverEvent<?> hoverEvent = component.hoverEvent();
      if (hoverEvent != null) {
         component = component.hoverEvent(hoverEvent.withRenderedValue(this, context));
      }

      List<Component> children = component.children();
      if (children.isEmpty()) {
         return component;
      } else {
         List<Component> rendered = new ArrayList<>(children.size());
         children.forEach(child -> rendered.add(this.render(child, context)));
         return component.children(rendered);
      }
   }

   protected <O extends Component, B extends ComponentBuilder<O, B>> O mergeStyleAndOptionallyDeepRender(
      final Component component, final B builder, final C context
   ) {
      this.mergeStyle(component, builder, context);
      return this.optionallyRenderChildrenAppendAndBuild(component.children(), builder, context);
   }

   protected <O extends Component, B extends ComponentBuilder<O, B>> O optionallyRenderChildrenAppendAndBuild(
      final List<Component> children, final B builder, final C context
   ) {
      if (!children.isEmpty()) {
         children.forEach(child -> builder.append(this.render(child, context)));
      }

      return builder.build();
   }

   protected <B extends ComponentBuilder<?, ?>> void mergeStyle(final Component component, final B builder, final C context) {
      builder.mergeStyle(component, MERGES);
      builder.clickEvent(component.clickEvent());
      HoverEvent<?> hoverEvent = component.hoverEvent();
      if (hoverEvent != null) {
         builder.hoverEvent(hoverEvent.withRenderedValue(this, context));
      }
   }

   static {
      Set<Style.Merge> merges = EnumSet.allOf(Style.Merge.class);
      merges.remove(Style.Merge.EVENTS);
      MERGES = Set.copyOf(merges);
   }
}
