package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;

public interface SelfHandledException {
   void handle(CommandActor var1);
}
