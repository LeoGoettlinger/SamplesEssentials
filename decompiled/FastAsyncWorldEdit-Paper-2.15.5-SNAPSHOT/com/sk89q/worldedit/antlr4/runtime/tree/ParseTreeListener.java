package com.sk89q.worldedit.antlr4.runtime.tree;

import com.sk89q.worldedit.antlr4.runtime.ParserRuleContext;

public interface ParseTreeListener {
   void visitTerminal(TerminalNode var1);

   void visitErrorNode(ErrorNode var1);

   void enterEveryRule(ParserRuleContext var1);

   void exitEveryRule(ParserRuleContext var1);
}
