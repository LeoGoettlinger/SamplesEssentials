package Commands;

import dev.huey.inventorysave.Core;
import java.io.IOException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class List_ implements Core.Subcommand {
   @NotNull
   @Override
   public String getName() {
      return "list";
   }

   @Override
   public boolean shouldExecute(Core.CommandBody body) {
      return body.getName().equals("list");
   }

   @Override
   public void execute(Core.CommandBody body) throws IOException {
      StringBuilder result = new StringBuilder("There are: ");

      for (String id : Core.inventory.list()) {
         result.append("\"").append(id).append("\", ");
      }

      body.reply(result.substring(0, result.length() - 2) + ".", NamedTextColor.GREEN);
   }
}
