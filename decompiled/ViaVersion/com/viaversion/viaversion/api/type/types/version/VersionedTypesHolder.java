package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.version.VersionedStructuredDataKeys;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.AbstractEntityDataTypes;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import java.util.List;

public interface VersionedTypesHolder {
   Type<Item> item();

   ArrayType<Item> itemArray();

   Type<Item> itemTemplate();

   Type<Item> optionalItemTemplate();

   ArrayType<Item> itemTemplateArray();

   Type<Item> itemCost();

   Type<Item> optionalItemCost();

   Type<Item> lengthPrefixedItem();

   StructuredDataType structuredData();

   Type<StructuredData<?>[]> structuredDataArray();

   VersionedStructuredDataKeys structuredDataKeys();

   ParticleType particle();

   ArrayType<Particle> particles();

   AbstractEntityDataTypes entityDataTypes();

   Type<List<EntityData>> entityDataList();
}
