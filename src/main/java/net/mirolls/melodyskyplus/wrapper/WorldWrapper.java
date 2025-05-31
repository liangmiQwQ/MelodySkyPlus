package net.mirolls.melodyskyplus.wrapper;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class WorldWrapper {
  private final World world;

  public WorldWrapper() {
    world = Minecraft.getMinecraft().theWorld;
  }

  public WorldWrapper(World world) {
    this.world = world;
  }

  /**
   * Performs a ray trace in the world from start to end vector.
   *
   * @param start                    The starting point of the ray.
   * @param end                      The ending point of the ray.
   * @param stopOnLiquid             Whether to stop the trace when hitting a liquid.
   * @param ignoreNonCollidableBlock If true, skip blocks with no collision bounding box.
   * @param returnLastMissed         If true, return the last missed block position if no collision is found.
   * @return The result of the ray trace, or null if no valid hit was found.
   */
  public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end, boolean stopOnLiquid, boolean ignoreNonCollidableBlock, boolean returnLastMissed) {
    if (!Double.isNaN(start.xCoord) && !Double.isNaN(start.yCoord) && !Double.isNaN(start.zCoord)) {
      if (!Double.isNaN(end.xCoord) && !Double.isNaN(end.yCoord) && !Double.isNaN(end.zCoord)) {
        int i = MathHelper.floor_double(end.xCoord);
        int j = MathHelper.floor_double(end.yCoord);
        int k = MathHelper.floor_double(end.zCoord);
        int l = MathHelper.floor_double(start.xCoord);
        int i1 = MathHelper.floor_double(start.yCoord);
        int j1 = MathHelper.floor_double(start.zCoord);
        BlockPos blockpos = new BlockPos(l, i1, j1);
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        // 检查起点所在方块是否可以碰撞，且是否有碰撞箱
        if (
            (!ignoreNonCollidableBlock || block.getCollisionBoundingBox(world, blockpos, iblockstate) != null) // 条件1
                && block.canCollideCheck(iblockstate, stopOnLiquid)
        ) {
          MovingObjectPosition movingobjectposition = block.collisionRayTrace(world, blockpos, start, end);
          if (movingobjectposition != null) {
            return movingobjectposition;
          }
        }

        MovingObjectPosition movingobjectposition2 = null;
        int k1 = 200;

        while (k1-- >= 0) {
          // 安全性检查
          if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord)) {
            return null;
          }

          if (l == i && i1 == j && j1 == k) {
            return returnLastMissed ? movingobjectposition2 : null;
          }

          boolean flag2 = true;
          boolean flag = true;
          boolean flag1 = true;
          double d0 = 999.0F;
          double d1 = 999.0F;
          double d2 = 999.0F;
          if (i > l) {
            d0 = (double) l + (double) 1.0F;
          } else if (i < l) {
            d0 = (double) l + (double) 0.0F;
          } else {
            flag2 = false;
          }

          if (j > i1) {
            d1 = (double) i1 + (double) 1.0F;
          } else if (j < i1) {
            d1 = (double) i1 + (double) 0.0F;
          } else {
            flag = false;
          }

          if (k > j1) {
            d2 = (double) j1 + (double) 1.0F;
          } else if (k < j1) {
            d2 = (double) j1 + (double) 0.0F;
          } else {
            flag1 = false;
          }

          double d3 = 999.0F;
          double d4 = 999.0F;
          double d5 = 999.0F;
          double d6 = end.xCoord - start.xCoord;
          double d7 = end.yCoord - start.yCoord;
          double d8 = end.zCoord - start.zCoord;
          if (flag2) {
            d3 = (d0 - start.xCoord) / d6;
          }

          if (flag) {
            d4 = (d1 - start.yCoord) / d7;
          }

          if (flag1) {
            d5 = (d2 - start.zCoord) / d8;
          }

          if (d3 == (double) -0.0F) {
            d3 = -1.0E-4;
          }

          if (d4 == (double) -0.0F) {
            d4 = -1.0E-4;
          }

          if (d5 == (double) -0.0F) {
            d5 = -1.0E-4;
          }

          EnumFacing enumfacing;
          if (d3 < d4 && d3 < d5) {
            enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
            start = new Vec3(d0, start.yCoord + d7 * d3, start.zCoord + d8 * d3);
          } else if (d4 < d5) {
            enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
            start = new Vec3(start.xCoord + d6 * d4, d1, start.zCoord + d8 * d4);
          } else {
            enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
            start = new Vec3(start.xCoord + d6 * d5, start.yCoord + d7 * d5, d2);
          }

          l = MathHelper.floor_double(start.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
          i1 = MathHelper.floor_double(start.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
          j1 = MathHelper.floor_double(start.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
          blockpos = new BlockPos(l, i1, j1);
          IBlockState iblockstate1 = world.getBlockState(blockpos);
          Block block1 = iblockstate1.getBlock();
          // 第二重检查
          if (!ignoreNonCollidableBlock || block1.getCollisionBoundingBox(world, blockpos, iblockstate1) != null) {
            if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {

              MovingObjectPosition movingobjectposition1 = getMovingObjectPosition(start, end, block1, blockpos, block);

              if (movingobjectposition1 != null) {
                return movingobjectposition1;
              }
            } else {
              movingobjectposition2 = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, start, enumfacing, blockpos);
            }
          }
        }

        return returnLastMissed ? movingobjectposition2 : null;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  private MovingObjectPosition getMovingObjectPosition(Vec3 start, Vec3 end, Block block1, BlockPos blockpos, Block block) {
    // 修改为：
    MovingObjectPosition movingobjectposition1;

    // 检查是否是需要特殊处理的不完整方块
    if (block1 instanceof BlockLadder ||
        block1 instanceof BlockPane ||
        block1 instanceof BlockFence ||
        block1 instanceof BlockWall ||
        block1 instanceof BlockHalfStoneSlab ||
        block1 instanceof BlockHalfStoneSlabNew ||
        block1 instanceof BlockHalfWoodSlab) {

      // 创建完整方块（1x1x1）的碰撞箱
      AxisAlignedBB fullBlockBB = new AxisAlignedBB(
          blockpos.getX(), blockpos.getY(), blockpos.getZ(),
          blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1
      );

      // 使用完整碰撞箱进行射线检测
      movingobjectposition1 = fullBlockBB.calculateIntercept(start, end);

      // 如果检测到碰撞点，构造完整的碰撞结果
      if (movingobjectposition1 != null) {
        // 计算碰撞面（基于射线方向）
        EnumFacing hitSide = calculateHitSide(movingobjectposition1.hitVec, blockpos);

        // 创建完整的碰撞结果对象
        movingobjectposition1 = new MovingObjectPosition(
            MovingObjectPosition.MovingObjectType.BLOCK,
            movingobjectposition1.hitVec,
            hitSide,
            blockpos
        );
      }
    } else {
      // 其他方块使用原始检测方法
      movingobjectposition1 = block.collisionRayTrace(world, blockpos, start, end);
    }
    return movingobjectposition1;
  }

  private EnumFacing calculateHitSide(Vec3 hitVec, BlockPos blockPos) {
    // 计算碰撞点相对于方块的位置（小数部分）
    double relX = hitVec.xCoord - blockPos.getX();
    double relY = hitVec.yCoord - blockPos.getY();
    double relZ = hitVec.zCoord - blockPos.getZ();

    // 计算到各面的距离
    double distEast = 1.0 - relX;
    double distUp = 1.0 - relY;
    double distSouth = 1.0 - relZ;

    // 找到最小距离的面
    double minDist = relX;
    EnumFacing hitFace = EnumFacing.WEST;

    if (distEast < minDist) {
      minDist = distEast;
      hitFace = EnumFacing.EAST;
    }
    if (relY < minDist) {
      minDist = relY;
      hitFace = EnumFacing.DOWN;
    }
    if (distUp < minDist) {
      minDist = distUp;
      hitFace = EnumFacing.UP;
    }
    if (relZ < minDist) {
      minDist = relZ;
      hitFace = EnumFacing.NORTH;
    }
    if (distSouth < minDist) {
      hitFace = EnumFacing.SOUTH;
    }

    return hitFace;
  }
}

