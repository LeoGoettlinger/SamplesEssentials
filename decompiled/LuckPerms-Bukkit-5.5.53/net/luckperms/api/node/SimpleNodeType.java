package net.luckperms.api.node;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

final class SimpleNodeType<T extends Node> implements NodeType<T> {
   private final String name;
   private final Predicate<Node> matches;
   private final Function<Node, T> cast;

   SimpleNodeType(String name, Predicate<Node> matches, Function<Node, T> cast) {
      this.name = name;
      this.matches = matches;
      this.cast = cast;
   }

   @NotNull
   @Override
   public String name() {
      return this.name;
   }

   @Override
   public boolean matches(@NonNull Node node) {
      Objects.requireNonNull(node, "node");
      return this.matches.test(node);
   }

   @NotNull
   @Override
   public T cast(@NonNull Node node) {
      if (!this.matches(node)) {
         throw new IllegalArgumentException("Node " + node.getClass() + " does not match " + this.name);
      } else {
         return this.cast.apply(node);
      }
   }

   @Override
   public String toString() {
      return this.name();
   }
}
