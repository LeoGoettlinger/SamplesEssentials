package com.viaversion.viaversion.libs.mcstructs.snbt.impl;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtDeserializeException;

public interface SNbtDeserializer<T extends Tag> {
   T deserialize(String var1) throws SNbtDeserializeException;

   Tag deserializeValue(String var1) throws SNbtDeserializeException;
}
