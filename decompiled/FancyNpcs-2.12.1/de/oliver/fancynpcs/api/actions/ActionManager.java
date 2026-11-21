package de.oliver.fancynpcs.api.actions;

import java.util.List;

public interface ActionManager {
   void registerAction(NpcAction var1);

   NpcAction getActionByName(String var1);

   void unregisterAction(NpcAction var1);

   List<NpcAction> getAllActions();
}
