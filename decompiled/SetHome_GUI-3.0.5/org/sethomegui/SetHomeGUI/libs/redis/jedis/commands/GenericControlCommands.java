package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Module;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.FailoverParams;

public interface GenericControlCommands extends ConfigCommands, ScriptingControlCommands, SlowlogCommands {
   String failover();

   String failover(FailoverParams var1);

   String failoverAbort();

   List<Module> moduleList();
}
