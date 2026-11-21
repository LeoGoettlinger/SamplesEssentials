package Commands;

import dev.huey.inventorysave.Core;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class Delete implements Core.Subcommand, Core.SubcommandTabCompleter {
   private int worldEvents = this.registerWorldEvents();

   @NotNull
   @Override
   public String getName() {
      return "delete";
   }

   @Override
   public boolean shouldExecute(Core.CommandBody body) {
      return body.getName().equals("delete") && body.isPlayer() && body.getArguments().size() >= 1;
   }

   @Override
   public void execute(Core.CommandBody body) throws IOException {
      String id = body.getArguments().get(0);

      try {
         boolean success = Core.inventory.delete(id);
         if (success) {
            body.reply("Successfully deleted inventory \"" + id + "\".", NamedTextColor.GREEN);
         } else {
            body.reply("Failed to delete inventory", NamedTextColor.RED);
         }
      } catch (IOException var4) {
         if (var4 instanceof FileNotFoundException) {
            body.reply("Inventory not found.", NamedTextColor.RED);
         } else {
            throw var4;
         }
      }
   }

   @Override
   public boolean shouldComplete(Core.CommandTabCompleteInfo info) {
      return info.commandName.equals("delete");
   }

   @Override
   public List<String> complete(Core.CommandTabCompleteInfo info) {
      List<String> result = new ArrayList<>();
      if (info.index == 0) {
         StringUtil.copyPartialMatches(info.arguments.get(0), Core.inventory.list(), result);
         return result;
      } else {
         return null;
      }
   }
}
