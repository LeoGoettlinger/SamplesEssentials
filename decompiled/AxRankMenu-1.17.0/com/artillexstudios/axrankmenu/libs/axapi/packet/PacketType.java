package com.artillexstudios.axrankmenu.libs.axapi.packet;

import com.artillexstudios.axrankmenu.libs.axapi.utils.Version;

public record PacketType(String name, Version from, Version to) {
}
