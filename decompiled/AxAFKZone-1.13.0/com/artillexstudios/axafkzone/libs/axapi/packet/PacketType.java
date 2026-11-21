package com.artillexstudios.axafkzone.libs.axapi.packet;

import com.artillexstudios.axafkzone.libs.axapi.utils.Version;

public record PacketType(String name, Version from, Version to) {
}
