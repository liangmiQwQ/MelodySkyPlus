package net.mirolls.melodyskyplus.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.*;

public class EtherWarpUtils {
  private Map<BlockPos, IBlockState> blockStateMap = new HashMap<>();

  public List<BlockPos> findWayToEtherWarp(BlockPos end) {
    return findWayToEtherWarp(1, end);
  }

  public List<BlockPos> findWayToEtherWarp(int layer, BlockPos end) {
    List<BlockPos> routes = findRoutesWithLayer(layer, end);
    if (routes.isEmpty()) {
      return findWayToEtherWarp(layer + 1, end);
    }

    return routes;
  }

  private List<BlockPos> findRoutesWithLayer(int layer, BlockPos end) {
    if (layer == 1) {
      if (PlayerUtils.rayTrace(end)) {
        return new ArrayList<>(Collections.singletonList(end));
      }
    }
  }


  private IBlockState getBlockState(BlockPos pos) {
    if (blockStateMap.containsKey(pos)) {
      return blockStateMap.get(pos);
    } else {
      IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(pos);
      blockStateMap.put(pos, blockState);
      return blockState;
    }
  }
}
