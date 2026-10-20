package de.blockhub.client.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Erlaubt es, Optionswerte außerhalb des normalen Slider-Bereichs zu setzen (z. B. Gamma 12). */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {
    @Accessor("value")
    void bh$setValue(T value);
}
