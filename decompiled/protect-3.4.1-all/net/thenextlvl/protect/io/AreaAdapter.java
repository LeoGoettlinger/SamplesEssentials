package net.thenextlvl.protect.io;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.protect.area.Area;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;

public interface AreaAdapter<T extends Area> extends Keyed {
   @Contract(
      pure = true
   )
   Key key();

   @Contract("_, _, _ -> new")
   Area construct(World var1, String var2, CompoundTag var3);
}
