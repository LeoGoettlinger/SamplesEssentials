package com.oheers.fish.messages;

import com.oheers.fish.FishUtils;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentListMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentSingleMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EMFListMessage extends EMFMessage {
   private ComponentListMessage underlying;

   private EMFListMessage(@NonNull ComponentListMessage message) {
      this.underlying = message;
   }

   public EMFListMessage createCopy() {
      EMFListMessage newMessage = new EMFListMessage(this.underlying.createCopy());
      newMessage.perPlayer = this.perPlayer;
      return newMessage;
   }

   @NonNull
   public ComponentListMessage getUnderlying() {
      return this.underlying;
   }

   @Override
   public void setUnderlying(@NonNull ComponentMessage<?, ?> message) {
      if (message instanceof ComponentSingleMessage singleMessage) {
         this.underlying = singleMessage.toListMessage();
      } else {
         if (!(message instanceof ComponentListMessage listMessage)) {
            return;
         }

         this.underlying = listMessage;
      }
   }

   public ComponentListMessage processPlaceholders(@Nullable OfflinePlayer player) {
      ComponentListMessage message = this.underlying;
      OfflinePlayer relevant = Optional.ofNullable(player).orElse(this.relevantPlayer);
      if (relevant != null) {
         String name = FishUtils.getPlayerNameOrDefault(relevant, "N/A");
         message = message.replace("{player}", name);
      }

      return message.parsePlaceholderAPI(relevant);
   }

   public static EMFListMessage empty() {
      return new EMFListMessage(ComponentMessage.componentMessage(List.of()));
   }

   public static EMFListMessage ofUnderlying(@NonNull ComponentListMessage underlying) {
      return new EMFListMessage(underlying);
   }

   public static EMFListMessage of(@NonNull Component component) {
      return new EMFListMessage(ComponentMessage.componentMessage(List.of(component)));
   }

   public static EMFListMessage ofList(@NonNull List<Component> components) {
      return new EMFListMessage(ComponentMessage.componentMessage(components));
   }

   public static EMFListMessage fromString(@NonNull String string) {
      return new EMFListMessage(ComponentMessage.componentMessage(List.of(string)));
   }

   public static EMFListMessage fromStringList(@NonNull List<String> strings) {
      return new EMFListMessage(ComponentMessage.componentMessage(strings));
   }

   @NonNull
   public List<Component> getRawMessage() {
      return this.underlying.get();
   }

   @NonNull
   @Override
   public Component getComponentMessage(@Nullable OfflinePlayer player) {
      return Component.join(JoinConfiguration.newlines(), this.getComponentListMessage(player));
   }

   @NonNull
   @Override
   public List<Component> getComponentListMessage(@Nullable OfflinePlayer player) {
      return this.processPlaceholders(player).get();
   }

   @NonNull
   @Override
   public String getLegacyMessage(@Nullable OfflinePlayer player) {
      return String.join("\n", this.getLegacyListMessage(player));
   }

   @NonNull
   @Override
   public List<String> getLegacyListMessage(@Nullable OfflinePlayer player) {
      return this.processPlaceholders(player).getLegacy();
   }

   @NonNull
   @Override
   public String getPlainTextMessage(@Nullable OfflinePlayer player) {
      return String.join("\n", this.getPlainTextListMessage(player));
   }

   @NonNull
   @Override
   public List<String> getPlainTextListMessage(@Nullable OfflinePlayer player) {
      return this.processPlaceholders(player).getPlainText();
   }

   @Override
   public void formatPlaceholderAPI() {
      this.underlying = this.underlying.parsePlaceholderAPI(this.relevantPlayer);
   }

   @Override
   public boolean containsString(@NonNull String string) {
      return this.underlying.getPlainText().stream().anyMatch(line -> line.contains(string));
   }

   public void setVariableWithListInsertion(@NonNull String variable, @NonNull Object replacement) {
      this.underlying = this.underlying.replaceWithListInsertion(variable, replacement);
   }

   public void setVariablesWithListInsertion(@Nullable Map<String, ?> variableMap) {
      if (variableMap != null && !variableMap.isEmpty()) {
         this.underlying = this.underlying.replaceWithListInsertion(variableMap);
      }
   }

   public void appendEachLine(@NonNull Object object) {
      this.setUnderlying(this.getUnderlying().appendEachLine(object));
   }

   public void prependEachLine(@NonNull Object object) {
      this.setUnderlying(this.getUnderlying().prependEachLine(object));
   }
}
