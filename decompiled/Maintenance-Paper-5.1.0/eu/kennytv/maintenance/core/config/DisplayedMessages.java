package eu.kennytv.maintenance.core.config;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DisplayedMessages {
   private static final Random RANDOM = new Random();
   private final Map<String, List<String>> messages;
   private final List<String> defaultMessages;

   public DisplayedMessages(Map<String, List<String>> messages) {
      this.messages = messages;
      this.defaultMessages = messages.get("default");
   }

   public List<String> getMessages(@Nullable String key) {
      Preconditions.checkNotNull(this.defaultMessages, "no default message configured in messages list");
      return key != null && this.messages.size() != 1 ? this.messages.getOrDefault(key, this.defaultMessages) : this.defaultMessages;
   }

   public String getRandomEntry(@Nullable String key) {
      List<String> messages = this.getMessages(key);
      return messages.get(RANDOM.nextInt(messages.size()));
   }

   public List<String> getDefaultMessages() {
      return this.defaultMessages;
   }

   public boolean isEmpty() {
      return this.messages.isEmpty();
   }

   public Set<String> getKeys() {
      return this.messages.keySet();
   }
}
