package org.skriptlang.skript.log.runtime;

import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ErrorSource(String syntaxType, String syntaxName, int lineNumber, String lineText, String script) {
   @NotNull
   public static ErrorSource fromNodeAndElement(@Nullable Node node, @NotNull SyntaxElement element) {
      Name annotation = element.getClass().getAnnotation(Name.class);
      String elementName = annotation != null ? annotation.value().trim().replaceAll("\n", "") : element.getClass().getSimpleName();
      if (node == null) {
         return new ErrorSource(element.getSyntaxTypeName(), elementName, 0, "-unknown-", "-unknown-");
      } else {
         String code = node.save().trim();
         return new ErrorSource(element.getSyntaxTypeName(), elementName, node.getLine(), code, node.getConfig().getFileName());
      }
   }

   @Contract(" -> new")
   @NotNull
   public ErrorSource.Location location() {
      return new ErrorSource.Location(this.script, this.lineNumber);
   }

   public record Location(String script, int lineNumber) {
   }
}
