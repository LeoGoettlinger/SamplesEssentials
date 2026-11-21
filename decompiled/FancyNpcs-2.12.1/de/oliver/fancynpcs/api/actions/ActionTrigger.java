package de.oliver.fancynpcs.api.actions;

public enum ActionTrigger {
   ANY_CLICK,
   LEFT_CLICK,
   RIGHT_CLICK,
   CUSTOM;

   public static ActionTrigger getByName(String name) {
      for (ActionTrigger trigger : values()) {
         if (trigger.name().equalsIgnoreCase(name)) {
            return trigger;
         }
      }

      return null;
   }
}
