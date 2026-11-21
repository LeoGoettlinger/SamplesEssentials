package com.sk89q.worldedit.antlr4.runtime.atn;

import com.sk89q.worldedit.antlr4.runtime.misc.ObjectEqualityComparator;

public class OrderedATNConfigSet extends ATNConfigSet {
   public OrderedATNConfigSet() {
      this.configLookup = new OrderedATNConfigSet.LexerConfigHashSet();
   }

   public static class LexerConfigHashSet extends ATNConfigSet.AbstractConfigHashSet {
      public LexerConfigHashSet() {
         super(ObjectEqualityComparator.INSTANCE);
      }
   }
}
