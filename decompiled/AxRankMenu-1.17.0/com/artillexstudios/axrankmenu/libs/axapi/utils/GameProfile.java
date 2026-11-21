package com.artillexstudios.axrankmenu.libs.axapi.utils;

import com.artillexstudios.axrankmenu.libs.axapi.items.component.type.ProfileProperties;
import java.util.UUID;

public record GameProfile(String name, UUID uuid, ProfileProperties properties) {
}
