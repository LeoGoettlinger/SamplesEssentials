package de.oliver.fancynpcs.libs.chatcolorhandler.paper;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.ColorHandler;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.HexParser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.LegacyAmpersandParser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.LegacyHexParser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParticleParser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.PlaceholderAPIParser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.SoundParser;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.MiniMessageColorResolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.MiniMessageDefaultsResolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.MiniMessageInteractionResolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.MiniMessagePlaceholderResolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.MiniMessageTextFormattingResolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.MiniPlaceholdersResolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.Resolver;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver.ResolverRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

public class PaperColor extends ColorHandler<Component> {
   private static final PaperColor INSTANCE = new PaperColor();
   private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(TagResolver.empty()).build();
   private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
      .character('§')
      .hexColors()
      .useUnusualXRepeatedCharacterHexFormat()
      .build();
   private final ResolverRegistry resolvers = new ResolverRegistry();

   public PaperColor() {
      this.parsers().register(HexParser.INSTANCE);
      this.parsers().register(LegacyHexParser.INSTANCE);
      this.parsers().register(LegacyAmpersandParser.INSTANCE);
      this.parsers().register(SoundParser.INSTANCE);
      this.parsers().register(ParticleParser.INSTANCE);
      this.resolvers()
         .register(
            MiniMessageColorResolver.INSTANCE,
            MiniMessageDefaultsResolver.INSTANCE,
            MiniMessageInteractionResolver.INSTANCE,
            MiniMessagePlaceholderResolver.INSTANCE,
            MiniMessageTextFormattingResolver.INSTANCE
         );
      PluginManager pluginManager = Bukkit.getServer().getPluginManager();
      if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
         this.parsers().register(PlaceholderAPIParser.INSTANCE);
      }

      if (pluginManager.isPluginEnabled("MiniPlaceholders")) {
         this.resolvers().register(MiniPlaceholdersResolver.INSTANCE);
      }

      List<Parser> defaultParsers = new ArrayList<>(this.parsers().values());
      defaultParsers.addAll(this.resolvers().values());
      this.settings().defaultParsers(defaultParsers);
   }

   public ResolverRegistry resolvers() {
      return this.resolvers;
   }

   @Override
   public Collection<Parser> allParsers() {
      return Stream.concat(this.parsers().values().stream(), this.resolvers().values().stream()).toList();
   }

   public Component translate(@Nullable String string, @Nullable Player player, UnaryOperator<Parsers> parsers) {
      if (string != null && !string.isBlank()) {
         Parsers parserSet = parsers.apply(new Parsers(this));
         String translated = this.parsers().parseString(string, player, parserSet);
         TagResolver resolver = this.resolvers
            .asTagResolver(player, parserSet.values().stream().filter(parser -> parser instanceof Resolver).map(parser -> (Resolver)parser).toList());
         return player != null
            ? MINI_MESSAGE.deserialize(translated, player, resolver).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE)
            : MINI_MESSAGE.deserialize(translated, resolver).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
      } else {
         return Component.empty();
      }
   }

   @Override
   public String translateRaw(@Nullable String string, @Nullable Player player, UnaryOperator<Parsers> parsers) {
      return LEGACY_SERIALIZER.serialize(this.translate(string, player, parsers));
   }

   @Override
   public void sendMessage(CommandSender recipient, @Nullable String message) {
      if (message != null && !message.isBlank()) {
         Component translated = this.translate(message, recipient instanceof Player player ? player : null);
         recipient.sendMessage(translated);
      }
   }

   @Override
   public void broadcastMessage(@Nullable String message) {
      if (message != null && !message.isBlank()) {
         Component translated = this.translate(message);
         Bukkit.getServer().sendMessage(translated);
      }
   }

   @Override
   public void sendActionBarMessage(Player player, @Nullable String message) {
      if (message != null && !message.isBlank()) {
         Component translated = this.translate(message, player);
         player.sendActionBar(translated);
      }
   }

   @Override
   public void sendTitle(Player player, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
      if (title != null && !title.isBlank() || subtitle != null && !subtitle.isBlank()) {
         Times times = Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L));
         player.showTitle(Title.title(this.translate(title, player), this.translate(subtitle, player), times));
      }
   }

   public static PaperColor handler() {
      return INSTANCE;
   }
}
