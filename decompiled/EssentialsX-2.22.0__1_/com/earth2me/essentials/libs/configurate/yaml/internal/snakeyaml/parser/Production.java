package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.parser;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.events.Event;

interface Production {
   Event produce();
}
