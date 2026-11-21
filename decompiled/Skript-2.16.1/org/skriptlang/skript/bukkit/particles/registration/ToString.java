package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ToString {
   SyntaxStringBuilder toString(@NotNull Expression<?>[] var1, SkriptParser.ParseResult var2, SyntaxStringBuilder var3);
}
