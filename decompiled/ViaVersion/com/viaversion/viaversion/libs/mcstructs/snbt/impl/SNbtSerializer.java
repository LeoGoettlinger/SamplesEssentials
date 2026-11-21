package com.viaversion.viaversion.libs.mcstructs.snbt.impl;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtSerializeException;

public interface SNbtSerializer {
   String serialize(Tag var1) throws SNbtSerializeException;
}
