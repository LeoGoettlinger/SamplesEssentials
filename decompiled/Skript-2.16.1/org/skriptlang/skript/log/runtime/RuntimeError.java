package org.skriptlang.skript.log.runtime;

import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public record RuntimeError(Level level, ErrorSource source, String error, @Nullable String toHighlight) {
}
