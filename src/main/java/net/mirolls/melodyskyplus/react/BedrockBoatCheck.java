package net.mirolls.melodyskyplus.react;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;

import java.util.Objects;

public class BedrockBoatCheck {
  public static void react() {
    // 判断是单纯的TP还是基岩船+TP
    Minecraft mc = Minecraft.getMinecraft();

    Block block = mc.theWorld.getBlockState(mc.thePlayer.getPosition().down()).getBlock();
    if (Objects.equals(block.getRegistryName(), Blocks.bedrock.getRegistryName())) {
      // 基岩船 or 基岩房子
      // 检测上面有没有基岩来看确定是基岩船还是基岩房子
    } else {
      // 单纯的tp
    }
  }
}

