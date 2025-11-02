package de.famst.dicom.visualizer;

import org.dcm4che3.data.VR;


/**
 * Represents a DICOM data element entry with metadata about its position, type, and properties.
 * Each entry corresponds to a single DICOM tag in a DICOM file.
 */
public class DicomEntry
{
  private final int idx;
  private int group;
  private int element;
  private int tag;

  private float level;

  private boolean isPrivateCreator;
  private boolean isPrivateTag;
  private VR vr;
  private int multiplicity;

  private float logLength;
  private float logPosition;

  /**
   * Creates a new DICOM entry with the specified index.
   *
   * @param idx the sequential index of this entry in the DICOM file
   */
  public DicomEntry(int idx)
  {
    if (idx < 0)
    {
      throw new IllegalArgumentException("Index must be non-negative, got: " + idx);
    }
    this.idx = idx;
    this.group = -1;
    this.element = -1;
    this.level = -1;
    this.isPrivateCreator = false;
    this.isPrivateTag = false;
    this.vr = null;
    this.logLength = 0;
    this.multiplicity = 0;
  }

  /**
   * Gets the sequential index of this entry.
   *
   * @return the entry index
   */
  public int getIdx()
  {
    return idx;
  }

  /**
   * Gets the DICOM group number.
   *
   * @return the group number
   */
  public int getGroup()
  {
    return group;
  }

  /**
   * Sets the DICOM group number.
   *
   * @param group the group number to set
   */
  public void setGroup(int group)
  {
    this.group = group;
  }

  /**
   * Gets the DICOM element number.
   *
   * @return the element number
   */
  public int getElement()
  {
    return element;
  }

  /**
   * Sets the DICOM element number.
   *
   * @param element the element number to set
   */
  public void setElement(int element)
  {
    this.element = element;
  }

  /**
   * Gets the nesting level (for sequences).
   *
   * @return the nesting level
   */
  public float getLevel()
  {
    return level;
  }

  /**
   * Sets the nesting level (for sequences).
   *
   * @param level the nesting level to set
   */
  public void setLevel(float level)
  {
    this.level = level;
  }

  /**
   * Checks if this entry is a private creator identification.
   *
   * @return true if this is a private creator, false otherwise
   */
  public boolean isPrivateCreator()
  {
    return isPrivateCreator;
  }

  /**
   * Sets whether this entry is a private creator identification.
   *
   * @param privateCreator true if this is a private creator
   */
  public void setPrivateCreator(boolean privateCreator)
  {
    isPrivateCreator = privateCreator;
  }

  /**
   * Checks if this entry is a private tag.
   *
   * @return true if this is a private tag, false otherwise
   */
  public boolean isPrivateTag()
  {
    return isPrivateTag;
  }

  /**
   * Sets whether this entry is a private tag.
   *
   * @param privateTag true if this is a private tag
   */
  public void setPrivateTag(boolean privateTag)
  {
    isPrivateTag = privateTag;
  }

  /**
   * Gets the Value Representation (VR) of this entry.
   *
   * @return the VR, or null if not set
   */
  public VR getVr()
  {
    return vr;
  }

  /**
   * Sets the Value Representation (VR) of this entry.
   *
   * @param vr the VR to set
   */
  public void setVr(VR vr)
  {
    this.vr = vr;
  }

  /**
   * Gets the logarithmic length of this entry (for visualization).
   *
   * @return the logarithmic length
   */
  public float getLogLength()
  {
    return logLength;
  }

  /**
   * Sets the logarithmic length of this entry (for visualization).
   *
   * @param logLength the logarithmic length to set
   */
  public void setLogLength(float logLength)
  {
    this.logLength = logLength;
  }

  /**
   * Gets the multiplicity (number of values) of this entry.
   *
   * @return the multiplicity
   */
  public int getMultiplicity()
  {
    return multiplicity;
  }

  /**
   * Sets the multiplicity (number of values) of this entry.
   *
   * @param multiplicity the multiplicity to set
   */
  public void setMultiplicity(int multiplicity)
  {
    if (multiplicity < 0)
    {
      throw new IllegalArgumentException("Multiplicity must be non-negative, got: " + multiplicity);
    }
    this.multiplicity = multiplicity;
  }

  /**
   * Gets the logarithmic position of this entry (for visualization).
   *
   * @return the logarithmic position
   */
  public float getLogPosition()
  {
    return logPosition;
  }

  /**
   * Sets the logarithmic position of this entry (for visualization).
   *
   * @param logPosition the logarithmic position to set
   */
  public void setLogPosition(float logPosition)
  {
    this.logPosition = logPosition;
  }

  /**
   * Gets the DICOM tag (combined group and element).
   *
   * @return the tag value
   */
  public int getTag()
  {
    return tag;
  }

  /**
   * Sets the DICOM tag (combined group and element).
   *
   * @param tag the tag value to set
   */
  public void setTag(int tag)
  {
    this.tag = tag;
  }

}
