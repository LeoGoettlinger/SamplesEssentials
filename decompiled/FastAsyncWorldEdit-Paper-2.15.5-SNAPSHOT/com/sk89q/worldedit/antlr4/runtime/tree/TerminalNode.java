package com.sk89q.worldedit.antlr4.runtime.tree;

import com.sk89q.worldedit.antlr4.runtime.Token;

public interface TerminalNode extends ParseTree {
   Token getSymbol();
}
