package me.clip.placeholderapi;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.clip.placeholderapi.replacer.ComponentReplacer;
import me.clip.placeholderapi.replacer.ExactReplacer;
import me.clip.placeholderapi.replacer.RelationalExactReplacer;
import me.clip.placeholderapi.replacer.Replacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PAPIComponents {
   private static final Replacer PERCENT_EXACT_REPLACER = new ExactReplacer('%', '%');
   private static final Replacer BRACKET_EXACT_REPLACER = new ExactReplacer('{', '}');
   private static final RelationalExactReplacer RELATIONAL_EXACT_REPLACER = new RelationalExactReplacer();

   @NotNull
   public static Component setPlaceholders(OfflinePlayer player, @NotNull Component component) {
      return setPlaceholders(player, component, null);
   }

   @NotNull
   public static Component setPlaceholders(OfflinePlayer player, @NotNull Component component, @Nullable Function<String, ComponentLike> deserializer) {
      return PlaceholderAPIPlugin.getInstance().getPlaceholderAPIConfig().useAdventureProvidedReplacer()
         ? component.replaceText(config -> config.match(PlaceholderAPI.PLACEHOLDER_PATTERN).replacement((result, builder) -> {
            String parsed = PERCENT_EXACT_REPLACER.apply(result.group(), player, PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion);
            return (ComponentLike)(deserializer == null ? builder.content(parsed) : deserializer.apply(parsed));
         }))
         : ComponentReplacer.replace(
            component, str -> PlaceholderAPI.setPlaceholders(player, str), deserializer == null ? null : s -> deserializer.apply(s).asComponent()
         );
   }

   @NotNull
   public static List<Component> setPlaceholders(OfflinePlayer player, @NotNull List<Component> components) {
      return setPlaceholders(player, components, null);
   }

   @NotNull
   public static List<Component> setPlaceholders(
      OfflinePlayer player, @NotNull List<Component> components, @Nullable Function<String, ComponentLike> deserializer
   ) {
      return components.stream()
         .map(component -> setPlaceholders(player, component, deserializer == null ? null : s -> deserializer.apply(s).asComponent()))
         .collect(Collectors.toList());
   }

   @NotNull
   public static Component setPlaceholders(Player player, @NotNull Component component) {
      return setPlaceholders(player, component, null);
   }

   @NotNull
   public static Component setPlaceholders(Player player, @NotNull Component component, @Nullable Function<String, ComponentLike> deserializer) {
      return setPlaceholders((OfflinePlayer)player, component, deserializer);
   }

   @NotNull
   public static List<Component> setPlaceholders(Player player, @NotNull List<Component> components) {
      return setPlaceholders(player, components, null);
   }

   @NotNull
   public static List<Component> setPlaceholders(Player player, @NotNull List<Component> components, @Nullable Function<String, ComponentLike> deserializer) {
      return setPlaceholders((OfflinePlayer)player, components, deserializer);
   }

   @NotNull
   public static Component setBracketPlaceholders(OfflinePlayer player, @NotNull Component component) {
      return setBracketPlaceholders(player, component, null);
   }

   @NotNull
   public static Component setBracketPlaceholders(OfflinePlayer player, @NotNull Component component, @Nullable Function<String, ComponentLike> deserializer) {
      return PlaceholderAPIPlugin.getInstance().getPlaceholderAPIConfig().useAdventureReplacer()
         ? component.replaceText(
            config -> config.match(PlaceholderAPI.BRACKET_PLACEHOLDER_PATTERN)
               .replacement(
                  (result, builder) -> builder.content(
                     BRACKET_EXACT_REPLACER.apply(result.group(), player, PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion)
                  )
               )
         )
         : ComponentReplacer.replace(
            component, str -> PlaceholderAPI.setBracketPlaceholders(player, str), deserializer == null ? null : s -> deserializer.apply(s).asComponent()
         );
   }

   @NotNull
   public static List<Component> setBracketPlaceholders(OfflinePlayer player, @NotNull List<Component> components) {
      return setBracketPlaceholders(player, components, null);
   }

   @NotNull
   public static List<Component> setBracketPlaceholders(
      OfflinePlayer player, @NotNull List<Component> components, @Nullable Function<String, ComponentLike> deserializer
   ) {
      return components.stream().map(component -> setBracketPlaceholders(player, component, deserializer)).collect(Collectors.toList());
   }

   @NotNull
   public static Component setBracketPlaceholders(Player player, @NotNull Component component) {
      return setBracketPlaceholders(player, component, null);
   }

   @NotNull
   public static Component setBracketPlaceholders(Player player, @NotNull Component component, @Nullable Function<String, ComponentLike> deserializer) {
      return setBracketPlaceholders((OfflinePlayer)player, component, deserializer);
   }

   @NotNull
   public static List<Component> setBracketPlaceholders(Player player, @NotNull List<Component> components) {
      return setBracketPlaceholders(player, components, null);
   }

   @NotNull
   public static List<Component> setBracketPlaceholders(
      Player player, @NotNull List<Component> components, @Nullable Function<String, ComponentLike> deserializer
   ) {
      return setBracketPlaceholders((OfflinePlayer)player, components, deserializer);
   }

   public static Component setRelationalPlaceholders(Player one, Player two, Component component) {
      return component.replaceText(
         config -> config.match(PlaceholderAPI.RELATIONAL_PLACEHOLDER_PATTERN)
            .replacement(
               (result, builder) -> builder.content(
                  RELATIONAL_EXACT_REPLACER.apply(result.group(2), one, two, PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion)
               )
            )
      );
   }

   public static List<Component> setRelationalPlaceholders(Player one, Player two, List<Component> components) {
      return components.stream().map(line -> setRelationalPlaceholders(one, two, line)).collect(Collectors.toList());
   }
}
