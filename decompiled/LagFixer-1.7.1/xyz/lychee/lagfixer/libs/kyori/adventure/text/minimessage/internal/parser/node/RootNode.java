package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.parser.node;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tree.Node;

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
