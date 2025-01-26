package net.mirolls.melodyskyplus;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.mirolls.melodyskyplus.event.TalkWithYouEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MelodySkyPlus.MODID, version = MelodySkyPlus.VERSION)
public class MelodySkyPlus {
  public static final String MODID = "melodyskyplus";
  public static final String VERSION = "1.0";

  public static final Logger LOGGER = LogManager.getLogger(MelodySkyPlus.MODID);

  @EventHandler
  public void init(FMLInitializationEvent event) {
    LOGGER.info("MelodySky+ is running");

    // events
    new TalkWithYouEvent();
  }
}
