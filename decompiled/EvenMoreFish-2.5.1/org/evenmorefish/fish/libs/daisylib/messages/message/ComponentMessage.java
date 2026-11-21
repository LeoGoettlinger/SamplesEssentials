package org.evenmorefish.fish.libs.daisylib.messages.message;

import java.util.List;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import org.bukkit.OfflinePlayer;
import org.evenmorefish.fish.libs.daisylib.messages.ObjectProcessor;
import org.evenmorefish.fish.libs.daisylib.messages.config.ConfigReader;
import org.evenmorefish.fish.libs.daisylib.messages.replacer.Replacer;
import org.evenmorefish.fish.libs.daisylib.utils.MessageUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class ComponentMessage<C, S> {
   public static final Component ROOT = Component.empty()
      .applyFallbackStyle(Style.style().color(NamedTextColor.WHITE).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE).build());

   protected ComponentMessage() {
   }

   @NonNull
   public static ComponentSingleMessage componentMessage(@NonNull Object object, @NonNull MessageType messageType) {
      if (object instanceof String string) {
         return componentMessage(MessageUtils.parseString(string), messageType);
      } else {
         return object instanceof Component component
            ? new ComponentSingleMessage(component, messageType)
            : componentMessage(Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(object)), messageType);
      }
   }

   @NonNull
   public static ComponentSingleMessage componentMessage(@NonNull Object object) {
      return componentMessage(object, MessageType.CHAT);
   }

   @NonNull
   public static ComponentListMessage componentMessage(@NonNull List<?> message, @NonNull MessageType messageType) {
      return new ComponentListMessage(message.stream().flatMap(object -> ObjectProcessor.process(object).stream()).toList(), messageType);
   }

   @NonNull
   public static ComponentListMessage componentMessage(@NonNull List<?> message) {
      return componentMessage(message, MessageType.CHAT);
   }

   @Nullable
   public static ComponentMessage<?, ?> componentMessage(@NonNull ConfigReader<?> loader, @NonNull String path) {
      return getFromConfig(loader, path);
   }

   @NonNull
   public static ComponentMessage<?, ?> componentMessage(@NonNull ConfigReader<?> loader, @NonNull String path, @NonNull String def) {
      ComponentMessage<?, ?> message = getFromConfig(loader, path);
      return (ComponentMessage<?, ?>)(message == null ? componentMessage(def) : message);
   }

   @NonNull
   public static ComponentMessage<?, ?> componentMessage(@NonNull ConfigReader<?> loader, @NonNull String path, @NonNull Component def) {
      ComponentMessage<?, ?> message = getFromConfig(loader, path);
      return (ComponentMessage<?, ?>)(message == null ? componentMessage(def) : message);
   }

   public ComponentSingleMessage toSingleMessage() {
      if (this instanceof ComponentSingleMessage singleMessage) {
         return singleMessage;
      } else if (this instanceof ComponentListMessage listMessage) {
         return new ComponentSingleMessage(Component.join(JoinConfiguration.newlines(), listMessage.get()), listMessage.messageType());
      } else {
         throw new IllegalArgumentException("Invalid ComponentMessage instance provided.");
      }
   }

   public ComponentListMessage toListMessage() {
      if (this instanceof ComponentSingleMessage singleMessage) {
         return new ComponentListMessage(List.of(singleMessage.get()), singleMessage.messageType());
      } else if (this instanceof ComponentListMessage listMessage) {
         return listMessage;
      } else {
         throw new IllegalArgumentException("Invalid ComponentMessage instance provided.");
      }
   }

   @NonNull
   public abstract C get();

   @NonNull
   public abstract S getPlainText();

   @NonNull
   public abstract S getJson();

   @NonNull
   public abstract S getLegacy();

   @NonNull
   public abstract S getMiniMessage();

   public abstract ComponentMessage<?, ?> createCopy();

   public abstract MessageType messageType();

   public abstract ComponentMessage<?, ?> messageType(@NonNull MessageType var1);

   public abstract ComponentMessage<?, ?> append(@NonNull Object var1);

   public abstract ComponentMessage<?, ?> prepend(@NonNull Object var1);

   public abstract ComponentMessage<?, ?> replace(@NonNull String var1, @Nullable Object var2);

   public abstract ComponentMessage<?, ?> replace(@NonNull Map<String, ?> var1);

   public abstract ComponentMessage<?, ?> replace(@Nullable Replacer var1);

   public abstract ComponentMessage<?, ?> parsePlaceholderAPI(@Nullable OfflinePlayer var1);

   public abstract boolean isEmpty();

   public abstract int getLength();

   public abstract void send(@Nullable Audience var1);

   public abstract void send(@NonNull List<? extends Audience> var1);

   public abstract void send(Audience... var1);

   public abstract void broadcast();

   @Nullable
   private static ComponentMessage<?, ?> getFromConfig(@NonNull ConfigReader<?> loader, @NonNull String path) {
      ConfigReader<?> section = loader.getSection(path);
      if (section == null) {
         return fromObject(loader.getObject(path));
      } else {
         String messageType = section.getString("type");
         MessageType type = MessageType.getFromString(messageType);
         ComponentMessage<?, ?> finalMessage = fromObject(section.getObject("message"));
         return finalMessage != null ? finalMessage.messageType(type) : null;
      }
   }

   @Nullable
   private static ComponentMessage<?, ?> fromObject(@Nullable Object object) {
      if (object == null) {
         return null;
      } else {
         return (ComponentMessage<?, ?>)(object instanceof List<?> list ? componentMessage(list) : componentMessage(object.toString()));
      }
   }
}
