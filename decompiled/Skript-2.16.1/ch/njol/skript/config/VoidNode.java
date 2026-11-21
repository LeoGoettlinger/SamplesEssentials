package ch.njol.skript.config;

import java.lang.invoke.StringConcatFactory;
import org.jetbrains.annotations.Nullable;

public class VoidNode extends Node {
   VoidNode(String line, String comment, SectionNode parent, int lineNum) {
      super(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(line.trim()), comment, parent, lineNum);
   }

   @Override
   public String getKey() {
      return this.key;
   }

   public void set(String s) {
      this.key = s;
   }

   @Override
   String save_i() {
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.key);
   }

   @Nullable
   @Override
   public Node get(String key) {
      return null;
   }
}
