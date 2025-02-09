package net.mirolls.melodyskyplus.mixin;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.module.FMLModules.ChatMonitor;

@Mixin(value = ChatMonitor.class, remap = false)
public class ChatMonitorMixin {
  @Inject(method = "clear", remap = false, at = @At("RETURN"))
  private void clear(WorldEvent.Load event, CallbackInfo ci) {
    MelodySkyPlus.pickaxeAbility.setPickaxeAbility(false);
    MelodySkyPlus.pickaxeAbility.setPickaxeAbilityReady(false);
  }

  @Inject(method = "onNecron", remap = false,
      at = @At(value = "FIELD", target = "Lxyz/Melody/Client;pickaxeAbility:Z", remap = false, ordinal = 0))
  private void onNecronSetPickaxeAbility1(ClientChatReceivedEvent event, CallbackInfo ci) {
    MelodySkyPlus.pickaxeAbility.setPickaxeAbility(true);
  }

  @Inject(method = "onNecron", remap = false,
      at = @At(value = "FIELD", target = "Lxyz/Melody/Client;pickaxeAbility:Z", remap = false, ordinal = 1))
  private void onNecronSetPickaxeAbility2(ClientChatReceivedEvent event, CallbackInfo ci) {
    MelodySkyPlus.pickaxeAbility.setPickaxeAbility(false);
  }

  @Inject(method = "onNecron", remap = false,
      at = @At(value = "FIELD", target = "Lxyz/Melody/Client;pickaxeAbility:Z", remap = false, ordinal = 0))
  private void onNecronSetPickaxeAbilityReady1(ClientChatReceivedEvent event, CallbackInfo ci) {
    MelodySkyPlus.pickaxeAbility.setPickaxeAbilityReady(true);
  }

  @Inject(method = "onNecron", remap = false,
      at = @At(value = "FIELD", target = "Lxyz/Melody/Client;pickaxeAbility:Z", remap = false, ordinal = 1))
  private void onNecronSetPickaxeAbilityReady2(ClientChatReceivedEvent event, CallbackInfo ci) {
    MelodySkyPlus.pickaxeAbility.setPickaxeAbilityReady(false);
  }

  @Inject(method = "onNecron", remap = false,
      at = @At(value = "FIELD", target = "Lxyz/Melody/Client;pickaxeAbility:Z", remap = false, ordinal = 2))
  private void onNecronSetPickaxeAbilityReady3(ClientChatReceivedEvent event, CallbackInfo ci) {
    MelodySkyPlus.pickaxeAbility.setPickaxeAbilityReady(false);
  }
}
