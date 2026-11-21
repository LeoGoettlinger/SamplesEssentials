package org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.xpath;

import java.util.ArrayList;
import java.util.Collection;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.$ParseTree;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.$Trees;

public class $XPathWildcardAnywhereElement extends $XPathElement {
   public $XPathWildcardAnywhereElement() {
      super("*");
   }

   @Override
   public Collection<$ParseTree> evaluate($ParseTree t) {
      return (Collection<$ParseTree>)(this.invert ? new ArrayList<>() : $Trees.getDescendants(t));
   }
}
