package com.artillexstudios.axtrade.libs.axapi.packet;

import com.artillexstudios.axtrade.libs.axapi.utils.Version;

public record PacketType(String name, Version from, Version to) {
}
