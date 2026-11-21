package ch.njol.skript.config;

import java.util.Collections;
import java.util.Iterator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NodeNavigator extends Iterable<Node> {
   @Override
   default Iterator<Node> iterator() {
      return Collections.emptyIterator();
   }

   @Nullable
   Node get(String var1);

   @NotNull
   Node getCurrentNode();

   @Nullable
   default Node getNodeAt(@NotNull String... steps) {
      Node node = this.getCurrentNode();

      for (String step : steps) {
         if (node == null) {
            return null;
         }

         node = node.get(step);
      }

      return node;
   }

   @Contract("null -> this")
   @Nullable
   default Node getNodeAt(@Nullable String path) {
      if (path == null || path.isEmpty()) {
         return this.getCurrentNode();
      } else {
         return !path.contains(".") ? this.getNodeAt(path) : this.getNodeAt(path.split("\\."));
      }
   }

   @Nullable
   default String getValue(String path) {
      return this.getNodeAt(path) instanceof EntryNode entryNode ? entryNode.getValue() : null;
   }
}
