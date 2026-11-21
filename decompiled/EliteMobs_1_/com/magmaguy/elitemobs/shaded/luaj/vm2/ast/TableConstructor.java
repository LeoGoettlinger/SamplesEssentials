package com.magmaguy.elitemobs.shaded.luaj.vm2.ast;

import java.util.List;

public class TableConstructor extends Exp {
   public List fields;

   public void accept(Visitor var1) {
      var1.visit(this);
   }
}
