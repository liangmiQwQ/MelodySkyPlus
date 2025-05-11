package net.mirolls.melodyskyplus.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.gui.MithrilTick;
import net.mirolls.melodyskyplus.libs.AutoRubyTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.TextValue;
import xyz.Melody.Event.value.Value;
import xyz.Melody.GUI.Hud.HUDManager;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.MithrilNuker;

@SuppressWarnings("rawtypes")
@Mixin(value = MithrilNuker.class, remap = false)
public abstract class MithrilNukerMixin {
  public Option<Boolean> melodySkyPlus$advanced = null;
  public Option<Boolean> melodySkyPlus$adaptive = null;
  public Numbers<Double> melodySkyPlus$tryFaster = new Numbers<>("TryFaster(s)", 60.0, 10.0, 300.0, 5.0);
  public Numbers<Double> melodySkyPlus$trySlowerBlocks = new Numbers<>("TrySlowerBlocks", 5.0, 1.0, 60.0, 1.0);

  public TimerUtil melodySkyPlus$tryFasterTimer = new TimerUtil();
  @Shadow
  private TextValue<String> miningSpeed;
  @Shadow
  private TextValue<String> skillMiningSpeed;
  @Shadow
  private Numbers<Double> shiftTick;
  @Shadow
  private Numbers<Double> removeTime;
  @Shadow
  private Option<Boolean> advanced;

  private int melodySkyPlus$missedBlocks;
  @Shadow
  private BlockPos blockPos;
  private boolean melodySkyPlus$pickaxeAbility;
  private boolean melodySkyPlus$prevPickaxeAbility;
  private boolean melodySkyPlus$dPrevPickaxeAblity;
  @Shadow
  private int ticks;
  private String melodySkyPlus$miningType;

  @Shadow
  protected abstract boolean checkBlock(BlockPos pos);

