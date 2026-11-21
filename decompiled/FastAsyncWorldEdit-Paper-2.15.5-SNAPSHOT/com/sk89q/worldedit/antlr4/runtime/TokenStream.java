package com.sk89q.worldedit.antlr4.runtime;

import com.sk89q.worldedit.antlr4.runtime.misc.Interval;

public interface TokenStream extends IntStream {
   Token LT(int var1);

   Token get(int var1);

   TokenSource getTokenSource();

   String getText(Interval var1);

   String getText();

   String getText(RuleContext var1);

   String getText(Token var1, Token var2);
}
