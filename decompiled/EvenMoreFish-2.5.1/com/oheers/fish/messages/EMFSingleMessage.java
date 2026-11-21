package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentListMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentSingleMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EMFSingleMessage extends EMFMessage {
   private ComponentSingleMessage underlying;

   protected EMFSingleMessage(@NonNull ComponentSingleMessage message) {
      this.underlying = message;
   }

   public EMFSingleMessage createCopy() {
      EMFSingleMessage newMessage = new EMFSingleMessage(this.underlying.createCopy());
      newMessage.perPlayer = this.perPlayer;
      return newMessage;
   }

   @NonNull
   public ComponentSingleMessage getUnderlying() {
      return this.underlying;
   }

   @Override
   public void setUnderlying(@NonNull ComponentMessage<?, ?> message) {
      if (message instanceof ComponentListMessage listMessage) {
         this.underlying = listMessage.toSingleMessage();
      } else {
         if (!(message instanceof ComponentSingleMessage singleMessage)) {
            return;
         }

         this.underlying = singleMessage;
      }
   }

   public ComponentSingleMessage processPlaceholders(@Nullable OfflinePlayer player) {
      ComponentSingleMessage message = this.underlying;
      OfflinePlayer relevant = Optional.ofNullable(player).orElse(this.relevantPlayer);
      if (relevant != null) {
         String name = FishUtils.getPlayerNameOrDefault(relevant, "N/A");
         message = message.replace("{player}", name);
      }

      return message.parsePlaceholderAPI(relevant);
   }

   public static EMFSingleMessage empty() {
      return new EMFSingleMessage(ComponentMessage.componentMessage(Component.empty()));
   }

   public static EMFSingleMessage ofUnderlying(@NonNull ComponentSingleMessage underlying) {
      return new EMFSingleMessage(underlying);
   }

   public static EMFSingleMessage of(@NonNull Component component) {
      return new EMFSingleMessage(ComponentMessage.componentMessage(component));
   }

   public static EMFSingleMessage ofList(@NonNull List<Component> components) {
      return new EMFSingleMessage(ComponentMessage.componentMessage(components).toSingleMessage());
   }

   public static EMFSingleMessage fromString(@NonNull String string) {
      return new EMFSingleMessage(ComponentMessage.componentMessage(string));
   }

   public static EMFSingleMessage fromStringList(@NonNull List<String> strings) {
      return new EMFSingleMessage(ComponentMessage.componentMessage(strings).toSingleMessage());
   }

   @NonNull
   public Component getRawMessage() {
      return this.underlying.get();
   }

   @NonNull
   @Override
   public Component getComponentMessage(@Nullable OfflinePlayer player) {
      return this.processPlaceholders(player).get();
   }

   @NonNull
   @Override
   public List<Component> getComponentListMessage(@Nullable OfflinePlayer player) {
      return List.of(this.getComponentMessage(player));
   }

   @NonNull
   @Override
   public String getLegacyMessage(@Nullable OfflinePlayer player) {
      return this.processPlaceholders(player).getLegacy();
   }

   @NonNull
   @Override
   public List<String> getLegacyListMessage(@Nullable OfflinePlayer player) {
      return List.of(this.getLegacyMessage(player));
   }

   @NonNull
   @Override
   public String getPlainTextMessage(@Nullable OfflinePlayer player) {
      return this.processPlaceholders(player).getPlainText();
   }

   @NonNull
   @Override
   public List<String> getPlainTextListMessage(@Nullable OfflinePlayer player) {
      return List.of(this.getPlainTextMessage(player));
   }

   @Override
   public void formatPlaceholderAPI() {
      this.underlying = this.underlying.parsePlaceholderAPI(this.relevantPlayer);
   }

   public void setMessage(@NonNull String message) {
      this.underlying = ComponentMessage.componentMessage(message).messageType(this.underlying.messageType());
   }

   public void setMessage(@NonNull Component message) {
      this.underlying = ComponentMessage.componentMessage(message).messageType(this.underlying.messageType());
   }

   public void setMessage(@NonNull EMFSingleMessage message) {
      this.underlying = message.underlying;
   }

   public void trim() {
      Component newComponent = MiniMessage.miniMessage().deserialize(((String)MiniMessage.miniMessage().serialize(this.underlying.get())).stripTrailing());
      this.underlying = ComponentMessage.componentMessage(newComponent, this.underlying.messageType());
   }

   @Override
   public boolean containsString(@NonNull String string) {
      return this.underlying.contains(string);
   }
}
