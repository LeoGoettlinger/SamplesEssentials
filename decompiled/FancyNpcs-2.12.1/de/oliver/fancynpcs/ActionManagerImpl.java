package de.oliver.fancynpcs;

import de.oliver.fancynpcs.api.actions.ActionManager;
import de.oliver.fancynpcs.api.actions.NpcAction;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionManagerImpl implements ActionManager {
   private final Map<String, NpcAction> actions = new ConcurrentHashMap<>();

   @Override
   public void registerAction(NpcAction action) {
      this.actions.put(action.getName(), action);
   }

   @Override
   public NpcAction getActionByName(String name) {
      return this.actions.getOrDefault(name, null);
   }

   @Override
   public void unregisterAction(NpcAction action) {
      this.actions.remove(action.getName());
   }

   @Override
   public List<NpcAction> getAllActions() {
      return List.copyOf(this.actions.values());
   }
}
