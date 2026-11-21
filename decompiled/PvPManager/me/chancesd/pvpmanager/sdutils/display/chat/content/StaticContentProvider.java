package me.chancesd.pvpmanager.sdutils.display.chat.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.chancesd.pvpmanager.sdutils.display.chat.ChatLine;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StaticContentProvider implements ContentProvider {
   private final List<ChatLine> lines;

   public StaticContentProvider() {
      this.lines = new ArrayList<>();
   }

   public StaticContentProvider(List<ChatLine> lines) {
      this.lines = new ArrayList<>(lines);
   }

   public void addLine(@NotNull ChatLine line) {
      this.lines.add(line);
   }

   public void addLine(@NotNull String text) {
      this.lines.add(new ChatLine(text));
   }

   public void addLine(@NotNull String text, String command, String hoverText) {
      this.lines
         .add(
            new ChatLine(
               text, command != null ? ChatLine.ClickAction.runCommand(command) : null, hoverText != null ? ChatLine.HoverAction.showText(hoverText) : null
            )
         );
   }

   public void clear() {
      this.lines.clear();
   }

   @NotNull
   @Override
   public List<ChatLine> getContent(CommandSender sender) {
      return Collections.unmodifiableList(this.lines);
   }
}
