package net.ess3.nms.refl.providers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.KnownCommandsProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

@ProviderData(
   description = "Reflection Known Commands Provider"
)
public class ReflKnownCommandsProvider implements KnownCommandsProvider {
   private final Map<String, Command> knownCommands;

   public ReflKnownCommandsProvider() {
      Map<String, Command> knownCommands = new HashMap<>();

      try {
         Class<? extends Server> craftServerClass = (Class<? extends Server>)ReflUtil.getOBCClass("CraftServer");
         if (craftServerClass != null) {
            Field commandMapField = ReflUtil.getFieldCached(craftServerClass, "commandMap");
            if (commandMapField != null) {
               SimpleCommandMap simpleCommandMap = (SimpleCommandMap)commandMapField.get(Bukkit.getServer());
               Field knownCommandsField = ReflUtil.getFieldCached(SimpleCommandMap.class, "knownCommands");
               if (knownCommandsField != null) {
                  knownCommands = (Map<String, Command>)knownCommandsField.get(simpleCommandMap);
               }
            }
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      } finally {
         this.knownCommands = knownCommands;
      }
   }

   @Override
   public Map<String, Command> getKnownCommands() {
      return this.knownCommands;
   }
}
