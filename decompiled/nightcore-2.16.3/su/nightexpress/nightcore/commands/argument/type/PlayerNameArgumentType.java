package su.nightexpress.nightcore.commands.argument.type;

import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.util.Players;

public class PlayerNameArgumentType extends StringArgumentType {
   public PlayerNameArgumentType() {
      super(false);
   }

   @NonNull
   @Override
   public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
      return context.getSender() instanceof Player player ? Players.playerNames(player) : Players.playerNames();
   }
}
