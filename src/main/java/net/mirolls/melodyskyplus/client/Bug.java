package net.mirolls.melodyskyplus.client;

public class Bug {
  private String bug;

  private Long reason;

  private String bugID;

  public Bug(String bug, long reason, String bugID) {
    this.bug = bug;
    this.reason = reason;
    this.bugID = bugID;
  }

  public Bug() {}

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
