package org.evenmorefish.fish.libs.daisylib.messages.message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.daisylib.messages.MessageSettings;
import org.evenmorefish.fish.libs.daisylib.messages.ObjectProcessor;
import org.evenmorefish.fish.libs.daisylib.messages.replacer.Replacer;
import org.evenmorefish.fish.libs.daisylib.utils.MessageUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ComponentSingleMessage extends ComponentMessage<Component, String> {
   private final Component message;
   private final MessageType messageType;

   protected ComponentSingleMessage(@NonNull Component message, @NonNull MessageType messageType) {
      this.message = ComponentMessage.ROOT.append(message).compact();
      this.messageType = messageType;
   }

   @NonNull
   public Component get() {
      return this.message;
   }

   @NonNull
   public String getPlainText() {
      return PlainTextComponentSerializer.plainText().serialize(this.message);
   }

   @NonNull
   public String getJson() {
      return (String)GsonComponentSerializer.gson().serialize(this.message);
   }

   @NonNull
   public String getLegacy() {
      return LegacyComponentSerializer.legacySection().serialize(this.message);
   }

   @NonNull
   public String getMiniMessage() {
      return (String)MessageSettings.getMiniMessage().serialize(this.message);
   }

   public ComponentSingleMessage createCopy() {
      return new ComponentSingleMessage(this.message, this.messageType);
   }

   @NonNull
   @Override
   public MessageType messageType() {
      return this.messageType;
   }

   public ComponentSingleMessage messageType(@NonNull MessageType messageType) {
      return new ComponentSingleMessage(this.message, messageType);
   }

   public ComponentSingleMessage append(@NonNull Object append) {
      if (!MessageSettings.isAllowEmptyAppend() && this.isEmpty()) {
         DaisyLib.get().getLogging().debug("Cannot append to empty ComponentSingleMessage");
         return this;
      } else {
         return new ComponentSingleMessage(this.message.append(Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(append))), this.messageType);
      }
   }

   public ComponentSingleMessage prepend(@NonNull Object prepend) {
      if (!MessageSettings.isAllowEmptyPrepend() && this.isEmpty()) {
         DaisyLib.get().getLogging().debug("Cannot prepend to empty ComponentSingleMessage");
         return this;
      } else {
         Component processed = Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(prepend));
         return MessageUtils.isEmpty(processed) ? this : new ComponentSingleMessage(processed, this.messageType).append(this.message);
      }
   }

   public ComponentSingleMessage replace(@NonNull String placeholder, @Nullable Object replacement) {
      Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
      return new ComponentSingleMessage(replacer.apply(this.message), this.messageType);
   }

   public ComponentSingleMessage replace(@NonNull Map<String, ?> replacements) {
      Replacer replacer = Replacer.replacer().addReplacements(replacements);
      return new ComponentSingleMessage(replacer.apply(this.message), this.messageType);
   }

   public ComponentSingleMessage replace(@Nullable Replacer replacer) {
      return replacer == null ? this : new ComponentSingleMessage(replacer.apply(this.message), this.messageType);
   }

   public ComponentSingleMessage parsePlaceholderAPI(@Nullable OfflinePlayer player) {
      return !MessageUtils.PAPI_AVAILABLE ? this : new ComponentSingleMessage(MessageUtils.parsePlaceholderAPI(this.message, player), this.messageType);
   }

   public boolean matches(@NonNull String string) {
      return this.getPlainText().equals(string);
   }

   public boolean contains(@NonNull String... string) {
      String plainText = this.getPlainText();
      return Arrays.stream(string).allMatch(plainText::contains);
   }

   @Override
   public boolean isEmpty() {
      return this.getPlainText().isEmpty();
   }

   @Override
   public int getLength() {
      return this.getPlainText().length();
   }

   public ComponentSingleMessage edit(@NonNull Function<Component, Component> editor) {
      return new ComponentSingleMessage(editor.apply(this.message), this.messageType);
   }

   @Override
   public void send(@Nullable Audience audience) {
      if (!this.isEmpty()) {
         this.messageType.send(audience, this.message);
      }
   }

   @Override
   public void send(@NonNull List<? extends Audience> audienceList) {
      if (!this.isEmpty()) {
         audienceList.forEach(this::send);
      }
   }

   @Override
   public void send(Audience... audiences) {
      if (!this.isEmpty()) {
         for (Audience audience : audiences) {
            this.send(audience);
         }
      }
   }

   @Override
   public void broadcast() {
      if (!this.isEmpty()) {
         Bukkit.broadcast(this.message);
      }
   }
}
