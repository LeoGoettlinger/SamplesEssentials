package com.sk89q.worldedit.scripting;

import java.util.Map;

public interface CraftScriptEngine {
   void setTimeLimit(int milliseconds);

   int getTimeLimit();

   Object evaluate(String script, String filename, Map<String, Object> args) throws Throwable;
}
