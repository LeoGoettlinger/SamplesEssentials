package io.github.retrooper.packetevents.impl.netty.manager.server;

import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public abstract class ServerManagerAbstract implements ServerManager {
   @Override
   public abstract ServerVersion getVersion();
}
