package com.lishid.openinv.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class TabCompleter {
   @NotNull
   @Unmodifiable
   public static List<String> completeInteger(@NotNull String argument) {
      if (!argument.isEmpty()) {
         try {
            Integer.parseInt(argument);
         } catch (NumberFormatException var3) {
            return List.of();
         }
      }

      List<String> completions = new ArrayList<>(10);

      for (int i = 0; i < 10; i++) {
         completions.add(argument + i);
      }

      return Collections.unmodifiableList(completions);
   }

   @NotNull
   public static List<String> completeEnum(@NotNull String argument, @NotNull Class<? extends Enum<?>> enumClazz) {
      argument = argument.toLowerCase(Locale.ENGLISH);
      List<String> completions = new ArrayList<>();

      for (Enum<?> enumConstant : enumClazz.getEnumConstants()) {
         String name = enumConstant.name().toLowerCase(Locale.ENGLISH);
         if (name.startsWith(argument)) {
            completions.add(name);
         }
      }

      return completions;
   }

   @NotNull
   public static List<String> completeString(@NotNull String argument, @NotNull String[] options) {
      argument = argument.toLowerCase(Locale.ENGLISH);
      List<String> completions = new ArrayList<>();

      for (String option : options) {
         if (option.startsWith(argument)) {
            completions.add(option);
         }
      }

      return completions;
   }

   public static List<String> completeOnlinePlayer(@Nullable CommandSender sender, @NotNull String argument) {
      List<String> completions = new ArrayList<>();
      Player senderPlayer = sender instanceof Player player ? player : null;

      for (Player playerx : Bukkit.getOnlinePlayers()) {
         if ((senderPlayer == null || senderPlayer.canSee(playerx)) && StringUtil.startsWithIgnoreCase(playerx.getName(), argument)) {
            completions.add(playerx.getName());
         }
      }

      return completions;
   }

   public static <T> List<String> completeObject(@NotNull String argument, @NotNull Function<T, String> converter, @NotNull T[] options) {
      argument = argument.toLowerCase(Locale.ENGLISH);
      List<String> completions = new ArrayList<>();

      for (T option : options) {
         String optionString = converter.apply(option).toLowerCase(Locale.ENGLISH);
         if (optionString.startsWith(argument)) {
            completions.add(optionString);
         }
      }

      return completions;
   }

   private TabCompleter() {
      throw new IllegalStateException("Cannot create instance of utility class.");
   }
}
