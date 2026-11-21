package com.artillexstudios.axtrade.libs.axapi.config.service;

import java.nio.file.Path;

public interface Writer {
   boolean write(Path path, String toWrite);
}
