package org.skriptlang.skript.lang.script;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Nullable;

public enum ScriptWarning {
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   VARIABLE_CONFLICT("conflict", "conflict", "Variable conflict warnings no longer need suppression, as they have been removed altogether"),
   VARIABLE_SAVE("variable save"),
   MISSING_CONJUNCTION("missing conjunction", "[missing] conjunction"),
   VARIABLE_STARTS_WITH_EXPRESSION("starting expression", "starting [with] expression[s]"),
   VARIABLE_CONTAINS_COLON("variable contains colon", "variable[ name][s] contain[s|ing] colon[s]"),
   DEPRECATED_SYNTAX("deprecated syntax"),
   UNREACHABLE_CODE("unreachable code"),
   CONSTANT_CONDITION("constant condition", "constant condition[s]");

   private final String warningName;
   private final String pattern;
   @Nullable
   private final String deprecationMessage;

   private ScriptWarning(String warningName) {
      this(warningName, warningName);
   }

   private ScriptWarning(String warningName, String pattern) {
      this(warningName, pattern, null);
   }

   private ScriptWarning(String warningName, String pattern, @Nullable String deprecationMessage) {
      this.warningName = warningName;
      this.pattern = pattern;
      this.deprecationMessage = deprecationMessage;
   }

   public String getWarningName() {
      return this.warningName;
   }

   public String getPattern() {
      return this.pattern;
   }

   public boolean isDeprecated() {
      return this.deprecationMessage != null;
   }

   public String getDeprecationMessage() {
      return this.deprecationMessage;
   }

   public static void printDeprecationWarning(String message) {
      ParserInstance parser = ParserInstance.get();
      Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
      if (currentScript == null || !currentScript.suppressesWarning(DEPRECATED_SYNTAX)) {
         Skript.warning("[Deprecated] " + message);
      }
   }
}
