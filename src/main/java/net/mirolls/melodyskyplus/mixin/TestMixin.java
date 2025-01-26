package net.mirolls.melodyskyplus.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class TestMixin {
  @Inject(method = "onCrafting", at = @At(value = "RETURN"))
  public void onCrafting(World worldIn, EntityPlayer playerIn, int amount, CallbackInfo ci){

  }

}
