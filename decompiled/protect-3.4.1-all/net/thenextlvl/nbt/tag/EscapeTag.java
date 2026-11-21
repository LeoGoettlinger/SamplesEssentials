package net.thenextlvl.nbt.tag;

public sealed interface EscapeTag extends Tag permits EscapeTagImpl {
   EscapeTag INSTANCE = EscapeTagImpl.INSTANCE;
   byte ID = 0;
}
