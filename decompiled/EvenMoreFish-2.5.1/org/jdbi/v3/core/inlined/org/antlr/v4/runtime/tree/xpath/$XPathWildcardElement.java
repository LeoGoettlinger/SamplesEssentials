package org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.$ParseTree;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.$Tree;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.$Trees;

public class $XPathWildcardElement extends $XPathElement {
   public $XPathWildcardElement() {
      super("*");
   }

   @Override
   public Collection<$ParseTree> evaluate($ParseTree t) {
      if (this.invert) {
         return new ArrayList<>();
      } else {
         List<$ParseTree> kids = new ArrayList<>();

         for ($Tree c : $Trees.getChildren(t)) {
            kids.add(($ParseTree)c);
         }

         return kids;
      }
   }
}
