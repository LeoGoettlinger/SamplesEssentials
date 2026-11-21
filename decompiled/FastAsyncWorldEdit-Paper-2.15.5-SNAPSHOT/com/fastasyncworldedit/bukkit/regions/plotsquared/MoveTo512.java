package com.fastasyncworldedit.bukkit.regions.plotsquared;

import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.command.CommandDeclaration;
import com.plotsquared.core.command.RequiredType;

@CommandDeclaration(
   command = "moveto512",
   permission = "plots.moveto512",
   category = CommandCategory.DEBUG,
   requiredType = RequiredType.CONSOLE,
   description = "Move plots to a 512 sized region",
   usage = "/plots moveto512 [world]"
)
public class MoveTo512 {
}
