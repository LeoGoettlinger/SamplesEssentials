package ch.njol.skript.aliases;

import org.skriptlang.skript.lang.script.ScriptData;

public class ScriptAliases implements ScriptData {
   public final AliasesProvider provider;
   public final AliasesParser parser;

   ScriptAliases(AliasesProvider provider, AliasesParser parser) {
      this.provider = provider;
      this.parser = parser;
   }
}
