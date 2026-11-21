package Commands;

import dev.huey.inventorysave.Core;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Load implements Core.Subcommand, Core.SubcommandTabCompleter {
   @NotNull
   @Override
   public String getName() {
      return "load";
   }

   @Override
   public boolean shouldExecute(Core.CommandBody body) {
      return body.getName().equals("load") && body.getArguments().size() >= 1 && (body.isPlayer() || body.getArguments().size() >= 2);
   }

   @Override
   public void execute(Core.CommandBody body) throws IOException {
      int count = 0;
      String id = body.getArguments().get(0);
      if (!Core.isValidString(id)) {
         body.reply("Inventory ID should only contains English letters, numbers and underlines.", NamedTextColor.RED);
      } else {
         if (body.getArguments().size() >= 2) {
            for (Entity entity : Bukkit.selectEntities(Bukkit.getConsoleSender(), body.getArguments().get(1))) {
               if (entity instanceof Player pl) {
                  try {
                     Core.inventory.load(id, pl);
                  } catch (IOException var10) {
                     if (var10 instanceof FileNotFoundException) {
                        body.reply("Inventory not found.", NamedTextColor.RED);
                        return;
                     }

                     throw var10;
                  }

                  count++;
               }
            }
         } else {
            count = 1;

            try {
               Core.inventory.load(id, body.getPlayer());
            } catch (IOException var9) {
               if (var9 instanceof FileNotFoundException) {
                  body.reply("Inventory not found.", NamedTextColor.RED);
                  return;
               }

               throw var9;
            }
         }

         if (count > 0) {
            body.reply("Loaded inventory \"" + id + "\" for " + count + " players.", NamedTextColor.GREEN);
         } else {
            body.reply("No entity was found.", NamedTextColor.RED);
         }
      }
   }

   @Override
   public boolean shouldComplete(Core.CommandTabCompleteInfo info) {
      return info.commandName.equals("load");
   }

   @Nullable
   @Override
   public List<String> complete(Core.CommandTabCompleteInfo info) {
      List<String> result = new ArrayList<>();
      switch (info.index) {
         case 0:
            StringUtil.copyPartialMatches(info.arguments.get(0), Core.inventory.list(), result);
            return result;
         case 1:
            StringUtil.copyPartialMatches(info.arguments.get(1), Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), result);
            return result;
         default:
            return null;
      }
   }
}