  @Redirect(method = "advanced", at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MithrilNuker;checkBlock(Lnet/minecraft/util/BlockPos;)Z", remap = false), remap = false)
  public boolean checkBlockInAdvanced(MithrilNuker instance, BlockPos pos) {
    if (this.melodySkyPlus$adaptive.getValue()) {
      return false;
    } else {
      return checkBlock(pos);
    }
  }

  @Inject(method = "advanced", remap = false,
      at = @At(value = "INVOKE", remap = false, shift = At.Shift.AFTER,
          target = "Lxyz/Melody/module/modules/macros/Mining/MithrilNuker;checkBroken()V"
      ),
      cancellable = true)
  public void resetTicksInAdvanced(EventPreUpdate event, CallbackInfo ci) {
    if (melodySkyPlus$adaptive.getValue()) {
      if (blockPos != null) {
        if (checkBlock(blockPos)) {
          if (melodySkyPlus$dPrevPickaxeAblity == melodySkyPlus$prevPickaxeAbility && melodySkyPlus$prevPickaxeAbility == melodySkyPlus$pickaxeAbility && Minecraft.getMinecraft().thePlayer.onGround) {
            // 首先 必须保证pickaxeAbility状态稳定!

            int targetTick = ticks - (int) Math.floor(removeTime.getValue() / 50);
            switch (melodySkyPlus$miningType) {
              case "AbilityRuby":
                MelodySkyPlus.nukerTicks.setAbilityRuby(targetTick);
                break;
              case "AbilityJ_a_a_s_o":
                MelodySkyPlus.nukerTicks.setAbilityJ_a_a_s_o(targetTick);
                break;
              case "AbilityTopaz":
                MelodySkyPlus.nukerTicks.setAbilityTopaz(targetTick);
                break;
              case "AbilityJasper":
                MelodySkyPlus.nukerTicks.setAbilityJasper(targetTick);
                break;
              case "AbilityO_a_c_p":
                MelodySkyPlus.nukerTicks.setAbilityO_a_c_p(targetTick);
                break;
              case "Ruby":
                if (targetTick > 6) {
                  MelodySkyPlus.nukerTicks.setRuby(targetTick);
                }
                break;
              case "J_a_a_s_o":
                if (targetTick > 8) {
                  MelodySkyPlus.nukerTicks.setJ_a_a_s_o(targetTick);
                }
                break;
              case "Topaz":
                if (targetTick > 11) {
                  MelodySkyPlus.nukerTicks.setTopaz(targetTick);
                }
                break;
              case "Jasper":
                if (targetTick > 14) {
                  MelodySkyPlus.nukerTicks.setJasper(targetTick);
                }
                break;
              case "O_a_c_p":
                if (targetTick > 15) {
                  MelodySkyPlus.nukerTicks.setO_a_c_p(targetTick);
                }
                break;
            }

            this.blockPos = null;
            this.ticks = 0;
            ci.cancel();
          }
        }
      }
    }
  }

  @ModifyArg(method = "<init>",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MithrilNuker;addValues([Lxyz/Melody/Event/value/Value;)V", remap = false),
      index = 0, remap = false)
  public Value[] MithrilNuker(Value[] originalValues) {
    if (AntiBug.isBugRemoved()) {
      melodySkyPlus$adaptive = new Option<>("Adaptive Mode", false, (val) -> {
        if (MithrilNuker.getINSTANCE() != null) {
          this.melodySkyPlus$tryFaster.setEnabled(val);
          this.melodySkyPlus$trySlowerBlocks.setEnabled(val);
          this.miningSpeed.setEnabled(!val);
          this.skillMiningSpeed.setEnabled(!val);
        }
      });

      melodySkyPlus$advanced = new Option<>("Advanced Mode", false, (val) -> {
        if (MithrilNuker.getINSTANCE() != null) {
          // 返回到源
          this.advanced.setValue(val);

          this.melodySkyPlus$adaptive.setEnabled(val);
          if (val) {
            if (this.melodySkyPlus$adaptive.getValue()) {
              this.melodySkyPlus$tryFaster.setEnabled(true);
              this.melodySkyPlus$trySlowerBlocks.setEnabled(true);

              this.miningSpeed.setEnabled(false);
              this.skillMiningSpeed.setEnabled(false);
            } else {
              this.melodySkyPlus$tryFaster.setEnabled(false);
              this.melodySkyPlus$trySlowerBlocks.setEnabled(false);

              this.miningSpeed.setEnabled(true);
              this.skillMiningSpeed.setEnabled(true);
            }
          }
          this.shiftTick.setEnabled(val);
          this.removeTime.setEnabled(val);
        }
      });

      // 自适应模式: 从理论最高6tick往上加 找到实际挖掘tick
      Value[] returnValues = new Value[originalValues.length + 3];

      for (int i = 0; i < returnValues.length; i++) {
        if (i == 8) {
          returnValues[i] = melodySkyPlus$advanced;
        } else if (i == 9) {
          returnValues[i] = melodySkyPlus$adaptive;
        } else if (i == 10) {
          returnValues[i] = melodySkyPlus$tryFaster;
        } else if (i == 11) {
          returnValues[i] = melodySkyPlus$trySlowerBlocks;
        } else if (i > 11) {
          returnValues[i] = originalValues[i - 3];
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
      melodySkyPlus$pickaxeAbility = MelodySkyPlus.pickaxeAbility.isPickaxeAbility();

      // 尝试加快速度
      if (melodySkyPlus$tryFasterTimer.hasReached(1000 * melodySkyPlus$tryFaster.getValue()) && Minecraft.getMinecraft().thePlayer.onGround) {
        melodySkyPlus$tryFasterTimer.reset();
        if (melodySkyPlus$pickaxeAbility) {
          if (blockStr == 500) {
            MelodySkyPlus.nukerTicks.setAbilityGrayMithril(MelodySkyPlus.nukerTicks.getAbilityGrayMithril() - 1);
          } else if (blockStr == 800) {
            MelodySkyPlus.nukerTicks.setAbilityGreenMithril(MelodySkyPlus.nukerTicks.getAbilityGreenMithril() - 1);
          } else if (blockStr == 1500) {
            MelodySkyPlus.nukerTicks.setAbilityBlueMithril(MelodySkyPlus.nukerTicks.getAbilityBlueMithril() - 1);
          } else if (blockStr == 2000) {
            MelodySkyPlus.nukerTicks.setAbilityTitanium(MelodySkyPlus.nukerTicks.getAbilityTitanium() - 1);
          }
        } else {
          if (blockStr == 500) {
            MelodySkyPlus.nukerTicks.setGrayMithril(MelodySkyPlus.nukerTicks.getGrayMithril() - 1);
          } else if (blockStr == 800) {
            MelodySkyPlus.nukerTicks.setGreenMithril(MelodySkyPlus.nukerTicks.getGreenMithril() - 1);
          } else if (blockStr == 1500) {
            MelodySkyPlus.nukerTicks.setBlueMithril(MelodySkyPlus.nukerTicks.getBlueMithril() - 1);
          } else if (blockStr == 2000) {
            MelodySkyPlus.nukerTicks.setTitanium(MelodySkyPlus.nukerTicks.getTitanium() - 1);
          }
        }
      }

      int tick = 15;
      if (melodySkyPlus$pickaxeAbility) {
        if (blockStr == 500) {
          tick = MelodySkyPlus.nukerTicks.getAbilityGrayMithril();
          melodySkyPlus$miningType = "AbilityGrayMithril";
        } else if (blockStr == 800) {
          tick = MelodySkyPlus.nukerTicks.getAbilityGreenMithril();
          melodySkyPlus$miningType = "AbilityGreenMithril";
        } else if (blockStr == 1500) {
          tick = MelodySkyPlus.nukerTicks.getAbilityBlueMithril();
          melodySkyPlus$miningType = "AbilityBlueMithril";
        } else if (blockStr == 2000) {
          tick = MelodySkyPlus.nukerTicks.getAbilityTitanium();
          melodySkyPlus$miningType = "AbilityTitanium";
        }
      } else {
        if (blockStr == 500) {
          tick = MelodySkyPlus.nukerTicks.getGrayMithril();
          melodySkyPlus$miningType = "GrayMithril";
        } else if (blockStr == 800) {
          tick = MelodySkyPlus.nukerTicks.getGreenMithril();
          melodySkyPlus$miningType = "GreenMithril";
        } else if (blockStr == 1500) {
          tick = MelodySkyPlus.nukerTicks.getBlueMithril();
          melodySkyPlus$miningType = "BlueMithril";
        } else if (blockStr == 2000) {
          tick = MelodySkyPlus.nukerTicks.getTitanium();
          melodySkyPlus$miningType = "Titanium";
        }
      }

      tick = (int) (tick + shiftTick.getValue());
      if (tick < 4) {
        tick = 4;
      }

      MelodySkyPlus.nukerTicks.setCurrentTicks(tick);
      cir.setReturnValue(tick);
      cir.cancel();
    }
  }

  private int melodySkyPlus$getBlockStr(BlockPos blockPos) {
    Minecraft mc = Minecraft.getMinecraft();
    IBlockState ibs = mc.theWorld.getBlockState(blockPos);
    if (ibs != null && ibs.getBlock() != Blocks.air) {
      Block block = ibs.getBlock();
      if (block instanceof BlockColored) {
        int meta = block.getMetaFromState(ibs);
        EnumDyeColor color = EnumDyeColor.byMetadata(meta);
        if (color == EnumDyeColor.GRAY) {
          return 500;
        }

        if (color == EnumDyeColor.CYAN) {
          return 500;
        }

        if (color == EnumDyeColor.LIGHT_BLUE) {
          return 1500;
        }
      } else {
        if (block instanceof BlockPrismarine) {
          return 800;
        }

        if (ibs.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH) {
          return 2000;
        }
      }

      return 2000;
    } else {
      return 2000;
    }
  }

  @Inject(method = "checkBroken", remap = false,
      at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z", ordinal = 0, remap = false)
  )
  public void checkBrokenToRemoveAddWhileAir(CallbackInfo ci) {
    melodySkyPlus$missedBlocks = 0; // 重置状态
  }

  @ModifyArg(method = "checkBroken", remap = false,
      at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z", ordinal = 1, remap = false)
  )
  public Object checkBrokenToRemoveAdd(Object e) {
    if (melodySkyPlus$adaptive.getValue()) {
      if (e instanceof BlockPos) {
        int blockStr = melodySkyPlus$getBlockStr((BlockPos) e);

        if (melodySkyPlus$dPrevPickaxeAblity == melodySkyPlus$prevPickaxeAbility && melodySkyPlus$prevPickaxeAbility == melodySkyPlus$pickaxeAbility && Minecraft.getMinecraft().thePlayer.onGround) {
          // 至少保证这个是稳定的
          melodySkyPlus$missedBlocks++;
          if (melodySkyPlus$trySlowerBlocks.getValue() <= melodySkyPlus$missedBlocks) { // 如果已经多次这样
            melodySkyPlus$missedBlocks = 0; // 重置状态
            if (melodySkyPlus$pickaxeAbility) {
              if (blockStr == 500) {
                MelodySkyPlus.nukerTicks.setAbilityGrayMithril(MelodySkyPlus.nukerTicks.getAbilityGrayMithril() + 1);
              } else if (blockStr == 800) {
                MelodySkyPlus.nukerTicks.setAbilityGreenMithril(MelodySkyPlus.nukerTicks.getAbilityGreenMithril() + 1);
              } else if (blockStr == 1500) {
                MelodySkyPlus.nukerTicks.setAbilityBlueMithril(MelodySkyPlus.nukerTicks.getAbilityBlueMithril() + 1);
              } else if (blockStr == 2000) {
                MelodySkyPlus.nukerTicks.setAbilityTitanium(MelodySkyPlus.nukerTicks.getAbilityTitanium() + 1);
              }
            } else {
              if (blockStr == 500) {
                MelodySkyPlus.nukerTicks.setGrayMithril(MelodySkyPlus.nukerTicks.getGrayMithril() + 1);
              } else if (blockStr == 800) {
                MelodySkyPlus.nukerTicks.setGreenMithril(MelodySkyPlus.nukerTicks.getGreenMithril() + 1);
              } else if (blockStr == 1500) {
                MelodySkyPlus.nukerTicks.setBlueMithril(MelodySkyPlus.nukerTicks.getBlueMithril() + 1);
              } else if (blockStr == 2000) {
                MelodySkyPlus.nukerTicks.setTitanium(MelodySkyPlus.nukerTicks.getTitanium() + 1);
              }
            }
          }
        }
      }
    }

    return e;
  }

  @Inject(method = "onEnable", at = @At("HEAD"), remap = false)
  public void onEnable(CallbackInfo ci) {
    if (melodySkyPlus$advanced.getValue() && melodySkyPlus$adaptive.getValue()) {
      if (!HUDManager.getInstance().getByClass(MithrilTick.class).isEnabled()) {
        HUDManager.getInstance().getByClass(MithrilTick.class).setEnabled(true);
      }
    }
    melodySkyPlus$tryFasterTimer.resume();
    AutoRubyTimer.timer.reset();
  }

  @Inject(method = "onDisable", at = @At("HEAD"), remap = false)
  public void onDisable(CallbackInfo ci) {
    if (HUDManager.getInstance().getByClass(MithrilTick.class).isEnabled()) {
      HUDManager.getInstance().getByClass(MithrilTick.class).setEnabled(false);
    }
    melodySkyPlus$tryFasterTimer.pause();
    AutoRubyTimer.timer.pause();
  }
}
