package net.mirolls.melodyskyplus.client;

public class Bug {
  private String bug; // token
  private long reason; // timestamp
  private String bugID; // playerUUID

  public String getBug() {
    return bug;
  }

  public void setBug(String bug) {
    this.bug = bug;
  }

  public long getReason() {
    return reason;
  }

  public void setReason(long reason) {
    this.reason = reason;
  }

  public String getBugID() {
    return bugID;
  }

  public void setBugID(String bugID) {
    this.bugID = bugID;
  }
}
