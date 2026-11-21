package org.evenmorefish.fish.libs.daisylib.messages.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

public class ComponentListMessage extends ComponentMessage<List<Component>, List<String>> {
   private final List<Component> message = new ArrayList<>();
   private final MessageType messageType;

   protected ComponentListMessage(@NonNull List<Component> message, @NonNull MessageType messageType) {
      message.forEach(component -> this.message.add(ROOT.append(component).compact()));
      this.messageType = messageType;
   }

   @NonNull
   public List<Component> get() {
      return new ArrayList<>(this.message);
   }

   @NonNull
   public List<String> getPlainText() {
      return this.message.stream().map(component -> PlainTextComponentSerializer.plainText().serialize(component)).toList();
   }

   @NonNull
   public List<String> getJson() {
      return this.message.stream().map(component -> (String)GsonComponentSerializer.gson().serialize(component)).toList();
   }

   @NonNull
   public List<String> getLegacy() {
      return this.message.stream().map(component -> LegacyComponentSerializer.legacySection().serialize(component)).toList();
   }

   @NonNull
   public List<String> getMiniMessage() {
      MiniMessage mm = MessageSettings.getMiniMessage();
      return this.message.stream().<String>map(mm::serialize).toList();
   }

   public ComponentListMessage createCopy() {
      return new ComponentListMessage(List.copyOf(this.message), this.messageType);
   }

   @NonNull
   @Override
   public MessageType messageType() {
      return this.messageType;
   }

   public ComponentListMessage messageType(@NonNull MessageType messageType) {
      return new ComponentListMessage(this.message, messageType);
   }

   public ComponentListMessage append(@NonNull Object append) {
      if (!MessageSettings.isAllowEmptyAppend() && this.isEmpty()) {
         DaisyLib.get().getLogging().debug("Cannot append to empty ComponentListMessage");
         return this;
      } else {
         List<Component> newMessage = new ArrayList<>(this.message);
         newMessage.addAll(ObjectProcessor.process(append).stream().map(ROOT::append).toList());
         return new ComponentListMessage(newMessage, this.messageType);
      }
   }

   public ComponentListMessage appendEachLine(@NonNull Object append) {
      if (!MessageSettings.isAllowEmptyAppend() && this.isEmpty()) {
         DaisyLib.get().getLogging().debug("Cannot append to empty ComponentListMessage");
         return this;
      } else {
         Component resolved = Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(append));
         List<Component> newMessage = this.message.stream().map(line -> new ComponentSingleMessage(line, this.messageType).append(resolved).get()).toList();
         return new ComponentListMessage(newMessage, this.messageType);
      }
   }

   public ComponentListMessage prepend(@NonNull Object prepend) {
      if (!MessageSettings.isAllowEmptyPrepend() && this.isEmpty()) {
         DaisyLib.get().getLogging().debug("Cannot prepend to empty ComponentListMessage");
         return this;
      } else {
         List<Component> newMessage = new ArrayList<>(ObjectProcessor.process(prepend).stream().map(ROOT::append).toList());
         newMessage.addAll(this.message);
         return new ComponentListMessage(newMessage, this.messageType);
      }
   }

   public ComponentListMessage prependEachLine(@NonNull Object prepend) {
      if (!MessageSettings.isAllowEmptyPrepend() && this.isEmpty()) {
         DaisyLib.get().getLogging().debug("Cannot prepend to empty ComponentListMessage");
         return this;
      } else {
         Component resolved = Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(prepend));
         List<Component> newMessage = this.message.stream().map(line -> new ComponentSingleMessage(line, this.messageType).prepend(resolved).get()).toList();
         return new ComponentListMessage(newMessage, this.messageType);
      }
   }

   public ComponentListMessage replace(@NonNull String placeholder, @Nullable Object replacement) {
      Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
      return new ComponentListMessage(replacer.apply(this.message), this.messageType);
   }

   public ComponentListMessage replaceWithListInsertion(@NonNull String placeholder, @Nullable Object replacement) {
      Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
      return new ComponentListMessage(replacer.applyWithListInsertion(this.message), this.messageType);
   }

   public ComponentListMessage replace(@NonNull Map<String, ?> replacements) {
      Replacer replacer = Replacer.replacer().addReplacements(replacements);
      return new ComponentListMessage(replacer.apply(this.message), this.messageType);
   }

   public ComponentListMessage replaceWithListInsertion(@NonNull Map<String, ?> replacements) {
      Replacer replacer = Replacer.replacer().addReplacements(replacements);
      return new ComponentListMessage(replacer.applyWithListInsertion(this.message), this.messageType);
   }

   public ComponentListMessage replace(@Nullable Replacer replacer) {
      return replacer == null ? this : new ComponentListMessage(replacer.apply(this.message), this.messageType);
   }

   public ComponentListMessage replaceWithListInsertion(@Nullable Replacer replacer) {
      return replacer == null ? this : new ComponentListMessage(replacer.applyWithListInsertion(this.message), this.messageType);
   }

   public ComponentListMessage parsePlaceholderAPI(@Nullable OfflinePlayer player) {
      return !MessageUtils.PAPI_AVAILABLE
         ? this
         : new ComponentListMessage(this.message.stream().map(component -> MessageUtils.parsePlaceholderAPI(component, player)).toList(), this.messageType);
   }

   @Override
   public boolean isEmpty() {
      return this.message.isEmpty();
   }

   @Override
   public int getLength() {
      return this.message.size();
   }

   public List<ComponentSingleMessage> toSingleMessages() {
      return this.message.stream().map(line -> new ComponentSingleMessage(line, this.messageType)).toList();
   }

   public ComponentListMessage edit(@NonNull Function<List<Component>, List<Component>> editor) {
      List<Component> newMessage = editor.apply(new ArrayList<>(this.message));
      return new ComponentListMessage(newMessage, this.messageType);
   }

   public ComponentListMessage editAllLines(@NonNull Function<ComponentSingleMessage, ComponentSingleMessage> editor) {
      List<Component> newMessage = this.message
         .stream()
         .map(line -> editor.apply(new ComponentSingleMessage(line, this.messageType)))
         .filter(Objects::nonNull)
         .map(ComponentSingleMessage::get)
         .toList();
      return new ComponentListMessage(newMessage, this.messageType);
   }

   @Override
   public void send(@Nullable Audience audience) {
      if (!this.isEmpty()) {
         this.message.forEach(component -> this.messageType.send(audience, component));
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
         this.message.forEach(Bukkit::broadcast);
      }
   }
}
