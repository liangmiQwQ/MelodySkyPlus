package net.mirolls.melodyskyplus.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.mirolls.melodyskyplus.Verify;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.modules.macros.Mining.HardStoneNuker;
import xyz.Melody.module.modules.macros.Mining.RouteHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@Mixin(value = HardStoneNuker.class, remap = false)
public class HardStoneNukerMixin {
  private final Option<Boolean> melodySkyPlus$routeMiner = new Option<>("RouteMiner", true);
  @Shadow
  private Numbers<Double> range;
  @Shadow
  private Numbers<Double> height;
  @Shadow
  private ArrayList<BlockPos> broken;
  @Shadow
  private Option<Boolean> ores;

  @SuppressWarnings("rawtypes")
  @ModifyArg(method = "<init>",
      at = @At(value = "INVOKE", remap = false, target = "Lxyz/Melody/module/modules/macros/Mining/HardStoneNuker;addValues([Lxyz/Melody/Event/value/Value;)V"),
      index = 0)
  public Value[] addValueArgs(Value[] originalValues) {
    if (Verify.isVerified() && AntiBug.isBugRemoved()) {
      Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 1);
      returnValues[returnValues.length - 1] = melodySkyPlus$routeMiner;

      return returnValues;
    }

    return originalValues;
  }


  @Inject(method = "closestStone", at = @At("HEAD"), remap = false, cancellable = true)
  private void closestStone(CallbackInfoReturnable<BlockPos> cir) {
    if (Verify.isVerified() && AntiBug.isBugRemoved() && melodySkyPlus$routeMiner.getValue()) {
      Minecraft mc = Minecraft.getMinecraft();

      if (mc.theWorld != null && mc.thePlayer != null) {
        // 获取搜索范围参数
        float range = this.range.getValue().floatValue();
        double height = this.height.getValue();

        // 获取玩家位置（脚部位置），并向上调整1格（大约到身体中心位置）
        BlockPos playerPos = mc.thePlayer.getPosition().up(1);

        // 定义搜索区域：
        // 水平方向：range格
        // 垂直方向：height格（向上）和1格（向下）
        Vec3i searchArea = new Vec3i(range, height, range);
        Vec3i depthArea = new Vec3i(range, 1.0F, range);  // 向下搜索1格

        // 存储找到的符合条件的方块位置
        ArrayList<Vec3> foundBlocks = new ArrayList<>();

        if (playerPos != null) {
          // 遍历以玩家为中心的定义区域内的所有方块位置
          for (BlockPos blockPos : BlockPos.getAllInBox(
              playerPos.add(searchArea),     // 最大坐标（+x, +y, +z）
              playerPos.subtract(depthArea))) // 最小坐标（-x, -y, -z）
          {

            RouteHelper routeHelper = (RouteHelper) new ModuleManager().getModuleByClass(RouteHelper.class);
            if (routeHelper != null) {
              // 反射获取blocksList

              try {
                Class<?> clazz = routeHelper.getClass();
                Field field = clazz.getDeclaredField("routes");
                field.setAccessible(true);

                @SuppressWarnings("unchecked")
                ArrayList<BlockPos> routes = (ArrayList<BlockPos>) field.get(routeHelper);

                if (routes.contains(blockPos)) {
                  // 基础检查通过
                  // 获取该位置的方块状态
                  IBlockState blockState = mc.theWorld.getBlockState(blockPos);

                  // 检查是否是石头方块且未被破坏过
                  if (blockState.getBlock() == Blocks.stone && !this.broken.contains(blockPos)) {
                    // 将方块中心位置（坐标+0.5）加入列表
                    foundBlocks.add(new Vec3(
                        blockPos.getX() + 0.5F,
                        blockPos.getY(),
                        blockPos.getZ() + 0.5F));
                  }

                  // 如果开启了矿石搜索，检查是否是各种矿石方块
                  if (ores.getValue()) {
                    Block block = blockState.getBlock();
                    // 检查各种矿石类型
                    boolean isOre = block == Blocks.diamond_ore ||
                        block == Blocks.gold_ore ||
                        block == Blocks.iron_ore ||
                        block == Blocks.coal_ore ||
                        block == Blocks.lapis_ore ||
                        block == Blocks.redstone_ore ||
                        block == Blocks.emerald_ore ||
                        block == Blocks.quartz_ore;

                    if (isOre && !this.broken.contains(blockPos)) {
                      foundBlocks.add(new Vec3(
                          blockPos.getX() + 0.5F,
                          blockPos.getY(),
                          blockPos.getZ() + 0.5F));
                    }
                  }
                }

              } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            }


          }
        }

        // 按距离玩家从近到远排序
        foundBlocks.sort(Comparator.comparingDouble(vec ->
            mc.thePlayer.getDistance(
                vec.xCoord,
                vec.yCoord,
                vec.zCoord)));

        // 返回最近的方块位置（如果有的话）
        cir.setReturnValue(!foundBlocks.isEmpty() ?
            new BlockPos(
                foundBlocks.get(0).xCoord,
                foundBlocks.get(0).yCoord,
                foundBlocks.get(0).zCoord) :
            null);
      } else {
        cir.setReturnValue(null);
      }
      cir.cancel();
    }
  }
}
