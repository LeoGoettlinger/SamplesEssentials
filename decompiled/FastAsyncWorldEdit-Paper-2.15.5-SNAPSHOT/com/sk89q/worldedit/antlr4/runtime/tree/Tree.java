package com.sk89q.worldedit.antlr4.runtime.tree;

public interface Tree {
   Tree getParent();

   Object getPayload();

   Tree getChild(int var1);

   int getChildCount();

   String toStringTree();
}
