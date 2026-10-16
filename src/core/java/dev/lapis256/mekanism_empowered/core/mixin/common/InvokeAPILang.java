package dev.lapis256.mekanism_empowered.core.mixin.common;

import mekanism.api.text.APILang;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(value = APILang.class, remap = false)
public interface InvokeAPILang {
    @Invoker("<init>")
    static APILang createDummy(String internalName, int internalId, String key) {
        throw new AssertionError("Mixin failed to apply, this should never be called");
    }
}
