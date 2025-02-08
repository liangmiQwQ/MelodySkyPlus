package net.mirolls.melodyskyplus.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.TextValue;
import xyz.Melody.Event.value.Value;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.GemstoneNuker;

@SuppressWarnings("rawtypes")
@Mixin(value = GemstoneNuker.class, remap = false)
public abstract class GemstoneNukerMixin {
  public Option<Boolean> melodySkyPlus$advanced = null;
  public Option<Boolean> melodySkyPlus$adaptive = null;
  public Numbers<Double> melodySkyPlus$tryFaster = new Numbers<>("TryFaster(s)", 60.0, 10.0, 300.0, 5.0);
  public TimerUtil tryFasterTimer = new TimerUtil();
  @Shadow
  private TextValue<String> miningSpeed;
  @Shadow
  private TextValue<String> skillMiningSpeed;
  @Shadow
  private Numbers<Double> shiftTick;
  @Shadow
  private Numbers<Double> removeTime;

  @Shadow
  private Option<Boolean> pane;
  private boolean melodySkyPlus$pickaxeAbility;
  private boolean melodySkyPlus$prevPickaxeAbility;
  private boolean melodySkyPlus$dPrevPickaxeAblity;

  @ModifyArg(method = "<init>",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/GemstoneNuker;addValues([Lxyz/Melody/Event/value/Value;)V", remap = false),
      index = 0, remap = false)
  public Value[] GemstoneNuker(Value[] originalValues) {
    if (AntiBug.isBugRemoved()) {
      melodySkyPlus$adaptive = new Option<>("Adaptive Mode", false, (val) -> {
        if (GemstoneNuker.getINSTANCE() != null) {
          this.melodySkyPlus$tryFaster.setEnabled(val);
          this.miningSpeed.setEnabled(!val);
          this.skillMiningSpeed.setEnabled(!val);
          this.shiftTick.setEnabled(!val);
        }
      });

      melodySkyPlus$advanced = new Option<>("Advanced Mode", false, (val) -> {
        if (GemstoneNuker.getINSTANCE() != null) {
          this.miningSpeed.setEnabled(val);
          this.skillMiningSpeed.setEnabled(val);
          this.shiftTick.setEnabled(val);
          this.removeTime.setEnabled(val);
          this.melodySkyPlus$adaptive.setEnabled(val);
        }
      });

      // 自适应模式: 从理论最高6tick往上加 找到实际挖掘tick
      Value[] returnValues = new Value[originalValues.length + 1];

      for (int i = 0; i < returnValues.length; i++) {
        if (i == 8) {
          returnValues[i] = melodySkyPlus$advanced;
        } else if (i == 9) {
          returnValues[i] = melodySkyPlus$adaptive;
        } else if (i == 10) {
          returnValues[i] = melodySkyPlus$tryFaster;
        } else if (i > 10) {
          returnValues[i] = originalValues[i - 2];
        } else {
          returnValues[i] = originalValues[i];
        }
      }
      return returnValues;
    } else {
      return originalValues;
    }
  }

  @Inject(method = "getTick", remap = false,
      at = @At("RETURN"),
      cancellable = true)
  public void getTick(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
    // 自适应模式
    if (this.melodySkyPlus$adaptive.getValue()) {
      int blockStr = melodySkyPlus$getBlockStr(pos);

      // 先保存一下目前的pickaxeAbility
      melodySkyPlus$dPrevPickaxeAblity = melodySkyPlus$prevPickaxeAbility;
      melodySkyPlus$prevPickaxeAbility = melodySkyPlus$pickaxeAbility;

      // 开技能了
      melodySkyPlus$pickaxeAbility = melodySkyPlus$abilitiedGetTick(blockStr) == cir.getReturnValue();

      // 尝试加快速度
      if (tryFasterTimer.hasReached(1000 * melodySkyPlus$tryFaster.getValue())) {
        tryFasterTimer.reset();
        if (melodySkyPlus$pickaxeAbility) {
          if (blockStr == 2300) {
            MelodySkyPlus.nukerTicks.setRuby(MelodySkyPlus.nukerTicks.getRuby());
          } else if (blockStr == 3000) {
            MelodySkyPlus.nukerTicks.setJ_a_a_s_o(MelodySkyPlus.nukerTicks.getJ_a_a_s_o());
          } else if (blockStr == 3800) {
            MelodySkyPlus.nukerTicks.setTopaz(MelodySkyPlus.nukerTicks.getTopaz());
          } else if (blockStr == 4800) {
            MelodySkyPlus.nukerTicks.setJasper(MelodySkyPlus.nukerTicks.getJasper());
          } else if (blockStr == 5200) {
            MelodySkyPlus.nukerTicks.setO_a_c_p(MelodySkyPlus.nukerTicks.getO_a_c_p());
          }
        } else {
          if (blockStr == 2300) {
            MelodySkyPlus.nukerTicks.setAbilityRuby(MelodySkyPlus.nukerTicks.getAbilityRuby());
          } else if (blockStr == 3000) {
            MelodySkyPlus.nukerTicks.setAbilityJ_a_a_s_o(MelodySkyPlus.nukerTicks.getAbilityJ_a_a_s_o());
          } else if (blockStr == 3800) {
            MelodySkyPlus.nukerTicks.setAbilityTopaz(MelodySkyPlus.nukerTicks.getAbilityTopaz());
          } else if (blockStr == 4800) {
            MelodySkyPlus.nukerTicks.setAbilityJasper(MelodySkyPlus.nukerTicks.getAbilityJasper());
          } else if (blockStr == 5200) {
            MelodySkyPlus.nukerTicks.setAbilityO_a_c_p(MelodySkyPlus.nukerTicks.getAbilityO_a_c_p());
          }
        }
      }

      if (melodySkyPlus$pickaxeAbility) {
        if (blockStr == 2300) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getRuby());
        } else if (blockStr == 3000) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getJ_a_a_s_o());
        } else if (blockStr == 3800) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getTopaz());
        } else if (blockStr == 4800) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getJasper());
        } else if (blockStr == 5200) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getO_a_c_p());
        }
      } else {
        if (blockStr == 2300) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getAbilityRuby());
        } else if (blockStr == 3000) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getAbilityJ_a_a_s_o());
        } else if (blockStr == 3800) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getAbilityTopaz());
        } else if (blockStr == 4800) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getAbilityJasper());
        } else if (blockStr == 5200) {
          cir.setReturnValue(MelodySkyPlus.nukerTicks.getAbilityO_a_c_p());
        }
      }

      cir.cancel();
    }
  }

  private int melodySkyPlus$abilitiedGetTick(int blockStr) {
    float ability = (float) Integer.parseInt(this.skillMiningSpeed.getValue());
    int tick = Math.round(30.0F * blockStr / ability);
    if (tick >= 1 && tick < 4) {
      tick = 4;
    }
    return tick + this.shiftTick.getValue().intValue();
  }

  private int melodySkyPlus$getBlockStr(BlockPos blockPos) {
    Minecraft mc = Minecraft.getMinecraft();
    IBlockState block = mc.theWorld.getBlockState(blockPos);
    if (block.getBlock() != Blocks.stained_glass && block.getBlock() != Blocks.stained_glass_pane) return 5000;
    if (pane.getValue() && block.getBlock() == Blocks.stained_glass_pane) return 5200;

    int metadata = block.getBlock().getMetaFromState(block);

    if (metadata == 0 || metadata == 1 || metadata == 3 || metadata == 5 || metadata == 10) {
      return 3000;
    } else if (metadata == 2) {
      // jasper
      return 4800;
    } else if (metadata == 14) {
      // ruby
      return 2300;
    } else if (metadata == 4) {
      return 3800;
    } else {
      return 5200;
    }
  }

  @ModifyArg(method = "checkBroken", remap = false,
      at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z", ordinal = 1, remap = false)
  )
  public Object checkBrokenToRemoveAdd(Object e) {
    if (e instanceof BlockPos) {
      IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState((BlockPos) e);
      if (blockState.getBlock() == Blocks.stained_glass_pane || blockState.getBlock() == Blocks.stained_glass) {
        int metadata = blockState.getBlock().getMetaFromState(blockState);

        if (melodySkyPlus$dPrevPickaxeAblity == melodySkyPlus$prevPickaxeAbility && melodySkyPlus$prevPickaxeAbility == melodySkyPlus$pickaxeAbility) {
          // 至少保证这个是稳定的
          if (melodySkyPlus$prevPickaxeAbility) {
            // 开技能了
            if (metadata == 0 || metadata == 1 || metadata == 3 || metadata == 5 || metadata == 10) {
              MelodySkyPlus.nukerTicks.setAbilityJ_a_a_s_o(MelodySkyPlus.nukerTicks.getAbilityJ_a_a_s_o() + 1);
            } else if (metadata == 2) {
              // jasper
              MelodySkyPlus.nukerTicks.setAbilityJasper(MelodySkyPlus.nukerTicks.getAbilityJasper() + 1);
            } else if (metadata == 14) {
              // ruby
              MelodySkyPlus.nukerTicks.setAbilityRuby(MelodySkyPlus.nukerTicks.getAbilityRuby() + 1);
            } else if (metadata == 4) {
              // topaz
              MelodySkyPlus.nukerTicks.setAbilityTopaz(MelodySkyPlus.nukerTicks.getAbilityTopaz() + 1);
            } else {
              MelodySkyPlus.nukerTicks.setAbilityO_a_c_p(MelodySkyPlus.nukerTicks.getAbilityO_a_c_p() + 1);
            }
          } else {
            if (metadata == 0 || metadata == 1 || metadata == 3 || metadata == 5 || metadata == 10) {
              MelodySkyPlus.nukerTicks.setJ_a_a_s_o(MelodySkyPlus.nukerTicks.getJ_a_a_s_o() + 1);
            } else if (metadata == 2) {
              // jasper
              MelodySkyPlus.nukerTicks.setJasper(MelodySkyPlus.nukerTicks.getJasper() + 1);
            } else if (metadata == 14) {
              // ruby
              MelodySkyPlus.nukerTicks.setRuby(MelodySkyPlus.nukerTicks.getRuby() + 1);
            } else if (metadata == 4) {
              // topaz
              MelodySkyPlus.nukerTicks.setTopaz(MelodySkyPlus.nukerTicks.getTopaz() + 1);
            } else {
              MelodySkyPlus.nukerTicks.setO_a_c_p(MelodySkyPlus.nukerTicks.getO_a_c_p() + 1);
            }
          }
        }
      }
    }

    return e;
  }

  @Inject(method = "onEnable", at = @At("HEAD"), remap = false)
  public void onEnable(CallbackInfo ci) {
    tryFasterTimer.reset();
  }
}
