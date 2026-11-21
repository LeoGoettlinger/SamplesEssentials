package com.sk89q.worldedit.antlr4.runtime.tree;

import com.sk89q.worldedit.antlr4.runtime.misc.Interval;

public interface SyntaxTree extends Tree {
   Interval getSourceInterval();
}
