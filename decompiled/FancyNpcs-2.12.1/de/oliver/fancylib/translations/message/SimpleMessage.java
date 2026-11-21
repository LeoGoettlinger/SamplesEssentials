package de.oliver.fancylib.translations.message;

import de.oliver.fancylib.translations.TextConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class SimpleMessage extends Message {
   private String message;

   public SimpleMessage(TextConfig config, String message) {
      super(config);
      this.message = message;
      this.applyColorPlaceholders();
   }

   public SimpleMessage replace(String placeholder, String replacement) {
      this.message = this.message.replace("{" + placeholder + "}", replacement).replace("%" + placeholder + "%", replacement);
      return this;
   }

   @Override
   public Message replaceStripped(String placeholder, String replacement) {
      return this.replace(placeholder, MiniMessage.miniMessage().stripTags(replacement, this.tagResolverBuilder.build()));
   }

   public SimpleMessage withPrefix() {
      this.message = this.config.prefix() + this.message;
      return this;
   }

   public SimpleMessage primary() {
      this.message = "<color:" + this.config.primaryColor() + ">" + this.message;
      return this;
   }

   public SimpleMessage secondary() {
      this.message = "<color:" + this.config.secondaryColor() + ">" + this.message;
      return this;
   }

   public SimpleMessage success() {
      this.message = "<color:" + this.config.successColor() + ">" + this.message;
      return this;
   }

   public SimpleMessage warning() {
      this.message = "<color:" + this.config.warningColor() + ">" + this.message;
      return this;
   }

   public SimpleMessage error() {
      this.message = "<color:" + this.config.errorColor() + ">" + this.message;
      return this;
   }

   public SimpleMessage applyCustomPlaceholders() {
      return this;
   }

   @Override
   public Component buildComponent() {
      return MiniMessage.miniMessage().deserialize(this.message, this.tagResolverBuilder.build());
   }

   @Override
   public Message copy() {
      return new SimpleMessage(this.config, this.message);
   }

   public String build() {
      return this.message;
   }

   @Override
   public void actionbar(Player receiver) {
      receiver.sendActionBar(this.buildComponent());
   }

   public String getMessage() {
      return this.message;
   }
}
