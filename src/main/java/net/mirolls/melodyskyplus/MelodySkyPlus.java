package net.mirolls.melodyskyplus;


import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.mirolls.melodyskyplus.client.Bug;
import net.mirolls.melodyskyplus.libs.*;
import net.mirolls.melodyskyplus.path.PathRenderer;
import net.mirolls.melodyskyplus.path.exec.PathExec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MelodySkyPlus.MODID, version = MelodySkyPlus.VERSION)
public class MelodySkyPlus {
  public static final String MODID = "melodyskyplus";
  public static final String VERSION = "2.0.0";
  public static final Logger LOGGER = LogManager.getLogger(MelodySkyPlus.MODID);
  public static RotationLib rotationLib;
  public static CheckPlayerFlying checkPlayerFlying;
  public static WalkLib walkLib;
  public static MiningUtil miningUtil;
  public static Bug antiBug;
  public static NukerTicks nukerTicks;
  public static JasperUsed jasperUsed;
  public static PickaxeAbility pickaxeAbility;
  public static PathRenderer pathRenderer;
  public static PathExec pathExec;
  public static PackRecord packRecord;
  public static MiningSkillExecutor miningSkillExecutor;
  public static DrinkingLib drinkingLib;
  public static NewBlueEgg newBlueEgg;


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
    jasperUsed = new JasperUsed();
    pickaxeAbility = new PickaxeAbility();
    pathExec = new PathExec();
    pathRenderer = new PathRenderer();
    packRecord = new PackRecord();
    miningSkillExecutor = new MiningSkillExecutor();
    newBlueEgg = new NewBlueEgg();
    drinkingLib = new DrinkingLib();
    // events
  }
}
