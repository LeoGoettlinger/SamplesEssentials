package me.clip.placeholderapi.commands;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public abstract class PlaceholderCommand {
   @NotNull
   private final String label;
   @NotNull
   private final Set<String> alias;
   @Nullable
   private String permission;

   protected PlaceholderCommand(@NotNull String label, @NotNull String... alias) {
      this.label = label;
      this.alias = Sets.newHashSet(alias);
      this.setPermission("placeholderapi." + label);
   }

   @NotNull
   public static Stream<PlaceholderCommand> filterByPermission(@NotNull CommandSender sender, @NotNull Stream<PlaceholderCommand> commands) {
      return commands.filter(target -> target.getPermission() == null || sender.hasPermission(target.getPermission()));
   }

   public static void suggestByParameter(@NotNull Stream<String> possible, @NotNull List<String> suggestions, @Nullable String parameter) {
      if (parameter == null) {
         possible.forEach(suggestions::add);
      } else {
         possible.filter(suggestion -> suggestion.toLowerCase(Locale.ROOT).startsWith(parameter.toLowerCase(Locale.ROOT))).forEach(suggestions::add);
      }
   }

   @NotNull
   public final String getLabel() {
      return this.label;
   }

   @NotNull
   @Unmodifiable
   public final Set<String> getAlias() {
      return ImmutableSet.copyOf(this.alias);
   }

   @NotNull
   @Unmodifiable
   public final Set<String> getLabels() {
      return ImmutableSet.builder().add(this.label).addAll(this.alias).build();
   }

   @Nullable
   public final String getPermission() {
      return this.permission;
   }

   public void setPermission(@NotNull String permission) {
      this.permission = permission;
   }

   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
   }

   public void complete(
      @NotNull PlaceholderAPIPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params,
      @NotNull List<String> suggestions
   ) {
   }
}
