package ch.njol.skript.config;

import org.jetbrains.annotations.Nullable;

public class SimpleNode extends Node {
   public SimpleNode(String value, String comment, int lineNum, SectionNode parent) {
      super(value, comment, parent, lineNum);
   }

   public SimpleNode(Config c) {
      super(c);
   }

   @Override
   String save_i() {
      return this.key;
   }

   public void set(String s) {
      this.key = s;
   }

   @Nullable
   @Override
   public Node get(String key) {
      return null;
   }
}
