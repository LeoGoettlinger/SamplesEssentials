package com.artillexstudios.axafkzone.libs.axapi.dialog.action;

import com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag;
import net.kyori.adventure.key.Key;

public record CustomAllAction(Key key, CompoundTag tag) implements Action {
}
