package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class PlayerCommandAsOpAction extends NpcAction {
   public PlayerCommandAsOpAction() {
      super("player_command_as_op", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         Player player = context.getPlayer();
         if (player != null) {
            FancyNpcsPlugin.get()
               .getFancyLogger()
               .warn(
                  "Tried to execute 'player_command_as_op' action. This action got removed, due to it's security risks. Please change your NPCs to use 'player_command' and give the necessary permissions to your players to execute the commands."
               );
         }
      }
   }
}
