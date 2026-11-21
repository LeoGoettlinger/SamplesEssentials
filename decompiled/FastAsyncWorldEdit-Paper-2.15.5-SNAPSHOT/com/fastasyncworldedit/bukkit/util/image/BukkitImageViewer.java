package com.fastasyncworldedit.bukkit.util.image;

import com.fastasyncworldedit.core.util.image.Drawable;
import com.fastasyncworldedit.core.util.image.ImageViewer;
import java.io.IOException;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

@Deprecated(
   forRemoval = true,
   since = "2.13.1"
)
public class BukkitImageViewer implements ImageViewer {
   public BukkitImageViewer(Player player) {
      throw new UnsupportedOperationException("No longer supported.");
   }

   public void selectFrame(ItemFrame start) {
      throw new UnsupportedOperationException("No longer supported.");
   }

   public ItemFrame[][] getItemFrames() {
      throw new UnsupportedOperationException("No longer supported.");
   }

   @Override
   public void view(Drawable drawable) {
      throw new UnsupportedOperationException("No longer supported.");
   }

   public void refresh() {
      throw new UnsupportedOperationException("No longer supported.");
   }

   @Override
   public void close() throws IOException {
      throw new UnsupportedOperationException("No longer supported.");
   }
}
