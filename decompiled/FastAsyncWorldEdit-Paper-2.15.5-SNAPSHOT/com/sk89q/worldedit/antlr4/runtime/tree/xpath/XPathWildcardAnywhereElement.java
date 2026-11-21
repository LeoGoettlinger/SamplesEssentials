package com.sk89q.worldedit.antlr4.runtime.tree.xpath;

import com.sk89q.worldedit.antlr4.runtime.tree.ParseTree;
import com.sk89q.worldedit.antlr4.runtime.tree.Trees;
import java.util.ArrayList;
import java.util.Collection;

public class XPathWildcardAnywhereElement extends XPathElement {
   public XPathWildcardAnywhereElement() {
      super("*");
   }

   @Override
   public Collection<ParseTree> evaluate(ParseTree t) {
      return (Collection<ParseTree>)(this.invert ? new ArrayList<>() : Trees.getDescendants(t));
   }
}
