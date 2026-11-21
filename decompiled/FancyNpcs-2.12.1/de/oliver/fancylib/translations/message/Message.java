package de.oliver.fancylib.translations.message;

import de.oliver.fancylib.translations.TextConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class Message {
   protected final TextConfig config;
   protected Builder tagResolverBuilder = TagResolver.builder();

   public Message(TextConfig config) {
      this.config = config;
   }

   public Message addTagResolver(TagResolver resolver) {
      this.tagResolverBuilder = this.tagResolverBuilder.resolver(resolver);
      return this;
   }

   protected void applyColorPlaceholders() {
      this.replace("primaryColor", "<color:" + this.config.primaryColor() + ">");
      this.replace("secondaryColor", "<color:" + this.config.secondaryColor() + ">");
      this.replace("successColor", "<color:" + this.config.successColor() + ">");
      this.replace("warningColor", "<color:" + this.config.warningColor() + ">");
      this.replace("errorColor", "<color:" + this.config.errorColor() + ">");
   }

   public abstract Message replace(String var1, String var2);

   public abstract Message replaceStripped(String var1, String var2);

   public abstract Message withPrefix();

   public abstract Message primary();

   public abstract Message secondary();

   public abstract Message success();

   public abstract Message warning();

   public abstract Message error();

   public abstract Message applyCustomPlaceholders();

   public abstract Component buildComponent();

   public abstract Message copy();

   public void send(CommandSender receiver) {
      Component msg = this.buildComponent();
      if (!Component.empty().equals(msg)) {
         receiver.sendMessage(msg);
      }
   }

   public void actionbar(Player receiver) {
      Component msg = this.buildComponent();
      if (!Component.empty().equals(msg)) {
         receiver.sendActionBar(msg);
      }
   }
}
