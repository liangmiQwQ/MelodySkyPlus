package net.mirolls.melodyskyplus.utils;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public class BlockStateStoreUtils {
  private final Map<BlockPos, IBlockState> blockStateMap = new HashMap<>();

  public IBlockState getBlockState(BlockPos bp) {
    if (blockStateMap.containsKey(bp)) {
      return blockStateMap.get(bp);
    } else {
      IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(bp);
      blockStateMap.put(bp, blockState);

      return blockState;
    }
  }
}
