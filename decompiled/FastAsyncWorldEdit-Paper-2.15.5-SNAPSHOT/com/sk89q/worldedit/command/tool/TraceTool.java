package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;

public interface TraceTool extends Tool {
   boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session);
}
