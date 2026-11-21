package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;

public class BlockUntilDoneAction extends NpcAction {
   public BlockUntilDoneAction() {
      super("block_until_done", false);
   }

   @Override
   public void execute(ActionExecutionContext context, String value) {
   }
}
