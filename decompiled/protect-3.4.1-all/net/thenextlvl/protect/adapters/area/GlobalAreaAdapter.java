package net.thenextlvl.protect.adapters.area;

import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.CraftGlobalArea;
import net.thenextlvl.protect.io.AreaAdapter;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GlobalAreaAdapter implements AreaAdapter<CraftGlobalArea> {
   private final Key key = Key.key("protect:global");
   private final ProtectPlugin plugin;

   public GlobalAreaAdapter(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public Area construct(World world, String name, CompoundTag tag) {
      return new CraftGlobalArea(this.plugin, world, tag);
   }

   @Override
   public Key key() {
      return this.key;
   }
}
