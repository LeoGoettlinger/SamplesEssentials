package com.magmaguy.elitemobs.magmacore.scripting;

import java.nio.file.Path;

public interface ScriptProvider {
   String getNamespace();

   Path getScriptDirectory();

   ScriptHook resolveHook(String var1);
}
