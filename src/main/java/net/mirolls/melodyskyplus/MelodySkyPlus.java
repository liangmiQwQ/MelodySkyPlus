package net.mirolls.melodyskyplus;


import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.mirolls.melodyskyplus.client.Bug;
import net.mirolls.melodyskyplus.event.CheckPlayerFlying;
import net.mirolls.melodyskyplus.event.MiningUtil;
import net.mirolls.melodyskyplus.libs.NukerTicks;
import net.mirolls.melodyskyplus.libs.PickaxeAbility;
import net.mirolls.melodyskyplus.libs.RotationLib;
import net.mirolls.melodyskyplus.libs.WalkLib;
import net.mirolls.melodyskyplus.path.PathRenderer;
import net.mirolls.melodyskyplus.path.SmartyPathFinder;
import net.mirolls.melodyskyplus.path.test.CanGoRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = MelodySkyPlus.MODID, version = MelodySkyPlus.VERSION)
public class MelodySkyPlus {
  public static final String MODID = "melodyskyplus";
  public static final String VERSION = "path_finder_testing";
  public static final String MELODY_VERSION = "2.14.7";
  public static final Logger LOGGER = LogManager.getLogger(MelodySkyPlus.MODID);
  public static RotationLib rotationLib;
  public static CheckPlayerFlying checkPlayerFlying;
  public static WalkLib walkLib;
  public static MiningUtil miningUtil;
  public static Bug antiBug;
  public static NukerTicks nukerTicks;
  public static PickaxeAbility pickaxeAbility;
  public static PathRenderer pathRenderer;
  public static SmartyPathFinder smartyPathFinder;
  public static CanGoRenderer canGoRenderer;


  public static String verify(String text) {
    if (text.startsWith("UUID: ")) {
      return "Melody+ Verified: " + (Verify.isVerified() ? EnumChatFormatting.GREEN + "true" : EnumChatFormatting.GRAY + "false");
    } else {
      return text;
    }
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    LOGGER.info("MelodySky+ is running");

    rotationLib = new RotationLib();
    checkPlayerFlying = new CheckPlayerFlying();
    walkLib = new WalkLib();
    miningUtil = new MiningUtil();
    antiBug = new Bug();
    nukerTicks = new NukerTicks();
    pickaxeAbility = new PickaxeAbility();
    smartyPathFinder = new SmartyPathFinder();
    pathRenderer = new PathRenderer();
    canGoRenderer = new CanGoRenderer();
    // events

    try {
      Verify.verify();
    } catch (IOException e) {
      LOGGER.error("Cannot Verify Your Account!" + e);
    }
  }
}
