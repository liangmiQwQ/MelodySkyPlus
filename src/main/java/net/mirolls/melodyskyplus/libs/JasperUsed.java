package net.mirolls.melodyskyplus.libs;

import xyz.Melody.Event.value.Option;

public class JasperUsed {
  public final Option<Boolean> autoUseJasper = new Option<>("AutoUseJasper", true);
  public int minedBlock = 0;
  private boolean jasperUsed = false;

  public JasperUsed() {
  }

  public boolean isJasperUsed() {
    return jasperUsed;
  }

  public void setJasperUsed(boolean jasperUsed) {
    this.jasperUsed = jasperUsed;
  }


}
