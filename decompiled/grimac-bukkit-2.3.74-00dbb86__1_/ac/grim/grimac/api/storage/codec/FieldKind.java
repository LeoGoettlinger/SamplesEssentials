package ac.grim.grimac.api.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum FieldKind {
   ID,
   PARTITION,
   TIMESTAMP,
   INDEXED,
   SEARCHABLE,
   VALUE;
}
