package tk.xor7.specialmanhunt.mixin;

import net.minecraft.entity.mob.PiglinBruteEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PiglinBruteEntity.class)
public class PiglinBruteEntityMixin {
    @Final
    @Shadow
    private static final int ATTACK_DAMAGE = 0;

}
