/**
 * Field.java
 * JRE v1.8.0_40
 *
 * Created by William Myers on Mar 24, 2015.
 * Copyright (c) 2015 William Myers. All Rights reserved.
 */
package shared.model;

/**
 * The Class Field.
 */
public class Field {
  private int    fieldId;
  private int    projectId;
  private String title;
  private String knownData;
  private String helpURL;
  private int    xCoord;
  private int    width;
  private int    colNum;

  /**
   * Instantiates a new Field.
   *
   */
  public Field() {// @formatter:off
    this.fieldId = -1;
    this.projectId = -1;
    this.title = null;
    this.knownData = null;
    this.helpURL = null;
    this.xCoord = -1;
    this.width = -1;
    this.colNum = -1;
  }

  /**
   * Instantiates a new Field.
   *
   * @param projectId the project id
   * @param title the title
   * @param knownData the known data
   * @param helpURL the help url
   * @param xCoord the x coord
   * @param width the width
   * @param colNum the col num
   */
  public Field(int projectId, String title, String knownData, String helpURL, int xCoord,
      int width, int colNum) {
    //System.out.println("-1");
    //System.out.println(projectId);
    //System.out.println(title.toString());
    //System.out.println(knownData.toString());
    //System.out.println(helpURL.toString());
    //System.out.println(xCoord);
    //System.out.println(width);
    //System.out.println(colNum);
    //System.out.println("-----------------");

    this.fieldId   = -1;
    this.projectId = projectId;
    this.title     = title;
    this.knownData = knownData;
    this.helpURL   = helpURL;
    this.xCoord    = xCoord;
    this.width     = width;
    this.colNum    = colNum;
  }

  /**
   * Instantiates a new Field.
   *
   * @param fieldId the field id
   * @param projectId the project id
   * @param title the title
   * @param knownData the known data
   * @param helpURL the help url
   * @param xCoord the x coord
   * @param width the width
   * @param colNum the col num
   */
  public Field(int fieldId, int projectId, String title, String knownData, String helpURL,
      int xCoord, int width, int colNum) {
      //System.out.println(fieldId);
      //System.out.println(projectId);
      //System.out.println(title.toString());
      //System.out.println(knownData.toString());
      //System.out.println(helpURL.toString());
      //System.out.println(xCoord);
      //System.out.println(width);
      //System.out.println(colNum);
      //System.out.println("-----------------");

    this.fieldId   = fieldId;
    this.projectId = projectId;
    this.title     = title;
    this.knownData = knownData;
    this.helpURL   = helpURL;
    this.xCoord    = xCoord;
    this.width     = width;
    this.colNum    = colNum;
  }// @formatter:on

  public int getFieldId() {
    return fieldId;
  }

  public void setFieldId(int fieldId) {
    this.fieldId = fieldId;
  }

  public int getProjectId() {
    return projectId;
  }

  public void setProjectId(int projectId) {
    this.projectId = projectId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getKnownData() {
    return knownData;
  }

  public void setKnownData(String knownData) {
    this.knownData = knownData;
  }

  public String getHelpURL() {
    return helpURL;
  }

  public void setHelpURL(String helpURL) {
    this.helpURL = helpURL;
  }

  public int getXCoord() {
    return xCoord;
  }

  public void setXCoord(int xCoord) {
    this.xCoord = xCoord;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getColNum() {
    return colNum;
  }

  public void setColNum(int colNum) {
    this.colNum = colNum;
  }

  @Override
  public boolean equals(Object o) {
    if ((o == null) || (o.getClass() != this.getClass())) {
      return false;
    }
    if (o == this) {
      return true;
    }

    Field other = (Field) o;
    return ((projectId == other.getProjectId()) // @formatter:off
        && title.equals(other.getTitle())
        && (xCoord == other.getXCoord())
        && (width == other.getWidth())
        && helpURL.equals(other.getHelpURL())
        && knownData.equals(other.getKnownData())
        && (colNum == other.getColNum()));  // @formatter:on
  }

  // @Override
  // public String toString() {
  // String s = "";

  // s += this.fieldId;
  // s += "\n";
  // s += this.projectId;
  // s += "\n";
  // s += this.title.toString();
  // s += "\n";
  // s += this.knownData.toString();
  // s += "\n";
  // s += this.helpURL.toString();
  // s += "\n";
  // s += this.xCoord;
  // s += "\n";
  // s += this.width;
  // s += "\n";
  // s += this.colNum;
  // s += "\n";
  // s += "-----------\n";
  // return s;
  // }
}
