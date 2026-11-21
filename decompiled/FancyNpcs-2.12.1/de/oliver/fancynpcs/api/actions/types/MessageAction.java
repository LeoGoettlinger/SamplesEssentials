package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import org.jetbrains.annotations.NotNull;

public class MessageAction extends NpcAction {
   public MessageAction() {
      super("message", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         if (context.getPlayer() != null) {
            context.getPlayer().sendMessage(PaperColor.handler().translate(value, context.getPlayer()));
         }
      }
   }
}
