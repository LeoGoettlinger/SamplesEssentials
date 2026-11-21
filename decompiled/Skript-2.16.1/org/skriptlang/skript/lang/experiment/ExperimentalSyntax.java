package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.lang.SyntaxElement;

public interface ExperimentalSyntax extends SyntaxElement {
   boolean isSatisfiedBy(ExperimentSet var1);
}
