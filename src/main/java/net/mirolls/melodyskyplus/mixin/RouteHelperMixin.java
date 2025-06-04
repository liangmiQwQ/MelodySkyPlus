package net.mirolls.melodyskyplus.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.utils.BlockUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;
import xyz.Melody.module.modules.macros.Mining.MiningSkill;
import xyz.Melody.module.modules.macros.Mining.RouteHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mixin(value = RouteHelper.class, remap = false)
public class RouteHelperMixin {
  public Option<Boolean> melodySkyPlus$liteMode;
  public Option<Boolean> melodySkyPlus$2BlockHeight;
  @Shadow
  private ArrayList<Vec3d> routeVecs;

  @Shadow
  private HashMap<BlockPos, TimerUtil> broken;

  @Shadow
  private ArrayList<BlockPos> routes;

  @Inject(method = "<init>", at = @At("RETURN"), remap = false)
  public void init(CallbackInfo ci) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (AntiBug.isBugRemoved()) {
      melodySkyPlus$liteMode = new Option<>("Lite Mode", true, (val) -> {
        if (MiningSkill.getINSTANCE() != null) {
          melodySkyPlus$2BlockHeight.setEnabled(val);
        }
      });
      melodySkyPlus$2BlockHeight = new Option<>("2-Block Height", false);


      Method method = Module.class.getDeclaredMethod("addValues", Value[].class);
      method.setAccessible(true);
      method.invoke(this, (Object) new Value[]{melodySkyPlus$liteMode, melodySkyPlus$2BlockHeight});
    }
  }

  @Inject(method = "calculate", at = @At("HEAD"), remap = false, cancellable = true)
  private void calculate(CallbackInfo ci) {
    if (AntiBug.isBugRemoved() && melodySkyPlus$liteMode.getValue()) {
      Minecraft mc = Minecraft.getMinecraft();
      AutoRuby ar = Objects.requireNonNull(AutoRuby.getINSTANCE());

      List<BlockPos> blocksBetween = new ArrayList<>();
      routeVecs.clear();
      for (int i = 0; i < ar.wps.size(); ++i) {
        Vec3d foot = Vec3d.ofCenter(ar.wps.get(i));
        Vec3d cur = new Vec3d(foot.getX(), ar.wps.get(i).getY() + 1 + mc.thePlayer.getEyeHeight(), foot.getZ());
        Vec3d next = Vec3d.ofCenter(i + 1 == ar.wps.size() ? ar.wps.get(0) : ar.wps.get(i + 1));

        this.routeVecs.add(cur);
        this.routeVecs.add(next);

        blocksBetween.addAll(melodySkyPlus$2BlockHeight.getValue() ? BlockUtils.getDoubleHeightBlocksBetween(cur, next) : BlockUtils.getBlocksBetween(cur, next));
      }

      List<BlockPos> result = new ArrayList<>();
      for (BlockPos bp : blocksBetween) {
        // 下一步: 继续筛选石头
        if (!this.broken.containsKey(bp)) {
          IBlockState blockState = mc.theWorld.getBlockState(bp);
          Block block = blockState.getBlock();
          boolean isMithril = block == Blocks.wool || block == Blocks.prismarine;
          if (isMithril
              || block == Blocks.stone
              || block == Blocks.iron_ore
              || block == Blocks.gravel
              || block == Blocks.gold_ore
              || block == Blocks.emerald_ore
              || block == Blocks.diamond_ore
              || block == Blocks.redstone_ore
              || block == Blocks.coal_ore
              || block == Blocks.netherrack
              || block == Blocks.lapis_ore
              || block == Blocks.dirt) {
            result.add(bp);
          }
        }
      }

      // 添加到列表
      this.routes.clear();
      this.routes.addAll(result.stream().distinct().collect(Collectors.toList()));

      ci.cancel();
    }
  }
}
