package com.sk89q.worldedit.jchronic.handlers;

import com.sk89q.worldedit.jchronic.Options;
import com.sk89q.worldedit.jchronic.utils.Span;
import com.sk89q.worldedit.jchronic.utils.Token;
import java.util.List;

public interface IHandler {
   Span handle(List<Token> var1, Options var2);
}
