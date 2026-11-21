package com.fastasyncworldedit.bukkit.regions.plotsquared;

import com.fastasyncworldedit.core.util.TaskManager;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.command.CommandDeclaration;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.command.SubCommand;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@CommandDeclaration(
   command = "trimchunks",
   permission = "plots.admin",
   description = "Delete unmodified portions of your plotworld",
   requiredType = RequiredType.PLAYER,
   category = CommandCategory.ADMINISTRATION
)
public class FaweTrim extends SubCommand {
   private boolean ran = false;

   public boolean onCommand(final PlotPlayer plotPlayer, final String[] strings) {
      if (this.ran) {
         plotPlayer.sendMessage(TranslatableCaption.of("error.task_in_process"), new TagResolver[0]);
         return false;
      } else if (strings.length != 2) {
         plotPlayer.sendMessage(
            StaticCaption.of("First make a backup of your world called <world-copy> then stand in the middle of an empty plot"), new TagResolver[0]
         );
         plotPlayer.sendMessage(StaticCaption.of("use /plot trimall <world> <boolean-delete-unowned>"), new TagResolver[0]);
         return false;
      } else if (!PlotSquared.platform().worldUtil().isWorld(strings[0])) {
         plotPlayer.sendMessage(
            TranslatableCaption.of("errors.not_valid_plot_world"), new TagResolver[]{TagResolver.resolver("value", Tag.inserting(Component.text(strings[0])))}
         );
         return false;
      } else {
         this.ran = true;
         TaskManager.taskManager().async(() -> this.ran = false);
         return true;
      }
   }
}
