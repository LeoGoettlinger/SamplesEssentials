package com.sk89q.worldedit.antlr4.runtime.tree;

import com.sk89q.worldedit.antlr4.runtime.RuleContext;

public interface RuleNode extends ParseTree {
   RuleContext getRuleContext();
}
