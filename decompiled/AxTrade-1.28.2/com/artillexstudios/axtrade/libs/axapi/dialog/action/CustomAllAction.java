package com.artillexstudios.axtrade.libs.axapi.dialog.action;

import com.artillexstudios.axtrade.libs.axapi.items.nbt.CompoundTag;
import net.kyori.adventure.key.Key;

public record CustomAllAction(Key key, CompoundTag tag) implements Action {
}
