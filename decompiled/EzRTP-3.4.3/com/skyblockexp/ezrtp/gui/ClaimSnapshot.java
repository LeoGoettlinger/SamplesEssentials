package com.skyblockexp.ezrtp.gui;

import java.util.UUID;

public record ClaimSnapshot(String worldName, int chunkX, int chunkZ, UUID ownerUuid) {
}
