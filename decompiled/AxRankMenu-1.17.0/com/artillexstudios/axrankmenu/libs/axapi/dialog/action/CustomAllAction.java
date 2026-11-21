package com.artillexstudios.axrankmenu.libs.axapi.dialog.action;

import com.artillexstudios.axrankmenu.libs.axapi.items.nbt.CompoundTag;
import net.kyori.adventure.key.Key;

public record CustomAllAction(Key key, CompoundTag tag) implements Action {
}
