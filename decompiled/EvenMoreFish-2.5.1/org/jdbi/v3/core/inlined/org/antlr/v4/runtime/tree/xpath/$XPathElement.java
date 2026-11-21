package org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.xpath;

import java.util.Collection;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.tree.$ParseTree;

public abstract class $XPathElement {
   protected String nodeName;
   protected boolean invert;

   public $XPathElement(String nodeName) {
      this.nodeName = nodeName;
   }

   public abstract Collection<$ParseTree> evaluate($ParseTree var1);

   @Override
   public String toString() {
      String inv = this.invert ? "!" : "";
      return this.getClass().getSimpleName() + "[" + inv + this.nodeName + "]";
   }
}
