package net.mirolls.melodyskyplus;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.mirolls.melodyskyplus.event.CheckAir;
import net.mirolls.melodyskyplus.event.CheckPlayerFlying;
import net.mirolls.melodyskyplus.libs.RotationLib;
import net.mirolls.melodyskyplus.libs.WalkLib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MelodySkyPlus.MODID, version = MelodySkyPlus.VERSION)
public class MelodySkyPlus {
  public static final String MODID = "melodyskyplus";
  public static final String VERSION = "1.0";
  public static final Logger LOGGER = LogManager.getLogger(MelodySkyPlus.MODID);

  public static RotationLib rotationLib;
  public static CheckPlayerFlying checkPlayerFlying;
  public static WalkLib walkLib;
  public static CheckAir checkAir;


  @EventHandler
  public void init(FMLInitializationEvent event) {
    LOGGER.info("MelodySky+ is running");

    rotationLib = new RotationLib();
    checkPlayerFlying = new CheckPlayerFlying();
    walkLib = new WalkLib();
    checkAir = new CheckAir();
    // events
  }
}
