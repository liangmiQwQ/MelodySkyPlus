package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.TextValue;
import xyz.Melody.Event.value.Value;
import xyz.Melody.module.modules.macros.Mining.MiningSkill;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mixin(value = MiningSkill.class, remap = false)
public class MiningSkillMixin {
  @Shadow
  public TextValue<String> ready;
  @Shadow
  public TextValue<String> used;
  @Shadow
  public TextValue<String> expire;
  public Option<Boolean> melodySkyPlus$useRod = new Option<>("Use Rod", false);
  public Option<Boolean> melodySkyPlus$autoMode;


  @SuppressWarnings("rawtypes")
  @ModifyArg(method = "<init>",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningSkill;addValues([Lxyz/Melody/Event/value/Value;)V", remap = false),
      index = 0)
  public Value[] addValueArgs(Value[] originalValues) {
    if (AntiBug.isBugRemoved()) {

      melodySkyPlus$autoMode = new Option<>("Auto Mode", true, (val) -> {
        if (MiningSkill.getINSTANCE() != null) {
          MelodySkyPlus.pickaxeAbility.check = true;
          
          ready.setValue(val ? "🪷𬺈〾🝼⇌🝼⻯" : "Mining Speed Boost is now available!");
          ready.setEnabled(val);
          used.setValue(val ? "🪷𬺈〾🝼⇌🝼⻯" : "You used your Mining Speed Boost Pickaxe Ability!");
          used.setEnabled(val);
          expire.setValue(val ? "🪷𬺈〾🝼⇌🝼⻯" : "Your Mining Speed Boost has expired!");
          expire.setEnabled(val);
        }
      });

      Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 2);
      returnValues[returnValues.length - 2] = melodySkyPlus$autoMode;
      returnValues[returnValues.length - 1] = melodySkyPlus$useRod;

      return returnValues;
    }

    return originalValues;
  }

  @Inject(method = "tryPerformSkill", at = @At("HEAD"), cancellable = true, remap = false)
  public void tryPerformSkill(CallbackInfoReturnable<Boolean> cir) {
    if (AntiBug.isBugRemoved() && melodySkyPlus$useRod.getValue()) {
      try {
        // 读 看是否应该执行
        Class<?> client = Class.forName("xyz.Melody.Client");
        Field pickaxeField = client.getDeclaredField("pickaxeAbilityReady");
        pickaxeField.setAccessible(true);

        // 若应该执行
        if ((boolean) pickaxeField.get(null)) {
          MelodySkyPlus.miningSkillExecutor.start();

          pickaxeField.set(null, false);

          cir.cancel();
          cir.setReturnValue(true);
        } else {
          // 否则
          cir.setReturnValue(false);
        }
      } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
        MelodySkyPlus.LOGGER.fatal("Cannot find whole melodysky.");
        throw new RuntimeException(e);
      }

    }
  }
}
