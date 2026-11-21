package me.clip.placeholderapi.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public final class Format {
   private Format() {
   }

   @NotNull
   public static Optional<List<String>> tablify(@NotNull Format.Align align, @NotNull List<List<String>> rows) {
      return findSpacing(rows)
         .map(spacing -> buildFormat(align, spacing))
         .map(
            format -> rows.stream()
               .map(row -> String.format(format, row.toArray()).substring(align == Format.Align.RIGHT ? 2 : 0))
               .collect(Collectors.toList())
         );
   }

   @NotNull
   private static String buildFormat(@NotNull Format.Align align, int[] spacing) {
      return Arrays.stream(spacing).mapToObj(space -> "%" + (align == Format.Align.LEFT ? "-" : "") + (space + 2) + "s").collect(Collectors.joining());
   }

   @NotNull
   private static Optional<int[]> findSpacing(@NotNull List<List<String>> rows) {
      return rows.stream()
         .map(row -> row.stream().mapToInt(String::length).toArray())
         .reduce((l, r) -> IntStream.range(0, Math.min(l.length, r.length)).map(i -> Math.max(l[i], r[i])).toArray());
   }

   public static enum Align {
      LEFT,
      RIGHT;
   }
}
