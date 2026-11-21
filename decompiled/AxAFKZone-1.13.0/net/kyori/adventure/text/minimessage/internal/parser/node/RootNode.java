package net.kyori.adventure.text.minimessage.internal.parser.node;

import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

public final class RootNode extends ElementNode implements Node.Root {
   private final String beforePreprocessing;

   public RootNode(@NotNull final String sourceMessage, @NotNull final String beforePreprocessing) {
      super(null, null, sourceMessage);
      this.beforePreprocessing = beforePreprocessing;
   }

   @NotNull
   @Override
   public String input() {
      return this.beforePreprocessing;
   }
}
