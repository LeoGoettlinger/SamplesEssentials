package de.oliver.fancynpcs.libs.chatcolorhandler.common;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParserRegistry;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.settings.ColorHandlerSettings;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class ColorHandler<T> {
   private final ColorHandlerSettings settings = new ColorHandlerSettings();
   private final ParserRegistry parsers = new ParserRegistry();

   public ColorHandlerSettings settings() {
      return this.settings;
   }

   public ParserRegistry parsers() {
      return this.parsers;
   }

   public Collection<Parser> allParsers() {
      return this.parsers.values();
   }

   public abstract T translate(@Nullable String var1, @Nullable Player var2, UnaryOperator<Parsers> var3);

   public T translate(@Nullable String string, @Nullable Player player) {
      return this.translate(string, player, this.settings().defaultParsers());
   }

   public T translate(@Nullable String string, UnaryOperator<Parsers> parsers) {
      return this.translate(string, null, parsers);
   }

   public T translate(@Nullable String string) {
      return this.translate(string, null, this.settings().defaultParsers());
   }

   public List<T> translate(Collection<String> strings, Player player, UnaryOperator<Parsers> parsers) {
      return strings.stream().map(string -> this.translate(string, player, parsers)).toList();
   }

   public List<T> translate(Collection<String> strings, Player player) {
      return strings.stream().map(string -> this.translate(string, player)).toList();
   }

   public List<T> translate(Collection<String> strings, UnaryOperator<Parsers> parsers) {
      return strings.stream().map(string -> this.translate(string, parsers)).toList();
   }

   public List<T> translate(Collection<String> strings) {
      return strings.stream().map(this::translate).toList();
   }

   public abstract String translateRaw(@Nullable String var1, @Nullable Player var2, UnaryOperator<Parsers> var3);

   public String translateRaw(@Nullable String string, @Nullable Player player) {
      return this.translateRaw(string, player, this.settings().defaultParsers());
   }

   public String translateRaw(@Nullable String string, UnaryOperator<Parsers> parsers) {
      return this.translateRaw(string, null, parsers);
   }

   public String translateRaw(@Nullable String string) {
      return this.translateRaw(string, null, this.settings().defaultParsers());
   }

   public abstract void sendMessage(CommandSender var1, @Nullable String var2);

   public void sendMessage(CommandSender recipient, String... messages) {
      for (String message : messages) {
         this.sendMessage(recipient, message);
      }
   }

   public void sendMessage(Collection<CommandSender> recipients, @Nullable String message) {
      for (CommandSender recipient : recipients) {
         this.sendMessage(recipient, message);
      }
   }

   public void sendMessage(Collection<CommandSender> recipients, String... messages) {
      for (String message : messages) {
         this.sendMessage(recipients, message);
      }
   }

   public abstract void broadcastMessage(@Nullable String var1);

   public void broadcastMessage(String... messages) {
      for (String message : messages) {
         this.broadcastMessage(message);
      }
   }

   public abstract void sendActionBarMessage(Player var1, @Nullable String var2);

   public void sendActionBarMessage(Collection<Player> players, @Nullable String message) {
      for (Player player : players) {
         this.sendActionBarMessage(player, message);
      }
   }

   public abstract void sendTitle(Player var1, @Nullable String var2, @Nullable String var3, int var4, int var5, int var6);

   public void sendTitle(Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int fadeOut) {
      this.sendTitle(player, title, subtitle, fadeIn, 70, fadeOut);
   }

   public void sendTitle(Player player, @Nullable String title, @Nullable String subtitle) {
      this.sendTitle(player, title, subtitle, 10, 20);
   }

   public void sendTitle(Player player, @Nullable String title) {
      this.sendTitle(player, title, null);
   }

   public void sendTitle(Collection<Player> players, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
      for (Player player : players) {
         this.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
      }
   }

   public void sendTitle(Collection<Player> players, @Nullable String title, @Nullable String subtitle, int fadeIn, int fadeOut) {
      this.sendTitle(players, title, subtitle, fadeIn, 70, fadeOut);
   }

   public void sendTitle(Collection<Player> players, @Nullable String title, @Nullable String subtitle) {
      this.sendTitle(players, title, subtitle, 10, 20);
   }

   public void sendTitle(Collection<Player> players, @Nullable String title) {
      this.sendTitle(players, title, null);
   }
}
