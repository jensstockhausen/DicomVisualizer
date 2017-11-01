package de.famst.dicom.visualizer;

import org.dcm4che3.data.VR;

/**
 * Created by jens on 13.05.17.
 */
public class DicomEntry
{
  private int idx;
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

  public DicomEntry(int idx)
  {
    this.idx = idx;
    group = -1;
    element = -1;
    level = -1;
    isPrivateCreator = false;
    isPrivateTag = false;
    vr = null;
    logLength = 0;
    multiplicity = 0;
  }

  public int getIdx()
  {
    return idx;
  }

  public int getGroup()
  {
    return group;
  }

  public void setGroup(int group)
  {
    this.group = group;
  }

  public int getElement()
  {
    return element;
  }

  public void setElement(int element)
  {
    this.element = element;
  }

  public float getLevel()
  {
    return level;
  }

  public void setLevel(float level)
  {
    this.level = level;
  }

  public boolean isPrivateCreator()
  {
    return isPrivateCreator;
  }

  public void setPrivateCreator(boolean privateCreator)
  {
    isPrivateCreator = privateCreator;
  }

  public boolean isPrivateTag()
  {
    return isPrivateTag;
  }

  public void setPrivateTag(boolean privateTag)
  {
    isPrivateTag = privateTag;
  }

  public VR getVr()
  {
    return vr;
  }

  public void setVr(VR vr)
  {
    this.vr = vr;
  }

  public float getLogLength()
  {
    return logLength;
  }

  public void setLogLength(float logLength)
  {
    this.logLength = logLength;
  }

  public int getMultiplicity()
  {
    return multiplicity;
  }

  public void setMultiplicity(int multiplicity)
  {
    this.multiplicity = multiplicity;
  }

  public void setLogPosition(float logPosition)
  {
    this.logPosition = logPosition;
  }

  public float getLogPosition()
  {
    return logPosition;
  }

  public void setTag(int tag)
  {
    this.tag = tag;
  }

  public int getTag()
  {
    return tag;
  }
}
