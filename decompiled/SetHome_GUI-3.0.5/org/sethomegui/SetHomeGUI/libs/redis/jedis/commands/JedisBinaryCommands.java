package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

public interface JedisBinaryCommands
   extends KeyBinaryCommands,
   StringBinaryCommands,
   ListBinaryCommands,
   HashBinaryCommands,
   SetBinaryCommands,
   SortedSetBinaryCommands,
   GeoBinaryCommands,
   HyperLogLogBinaryCommands,
   StreamBinaryCommands,
   ScriptingKeyBinaryCommands,
   FunctionBinaryCommands {
}
