package me.chancesd.pvpmanager.sdutils.display.chat.content;

import java.util.List;
import me.chancesd.pvpmanager.sdutils.display.chat.ChatLine;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface ContentProvider {
   @NotNull
   List<ChatLine> getContent(CommandSender var1);
}
