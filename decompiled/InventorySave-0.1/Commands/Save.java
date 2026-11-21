package Commands;

import dev.huey.inventorysave.Core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Save implements Core.Subcommand, Core.SubcommandTabCompleter {
   @NotNull
   @Override
   public String getName() {
      return "save";
   }

   @Override
   public boolean shouldExecute(Core.CommandBody body) {
      return body.getName().equals("save") && body.isPlayer() && body.getArguments().size() >= 1;
   }

   @Override
   public void execute(Core.CommandBody body) throws IOException {
      Player pl = body.getPlayer();

      assert pl != null;

      String id = body.getArguments().get(0);
      if (!Core.isValidString(id)) {
         body.reply("Inventory ID should only contains English letters, numbers and underlines.", NamedTextColor.RED);
      } else {
         Core.inventory.save(id, pl.getInventory());
         body.reply("Saved inventory as \"" + id + "\".", NamedTextColor.GREEN);
      }
   }

   @Override
   public boolean shouldComplete(Core.CommandTabCompleteInfo info) {
      return info.commandName.equals("save");
   }

   @Override
   public List<String> complete(Core.CommandTabCompleteInfo info) {
      return new ArrayList<>();
   }
}
