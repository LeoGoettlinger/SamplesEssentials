package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Module;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.ModuleLoadExParams;

public interface ModuleCommands {
   String moduleLoad(String var1);

   String moduleLoad(String var1, String... var2);

   String moduleLoadEx(String var1, ModuleLoadExParams var2);

   String moduleUnload(String var1);

   List<Module> moduleList();
}
