package de.oliver.fancylib.translations.message;

import de.oliver.fancylib.translations.TextConfig;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MultiMessage extends Message {
   private final List<String> messages;

   public MultiMessage(TextConfig config, List<String> messages) {
      super(config);
      this.messages = new ArrayList<>(messages);
      this.applyColorPlaceholders();
   }

   @Override
   public Message replace(String placeholder, String replacement) {
      this.messages.replaceAll(s -> s.replace("{" + placeholder + "}", replacement).replace("%" + placeholder + "%", replacement));
      return this;
   }

   @Override
   public Message replaceStripped(String placeholder, String replacement) {
      return this.replace(placeholder, MiniMessage.miniMessage().stripTags(replacement, this.tagResolverBuilder.build()));
   }

   @Override
   public Message withPrefix() {
      this.messages.replaceAll(s -> this.config.prefix() + s);
      return this;
   }

   @Override
   public Message primary() {
      this.messages.replaceAll(s -> "<color:" + this.config.primaryColor() + ">" + s);
      return this;
   }

   @Override
   public Message secondary() {
      this.messages.replaceAll(s -> "<color:" + this.config.secondaryColor() + ">" + s);
      return this;
   }

   @Override
   public Message success() {
      this.messages.replaceAll(s -> "<color:" + this.config.successColor() + ">" + s);
      return this;
   }

   @Override
   public Message warning() {
      this.messages.replaceAll(s -> "<color:" + this.config.warningColor() + ">" + s);
      return this;
   }

   @Override
   public Message error() {
      this.messages.replaceAll(s -> "<color:" + this.config.errorColor() + ">" + s);
      return this;
   }

   @Override
   public Message applyCustomPlaceholders() {
      return this;
   }

   @Override
   public Component buildComponent() {
      String joined = String.join("\n", this.messages);
      return MiniMessage.miniMessage().deserialize(joined, this.tagResolverBuilder.build());
   }

   @Override
   public Message copy() {
      return new MultiMessage(this.config, this.messages);
   }

   public String build() {
      return String.join("\n", this.messages);
   }

   public List<String> getRawMessages() {
      return this.messages;
   }

   public List<SimpleMessage> getSimpleMessages() {
      List<SimpleMessage> messages = new ArrayList<>();

      for (String s : this.messages) {
         messages.add(new SimpleMessage(this.config, s));
      }

      return messages;
   }

   public MultiMessage page(int page, int messagesPerPage) {
      List<String> pageMessages = new ArrayList<>();
      int start = (page - 1) * messagesPerPage;
      int end = Math.min(start + messagesPerPage, this.messages.size());

      for (int i = start; i < end; i++) {
         pageMessages.add(this.messages.get(i));
      }

      return new MultiMessage(this.config, pageMessages);
   }

   public int getPages(int messagesPerPage) {
      return (int)Math.ceil((double)this.messages.size() / messagesPerPage);
   }
}
