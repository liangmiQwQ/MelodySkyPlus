package net.mirolls.melodyskyplus.mixin;

import java.lang.reflect.Field;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.Event.value.*;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.module.modules.macros.Mining.MiningSkill;

@Mixin(value = MiningSkill.class, remap = false)
public class MiningSkillMixin {
  @Shadow public TextValue<String> ready;
  @Shadow public TextValue<String> used;
  @Shadow public TextValue<String> expire;
  public Option<Boolean> melodySkyPlus$useRod = new Option<>("Use Rod", false);
  public Option<Boolean> melodySkyPlus$autoMode;
  public Option<Boolean> melodySkyPlus$newBlueEgg = new Option<>("New BlueEgg",false);
  public Numbers<Double> melodySkyPlus$blueSlot = new Numbers("New EggDrill Slot", (double)5.0F, (double)1.0F, (double)8.0F, (double)1.0F, new IValAction[0]);

  @SuppressWarnings("rawtypes")
  @ModifyArg(
      method = "<init>",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lxyz/Melody/module/modules/macros/Mining/MiningSkill;addValues([Lxyz/Melody/Event/value/Value;)V",
              remap = false),
      index = 0)
  public Value[] addValueArgs(Value[] originalValues) {
    if (AntiBug.isBugRemoved()) {

      melodySkyPlus$autoMode =
          new Option<>(
              "Auto Mode",
              true,
              (val) -> {
                if (MiningSkill.getINSTANCE() != null) {
                  MelodySkyPlus.pickaxeAbility.check = val;

                  ready.setValue(val ? "🪷𬺈〾🝼⇌🝼⻯" : "Mining Speed Boost is now available!");
                  ready.setEnabled(!val);
                  used.setValue(
                      val ? "🪷𬺈〾🝼⇌🝼⻯" : "You used your Mining Speed Boost Pickaxe Ability!");
                  used.setEnabled(!val);
                  expire.setValue(val ? "🪷𬺈〾🝼⇌🝼⻯" : "Your Mining Speed Boost has expired!");
                  expire.setEnabled(!val);
                }
              });

      Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 4);
      returnValues[returnValues.length - 4] = melodySkyPlus$newBlueEgg;
      returnValues[returnValues.length - 3] = melodySkyPlus$blueSlot;
      returnValues[returnValues.length - 2] = melodySkyPlus$autoMode;
      returnValues[returnValues.length - 1] = melodySkyPlus$useRod;

      return returnValues;
    }

    return originalValues;
  }

  @Inject(method = "tryPerformSkill", at = @At("HEAD"), cancellable = true, remap = false)
  public void tryPerformSkill(CallbackInfoReturnable<Boolean> cir) {
    MelodySkyPlus.pickaxeAbility.check = melodySkyPlus$autoMode.getValue();

    Minecraft mc = Minecraft.getMinecraft();
    if (System.currentTimeMillis() % 140 * 1000 == 0 && false) {
      // 140秒尝试重新处理一次

      if (mc.thePlayer.getHeldItem() != null) {
        String id = ItemUtils.getSkyBlockID(mc.thePlayer.getHeldItem());
        if (mc.thePlayer.getHeldItem().getItem() == Items.prismarine_shard
            || id.contains("GEMSTONE_GAUNTLET")
            || mc.thePlayer.getHeldItem().getItem() instanceof ItemPickaxe) {
          mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        }
      }
    }
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
    if (AntiBug.isBugRemoved() && melodySkyPlus$newBlueEgg.getValue()) {
        try {
            // 读 看是否应该执行
            Class<?> client = Class.forName("xyz.Melody.Client");
            Field pickaxeField = client.getDeclaredField("pickaxeAbilityReady");
            pickaxeField.setAccessible(true);

            // 若应该执行
            if ((boolean) pickaxeField.get(null)) {
                MelodySkyPlus.newBlueEgg.start(melodySkyPlus$blueSlot.getValue().intValue() - 1);

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
    if (AntiBug.isBugRemoved() && melodySkyPlus$newBlueEgg.getValue()) {
        try {
            // 读 看是否应该执行
            Class<?> client = Class.forName("xyz.Melody.Client");
            Field pickaxeField = client.getDeclaredField("pickaxeAbilityReady");
            pickaxeField.setAccessible(true);

            // 若应该执行
            if ((boolean) pickaxeField.get(null)) {
                MelodySkyPlus.newBlueEgg.start(melodySkyPlus$blueSlot.getValue().intValue() - 1);

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
