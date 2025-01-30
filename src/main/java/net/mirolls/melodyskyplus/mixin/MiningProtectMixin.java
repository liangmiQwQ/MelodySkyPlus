package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.mirolls.melodyskyplus.libs.CustomPlayerInRange;
import net.mirolls.melodyskyplus.react.NgComeReact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.module.modules.macros.Mining.MiningProtect;

import java.util.Arrays;


@SuppressWarnings("rawtypes")
@Mixin(value = xyz.Melody.module.modules.macros.Mining.MiningProtect.class, remap = false)
public abstract class MiningProtectMixin {

  public Option<Boolean> melodySkyPlus$kickOut = new Option<>("Kick him out", false);
  public Option<Boolean> melodySkyPlus$lookAt = null;

  @Shadow
  public Numbers<Double> resumeTime;
  // 构造函数
  @Shadow
  public Numbers<Double> range;

  @ModifyArg(method = "<init>",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningProtect;addValues([Lxyz/Melody/Event/value/Value;)V", remap = false),
      index = 0)
  public Value[] addValueArgs(Value[] originalValues) {
    // 先初始化一下 避免mixin错误 在此初始化的原因: https://github.com/SpongePowered/Mixin/issues/322
    Option<Boolean> lobby = new Option<>("/lobby", false,
        value -> {
          MiningProtect instance = MiningProtect.getINSTANCE();
          if (instance != null) {
            // 两个小弟
            instance.escapeRange.setEnabled(value);
            instance.escapeTime.setEnabled(value);

            // 互相不支持的2个项目
            this.melodySkyPlus$lookAt.setEnabled(!value);
            this.melodySkyPlus$kickOut.setEnabled(!value);

            instance.lobby.setValue(value); // 反映到源值 允许代码继续运行
          }
        }
    );

    melodySkyPlus$lookAt = new Option<>("Look at him", true,
        value -> {
          MiningProtect instance = MiningProtect.getINSTANCE();
          if (instance != null) {
            // 小弟
            this.melodySkyPlus$kickOut.setEnabled(value);

            // 互不支持的项目
            lobby.setEnabled(!value);
            instance.escapeRange.setEnabled(!value);
            instance.escapeTime.setEnabled(!value);
          }
        }
    );


    Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 2);

    returnValues[returnValues.length - 2] = melodySkyPlus$lookAt;
    returnValues[returnValues.length - 1] = melodySkyPlus$kickOut;
    returnValues[1] = lobby;

    return returnValues;
  }

  @Inject(method = "checkPause", at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningProtect;disableMacros()V", remap = false), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
  private void checkPause(CallbackInfo ci, Object[] info) {
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayer targetPlayer = CustomPlayerInRange.findPlayer((String) info[1]);
    NgComeReact.react(mc, targetPlayer, melodySkyPlus$kickOut.getValue(), melodySkyPlus$lookAt.getValue(), resumeTime.getValue(), range.getValue());
  }
}
