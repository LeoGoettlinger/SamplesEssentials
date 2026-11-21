package com.artillexstudios.axrankmenu.libs.axapi.dialog;

import com.artillexstudios.axrankmenu.libs.axapi.dialog.action.Action;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;

public record ActionButton(Component label, @Nullable Component tooltip, int width, @Nullable Action action) {
}
