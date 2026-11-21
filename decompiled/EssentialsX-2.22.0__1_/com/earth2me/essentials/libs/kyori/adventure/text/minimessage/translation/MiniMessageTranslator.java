package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.translation;

import com.earth2me.essentials.libs.kyori.adventure.pointer.Pointered;
import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.TranslatableComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.TranslationArgument;
import com.earth2me.essentials.libs.kyori.adventure.text.TranslationArgumentLike;
import com.earth2me.essentials.libs.kyori.adventure.text.VirtualComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.VirtualComponentRenderer;
import com.earth2me.essentials.libs.kyori.adventure.text.format.Style;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.MiniMessage;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import com.earth2me.essentials.libs.kyori.adventure.translation.Translator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MiniMessageTranslator implements Translator {
   private final MiniMessage miniMessage;

   public MiniMessageTranslator() {
      this(MiniMessage.miniMessage());
   }

   public MiniMessageTranslator(@NotNull final MiniMessage miniMessage) {
      this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
   }

   @Nullable
   protected abstract String getMiniMessageString(@NotNull final String key, @NotNull final Locale locale);

   @Nullable
   @Override
   public final MessageFormat translate(@NotNull final String key, @NotNull final Locale locale) {
      return null;
   }

   @Nullable
   @Override
   public final Component translate(@NotNull final TranslatableComponent component, @NotNull final Locale locale) {
      String miniMessageString = this.getMiniMessageString(component.key(), locale);
      if (miniMessageString == null) {
         return null;
      } else {
         Pointered target = new LocalePointered(locale);
         List<TranslationArgument> translationArguments = component.arguments();
         Component resultingComponent;
         if (translationArguments.isEmpty()) {
            resultingComponent = this.miniMessage.deserialize(miniMessageString, target);
         } else {
            TagResolver.Builder tagResolverBuilder = TagResolver.builder();
            List<Tag> indexedArguments = new ArrayList<>(translationArguments.size());
            boolean targetAlreadyOverridden = false;

            for (TranslationArgument argument : translationArguments) {
               Object value = argument.value();
               if (value instanceof VirtualComponent) {
                  VirtualComponentRenderer<?> renderer = ((VirtualComponent)value).renderer();
                  if (renderer instanceof MiniMessageTranslatorTarget) {
                     if (targetAlreadyOverridden) {
                        throw new IllegalArgumentException("Multiple Argument.target() translation arguments have been set!");
                     }

                     target = ((MiniMessageTranslatorTarget)renderer).pointered();
                     targetAlreadyOverridden = true;
                     continue;
                  }

                  if (renderer instanceof MiniMessageTranslatorArgument) {
                     MiniMessageTranslatorArgument<?> translatorArgument = (MiniMessageTranslatorArgument<?>)renderer;
                     Object data = translatorArgument.data();
                     if (data instanceof TranslationArgumentLike) {
                        Tag tag = Tag.selfClosingInserting((TranslationArgumentLike)data);
                        tagResolverBuilder.tag(translatorArgument.name(), tag);
                        indexedArguments.add(tag);
                        continue;
                     }

                     if (data instanceof Tag) {
                        Tag tag = (Tag)data;
                        tagResolverBuilder.tag(translatorArgument.name(), tag);
                        indexedArguments.add(tag);
                        continue;
                     }

                     if (!(data instanceof TagResolver)) {
                        throw new IllegalArgumentException("Unknown translator argument type: " + data.getClass());
                     }

                     tagResolverBuilder.resolvers((TagResolver)data);
                  }
               }

               indexedArguments.add(Tag.selfClosingInserting(argument));
            }

            resultingComponent = this.miniMessage.deserialize(miniMessageString, target, new ArgumentTag(indexedArguments, tagResolverBuilder.build()));
         }

         Style style = component.style();
         if (!style.isEmpty()) {
            resultingComponent = resultingComponent.applyFallbackStyle(style);
         }

         return resultingComponent.append(component.children());
      }
   }
}
