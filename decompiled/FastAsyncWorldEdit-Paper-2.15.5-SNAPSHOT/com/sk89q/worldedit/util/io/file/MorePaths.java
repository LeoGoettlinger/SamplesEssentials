package com.sk89q.worldedit.util.io.file;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.Spliterator;
import java.util.stream.Stream;

public class MorePaths {
   public static Comparator<Path> oldestFirst() {
      return Comparator.comparing(x -> {
         try {
            return Files.getLastModifiedTime(x);
         } catch (IOException var2) {
            return FileTime.from(Instant.EPOCH);
         }
      });
   }

   public static Comparator<Path> newestFirst() {
      return oldestFirst().reversed();
   }

   public static Stream<Path> iterPaths(Path path) {
      Deque<Path> parents = new ArrayDeque<>(path.getNameCount());

      for (Path next = path; next != null; next = next.getParent()) {
         parents.addFirst(next);
      }

      return ImmutableList.copyOf(parents).stream();
   }

   public static Spliterator<Path> optimizedSpliterator(Path path) {
      return Arrays.spliterator(Streams.stream(path).toArray(Path[]::new));
   }

   private MorePaths() {
   }
}
