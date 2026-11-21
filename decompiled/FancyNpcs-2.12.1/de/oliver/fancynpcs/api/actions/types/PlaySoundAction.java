package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import org.jetbrains.annotations.NotNull;

public class PlaySoundAction extends NpcAction {
   public PlaySoundAction() {
      super("play_sound", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         if (context.getPlayer() != null) {
            String sound = PaperColor.handler().translateRaw(value, context.getPlayer(), Parsers::placeholder);
            FancyNpcsPlugin.get().getScheduler().runTask(context.getPlayer().getLocation(), () -> {
               try {
                  context.getPlayer().playSound(context.getPlayer().getLocation(), value, 1.0F, 1.0F);
               } catch (Exception var4) {
                  FancyNpcsPlugin.get().getFancyLogger().warn("Failed to play sound: " + sound);
               }
            });
         }
      }
   }
}
