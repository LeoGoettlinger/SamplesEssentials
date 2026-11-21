package com.artillexstudios.axtrade.libs.lamp.exception;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;

public interface SelfHandledException {
   void handle(CommandActor var1);
}
