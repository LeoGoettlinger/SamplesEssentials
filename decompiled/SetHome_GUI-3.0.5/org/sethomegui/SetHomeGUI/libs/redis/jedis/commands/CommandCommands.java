package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.CommandListFilterByParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.CommandDocument;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.CommandInfo;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.KeyValue;

public interface CommandCommands {
   long commandCount();

   Map<String, CommandDocument> commandDocs(String... var1);

   List<String> commandGetKeys(String... var1);

   List<KeyValue<String, List<String>>> commandGetKeysAndFlags(String... var1);

   Map<String, CommandInfo> commandInfo(String... var1);

   List<String> commandList();

   List<String> commandListFilterBy(CommandListFilterByParams var1);
}
