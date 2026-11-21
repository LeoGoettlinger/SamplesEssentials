package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.emitter;

import java.io.IOException;

interface EmitterState {
   void expect() throws IOException;
}
