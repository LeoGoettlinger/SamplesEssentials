package me.clip.placeholderapi.commands.impl.cloud;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.configuration.ExpansionSort;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import me.clip.placeholderapi.libs.kyori.adventure.text.Component;
import me.clip.placeholderapi.libs.kyori.adventure.text.ComponentLike;
import me.clip.placeholderapi.libs.kyori.adventure.text.TextComponent;
import me.clip.placeholderapi.libs.kyori.adventure.text.event.ClickEvent;
import me.clip.placeholderapi.libs.kyori.adventure.text.event.HoverEvent;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.NamedTextColor;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.TextDecoration;
import me.clip.placeholderapi.util.Format;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandECloudExpansionList extends PlaceholderCommand {
   private static final int PAGE_SIZE = 10;
   @NotNull
   private static final Function<CloudExpansion, Object> EXPANSION_NAME = expansion -> (
         expansion.shouldUpdate() ? "&6" : (expansion.hasExpansion() ? "&a" : "&7")
      )
      + expansion.getName();
   @NotNull
   private static final Function<CloudExpansion, Object> EXPANSION_AUTHOR = expansion -> "&f" + expansion.getAuthor();
   @NotNull
   private static final Function<CloudExpansion, Object> EXPANSION_VERIFIED = expansion -> expansion.getVersion().isVerified() ? "&aY" : "&cN";
   @NotNull
   private static final Function<CloudExpansion, Object> EXPANSION_LATEST_VERSION = expansion -> "&f" + expansion.getLatestVersion();
   @NotNull
   private static final Function<CloudExpansion, Object> EXPANSION_CURRENT_VERSION = expansion -> "&f"
      + PlaceholderAPIPlugin.getInstance()
         .getLocalExpansionManager()
         .findExpansionByName(expansion.getName())
         .map(PlaceholderExpansion::getVersion)
         .orElse("Unknown");
   @Unmodifiable
   private static final Set<String> OPTIONS = ImmutableSet.of("all", "installed");

   public CommandECloudExpansionList() {
      super("list");
   }

   @NotNull
   private static Collection<CloudExpansion> getExpansions(@NotNull String target, @NotNull PlaceholderAPIPlugin plugin) {
      String var2 = target.toLowerCase(Locale.ROOT);
      switch (var2) {
         case "all":
            return plugin.getCloudExpansionManager().getCloudExpansions().values();
         case "installed":
            return plugin.getCloudExpansionManager().getCloudExpansionsInstalled().values();
         default:
            return plugin.getCloudExpansionManager().getCloudExpansionsByAuthor(target).values();
      }
   }

   @NotNull
   private static List<CloudExpansion> getPage(@NotNull List<CloudExpansion> expansions, int page) {
      int head = page * 10;
      int tail = Math.min(expansions.size(), head + 10);
      return expansions.size() < head ? Collections.emptyList() : expansions.subList(head, tail);
   }

   public static void addExpansionTitle(@NotNull StringBuilder builder, @NotNull String target, int page) {
      String var3 = target.toLowerCase(Locale.ROOT);
      switch (var3) {
         case "all":
            builder.append("&bAll Expansions");
            break;
         case "installed":
            builder.append("&bInstalled Expansions");
            break;
         default:
            builder.append("&bExpansions by &f").append(target);
      }

      if (page == -1) {
         builder.append('\n');
      } else {
         builder.append(" &bPage&7: &a").append(page).append("&r");
      }
   }

   private static Component getMessage(@NotNull List<CloudExpansion> expansions, int page, int limit, @NotNull String target) {
      SimpleDateFormat format = PlaceholderAPIPlugin.getDateFormat();
      TextComponent.Builder message = Component.text();

      for (int index = 0; index < expansions.size(); index++) {
         CloudExpansion expansion = expansions.get(index);
         TextComponent.Builder line = Component.text();
         int expansionNumber = index + (page - 1) * 10 + 1;
         line.append(Component.text(expansionNumber + ". ", NamedTextColor.DARK_GRAY));
         NamedTextColor expansionColour;
         if (expansion.shouldUpdate()) {
            expansionColour = NamedTextColor.GOLD;
         } else if (expansion.hasExpansion()) {
            expansionColour = NamedTextColor.GREEN;
         } else {
            expansionColour = NamedTextColor.GRAY;
         }

         line.append(Component.text(expansion.getName(), expansionColour));
         line.clickEvent(ClickEvent.suggestCommand("/papi ecloud download " + expansion.getName()));
         TextComponent.Builder hoverText = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text(
                                                      "Click to download this expansion!", NamedTextColor.AQUA
                                                   )
                                                   .append(Component.newline()))
                                                .append(Component.newline()))
                                             .append(Component.text("Author: ", NamedTextColor.AQUA)))
                                          .append(Component.text(expansion.getAuthor(), NamedTextColor.WHITE)))
                                       .append(Component.newline()))
                                    .append(Component.text("Version: ", NamedTextColor.AQUA)))
                                 .append(Component.text(expansion.getVersion().getVersion(), NamedTextColor.WHITE)))
                              .append(Component.newline()))
                           .append(Component.text("Verified: ", NamedTextColor.AQUA)))
                        .append(
                           Component.text(
                              expansion.getVersion().isVerified() ? "✔" : "❌",
                              expansion.getVersion().isVerified() ? NamedTextColor.GREEN : NamedTextColor.RED,
                              TextDecoration.BOLD
                           )
                        ))
                     .append(Component.newline()))
                  .append(Component.text("Released: ", NamedTextColor.AQUA)))
               .append(Component.text(format.format(expansion.getLastUpdate()), NamedTextColor.WHITE)))
            .toBuilder();
         Optional.ofNullable(expansion.getDescription())
            .filter(description -> !description.isEmpty())
            .ifPresent(
               description -> hoverText.append(Component.newline())
                  .append(Component.newline())
                  .append(Component.text(description.replace("\r", "").trim(), NamedTextColor.WHITE))
            );
         line.hoverEvent(HoverEvent.showText((Component)hoverText.build()));
         if (index != expansions.size() - 1) {
            line.append(Component.newline());
         }

         message.append(line.build());
      }

      if (limit > 1) {
         message.append(Component.newline());
         TextComponent.Builder left = Component.text("◀", page > 1 ? NamedTextColor.GRAY : NamedTextColor.DARK_GRAY).toBuilder();
         if (page > 1) {
            left.clickEvent(ClickEvent.runCommand("/papi ecloud list " + target + " " + (page - 1)));
         }

         TextComponent.Builder right = Component.text("▶", page < limit ? NamedTextColor.GRAY : NamedTextColor.DARK_GRAY).toBuilder();
         if (page < limit) {
            right.clickEvent(ClickEvent.runCommand("/papi ecloud list " + target + " " + (page + 1)));
         }

         message.append(new ComponentLike[]{left, Component.text(" " + page + " ", NamedTextColor.GREEN), right});
      }

      return message.build();
   }

   private static void addExpansionTable(
      @NotNull List<CloudExpansion> expansions,
      @NotNull StringBuilder message,
      int startIndex,
      @NotNull String versionTitle,
      @NotNull Function<CloudExpansion, Object> versionFunction
   ) {
      Map<String, Function<CloudExpansion, Object>> functions = new LinkedHashMap<>();
      AtomicInteger counter = new AtomicInteger(startIndex);
      functions.put("&f", expansionx -> "&8" + counter.getAndIncrement() + ".");
      functions.put("&9Name", EXPANSION_NAME);
      functions.put("&9Author", EXPANSION_AUTHOR);
      functions.put("&9Verified", EXPANSION_VERIFIED);
      functions.put(versionTitle, versionFunction);
      List<List<String>> rows = new ArrayList<>();
      rows.add(0, new ArrayList<>(functions.keySet()));

      for (CloudExpansion expansion : expansions) {
         rows.add(functions.values().stream().map(function -> function.apply(expansion)).map(Objects::toString).collect(Collectors.toList()));
      }

      List<String> table = Format.tablify(Format.Align.LEFT, rows).orElse(Collections.emptyList());
      if (!table.isEmpty()) {
         table.add(1, "&8" + Strings.repeat("-", table.get(0).length() - rows.get(0).size() * 2));
         message.append(String.join("\n", table));
      }
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.isEmpty()) {
         Msg.msg(sender, "&cYou must specify an option. [all, {author}, installed]");
      } else {
         boolean installed = params.get(0).equalsIgnoreCase("installed");
         List<CloudExpansion> expansions = Lists.newArrayList(getExpansions(params.get(0), plugin));
         if (expansions.isEmpty()) {
            Msg.msg(sender, "&cNo expansions available to list.");
         } else {
            expansions.sort(plugin.getPlaceholderAPIConfig().getExpansionSort().orElse(ExpansionSort.LATEST));
            if (!(sender instanceof Player) && params.size() < 2) {
               StringBuilder builder = new StringBuilder();
               addExpansionTitle(builder, params.get(0), -1);
               addExpansionTable(
                  expansions, builder, 1, installed ? "&9Version" : "&9Latest Version", installed ? EXPANSION_CURRENT_VERSION : EXPANSION_LATEST_VERSION
               );
               Msg.msg(sender, builder.toString());
            } else {
               int page;
               if (params.size() < 2) {
                  page = 1;
               } else {
                  Integer parsed = Ints.tryParse(params.get(1));
                  if (parsed == null) {
                     Msg.msg(sender, "&cPage number must be an integer.");
                     return;
                  }

                  int limit = (int)Math.ceil(expansions.size() / 10.0);
                  if (parsed < 1 || parsed > limit) {
                     Msg.msg(sender, "&cPage number must be in the range &8[&a1&7..&a" + limit + "&8]");
                     return;
                  }

                  page = parsed;
               }

               StringBuilder builder = new StringBuilder();
               List<CloudExpansion> values = getPage(expansions, page - 1);
               addExpansionTitle(builder, params.get(0), page);
               if (!(sender instanceof Player)) {
                  addExpansionTable(
                     values,
                     builder,
                     (page - 1) * 10 + 1,
                     installed ? "&9Version" : "&9Latest Version",
                     installed ? EXPANSION_CURRENT_VERSION : EXPANSION_LATEST_VERSION
                  );
                  Msg.msg(sender, builder.toString());
               } else {
                  Msg.msg(sender, builder.toString());
                  int limit = (int)Math.ceil(expansions.size() / 10.0);
                  Component message = getMessage(values, page, limit, params.get(0));
                  plugin.getAdventure().player((Player)sender).sendMessage(message);
               }
            }
         }
      }
   }

   @Override
   public void complete(
      @NotNull PlaceholderAPIPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params,
      @NotNull List<String> suggestions
   ) {
      if (params.size() <= 2) {
         if (params.size() <= 1) {
            suggestByParameter(
               Sets.union(OPTIONS, plugin.getCloudExpansionManager().getCloudExpansionAuthors()).stream(), suggestions, params.isEmpty() ? null : params.get(0)
            );
         } else {
            suggestByParameter(
               IntStream.rangeClosed(1, (int)Math.ceil(getExpansions(params.get(0), plugin).size() / 10.0)).mapToObj(Objects::toString),
               suggestions,
               params.get(1)
            );
         }
      }
   }
}
