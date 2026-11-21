package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

public class ExecuteRandomActionAction extends NpcAction {
   public ExecuteRandomActionAction() {
      super("execute_random_action", false);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      int currentIndex = context.getActionIndex();
      int actionCount = context.getActions().size();
      int randomIndex = this.getRandomIndex(currentIndex, actionCount);
      NpcAction.NpcActionData action = context.getActions().get(randomIndex);
      action.action().execute(context, action.value());
      context.terminate();
   }

   private int getRandomIndex(int from, int to) {
      return new Random().nextInt(to - from) + from;
   }
}
