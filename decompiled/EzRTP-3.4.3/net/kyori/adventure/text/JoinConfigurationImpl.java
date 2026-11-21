package net.kyori.adventure.text;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

record JoinConfigurationImpl(
   Component prefix,
   Component suffix,
   Component separator,
   Component lastSeparator,
   Component lastSeparatorIfSerial,
   Function<ComponentLike, Component> convertor,
   Predicate<ComponentLike> predicate,
   Style parentStyle
) implements JoinConfiguration {
   static final Function<ComponentLike, Component> DEFAULT_CONVERTOR = ComponentLike::asComponent;
   static final Predicate<ComponentLike> DEFAULT_PREDICATE = componentLike -> true;
   static final JoinConfigurationImpl NULL = new JoinConfigurationImpl(null, null, null, null, null, DEFAULT_CONVERTOR, DEFAULT_PREDICATE, Style.empty());
   static final JoinConfiguration STANDARD_NEW_LINES = JoinConfiguration.separator(Component.newline());
   static final JoinConfiguration STANDARD_SPACES = JoinConfiguration.separator(Component.space());
   static final JoinConfiguration STANDARD_COMMA_SEPARATED = JoinConfiguration.separator(Component.text(","));
   static final JoinConfiguration STANDARD_COMMA_SPACE_SEPARATED = JoinConfiguration.separator(Component.text(", "));
   static final JoinConfiguration STANDARD_ARRAY_LIKE = JoinConfiguration.builder()
      .separator(Component.text(", "))
      .prefix(Component.text("["))
      .suffix(Component.text("]"))
      .build();

   private JoinConfigurationImpl(final JoinConfigurationImpl.BuilderImpl builder) {
      this(
         ComponentLike.unbox(builder.prefix),
         ComponentLike.unbox(builder.suffix),
         ComponentLike.unbox(builder.separator),
         ComponentLike.unbox(builder.lastSeparator),
         ComponentLike.unbox(builder.lastSeparatorIfSerial),
         builder.convertor,
         builder.predicate,
         builder.parentStyle
      );
   }

   @Contract(
      pure = true
   )
   static Component join(final JoinConfiguration config, final Iterable<? extends ComponentLike> components) {
      Objects.requireNonNull(config, "config");
      Objects.requireNonNull(components, "components");
      Iterator<? extends ComponentLike> it = components.iterator();
      if (!it.hasNext()) {
         return singleElementJoin(config, null);
      } else {
         ComponentLike component = Objects.requireNonNull(it.next(), "Null elements in \"components\" are not allowed");
         int componentsSeen = 0;
         if (!it.hasNext()) {
            return singleElementJoin(config, component);
         } else {
            Component prefix = config.prefix();
            Component suffix = config.suffix();
            Function<ComponentLike, Component> convertor = config.convertor();
            Predicate<ComponentLike> predicate = config.predicate();
            Style rootStyle = config.parentStyle();
            boolean hasRootStyle = rootStyle != Style.empty();
            Component separator = config.separator();
            boolean hasSeparator = separator != null;
            TextComponent.Builder builder = hasRootStyle ? Component.text().style(rootStyle) : Component.text();
            if (prefix != null) {
               builder.append(prefix);
            }

            while (component != null) {
               if (!predicate.test(component)) {
                  if (!it.hasNext()) {
                     break;
                  }

                  component = it.next();
               } else {
                  builder.append(Objects.requireNonNull(convertor.apply(component), "Null output from \"convertor\" is not allowed"));
                  componentsSeen++;
                  if (!it.hasNext()) {
                     component = null;
                  } else {
                     component = Objects.requireNonNull(it.next(), "Null elements in \"components\" are not allowed");
                     if (it.hasNext()) {
                        if (hasSeparator) {
                           builder.append(separator);
                        }
                     } else {
                        Component lastSeparator = null;
                        if (componentsSeen > 1) {
                           lastSeparator = config.lastSeparatorIfSerial();
                        }

                        if (lastSeparator == null) {
                           lastSeparator = config.lastSeparator();
                        }

                        if (lastSeparator == null) {
                           lastSeparator = config.separator();
                        }

                        if (lastSeparator != null) {
                           builder.append(lastSeparator);
                        }
                     }
                  }
               }
            }

            if (suffix != null) {
               builder.append(suffix);
            }

            return builder.build();
         }
      }
   }

   static Component singleElementJoin(final JoinConfiguration config, @Nullable final ComponentLike component) {
      Component prefix = config.prefix();
      Component suffix = config.suffix();
      Function<ComponentLike, Component> convertor = config.convertor();
      Predicate<ComponentLike> predicate = config.predicate();
      Style rootStyle = config.parentStyle();
      boolean hasRootStyle = rootStyle != Style.empty();
      if (prefix == null && suffix == null) {
         Component result;
         if (component != null && predicate.test(component)) {
            result = convertor.apply(component);
         } else {
            result = Component.empty();
         }

         return (Component)(hasRootStyle ? Component.text().style(rootStyle).append(result).build() : result);
      } else {
         TextComponent.Builder builder = Component.text();
         if (prefix != null) {
            builder.append(prefix);
         }

         if (component != null && predicate.test(component)) {
            builder.append(convertor.apply(component));
         }

         if (suffix != null) {
            builder.append(suffix);
         }

         return hasRootStyle ? Component.text().style(rootStyle).append(builder).build() : builder.build();
      }
   }

   static final class BuilderImpl implements JoinConfiguration.Builder {
      private ComponentLike prefix;
      private ComponentLike suffix;
      private ComponentLike separator;
      private ComponentLike lastSeparator;
      private ComponentLike lastSeparatorIfSerial;
      private Function<ComponentLike, Component> convertor;
      private Predicate<ComponentLike> predicate;
      private Style parentStyle;

      BuilderImpl() {
         this(JoinConfigurationImpl.NULL);
      }

      private BuilderImpl(final JoinConfigurationImpl joinConfig) {
         this.separator = joinConfig.separator;
         this.lastSeparator = joinConfig.lastSeparator;
         this.prefix = joinConfig.prefix;
         this.suffix = joinConfig.suffix;
         this.convertor = joinConfig.convertor;
         this.lastSeparatorIfSerial = joinConfig.lastSeparatorIfSerial;
         this.predicate = joinConfig.predicate;
         this.parentStyle = joinConfig.parentStyle;
      }

      @Override
      public JoinConfiguration.Builder prefix(@Nullable final ComponentLike prefix) {
         this.prefix = prefix;
         return this;
      }

      @Override
      public JoinConfiguration.Builder suffix(@Nullable final ComponentLike suffix) {
         this.suffix = suffix;
         return this;
      }

      @Override
      public JoinConfiguration.Builder separator(@Nullable final ComponentLike separator) {
         this.separator = separator;
         return this;
      }

      @Override
      public JoinConfiguration.Builder lastSeparator(@Nullable final ComponentLike lastSeparator) {
         this.lastSeparator = lastSeparator;
         return this;
      }

      @Override
      public JoinConfiguration.Builder lastSeparatorIfSerial(@Nullable final ComponentLike lastSeparatorIfSerial) {
         this.lastSeparatorIfSerial = lastSeparatorIfSerial;
         return this;
      }

      @Override
      public JoinConfiguration.Builder convertor(final Function<ComponentLike, Component> convertor) {
         this.convertor = Objects.requireNonNull(convertor, "convertor");
         return this;
      }

      @Override
      public JoinConfiguration.Builder predicate(final Predicate<ComponentLike> predicate) {
         this.predicate = Objects.requireNonNull(predicate, "predicate");
         return this;
      }

      @Override
      public JoinConfiguration.Builder parentStyle(final Style parentStyle) {
         this.parentStyle = Objects.requireNonNull(parentStyle, "parentStyle");
         return this;
      }

      public JoinConfiguration build() {
         return new JoinConfigurationImpl(this);
      }
   }
}
