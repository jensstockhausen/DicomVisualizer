package de.famst.dicom.visualizer;

import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.log;

/**
 * Parsing a DICOM file and extracting meta-information and all entries
 */
public class DicomParser implements DicomInputHandler
{
  private static final Logger LOG = LoggerFactory.getLogger(DicomParser.class);

  private String fileName;
  private String stuid;
  private String seuid;
  private String siuid;
  private String modality;

  private List<DicomEntry> entries = new ArrayList<>();
  private float length;

  private int idx;

  private static final int DEFAULT_WIDTH = 130;
  private int width = DEFAULT_WIDTH;
  private DicomInputStream dis;
  private StringBuilder line;
  private DicomEntry entry;

  static public DicomParser parseFile(String fileName)
  {
    return new DicomParser(fileName);
  }

  private DicomParser(String fileName)
  {
    this.fileName = fileName;
    idx = 0;

    File file = new File(fileName);
    if (!file.exists())
    {
      throw new IllegalArgumentException("File does not exist: " + fileName);
    }
    if (!file.isFile())
    {
      throw new IllegalArgumentException("Path is not a file: " + fileName);
    }
    if (!file.canRead())
    {
      throw new IllegalArgumentException("File is not readable: " + fileName);
    }

    readContent(file);
  }

  private void readContent(File file)
  {
    LOG.info("parsing DICOM file {}", file.getAbsolutePath());

    try (DicomInputStream dis = new DicomInputStream(file))
    {
      dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
      dis.setDicomInputHandler(this);
      dis.readDataset(-1, -1);
    }
    catch (IOException e)
    {
      LOG.error("Error reading DICOM file [{}]", file.getAbsolutePath(), e);
      throw new RuntimeException("Failed to read DICOM file: " + file.getAbsolutePath(), e);
    }
  }

  @Override
  public void readValue(DicomInputStream dis, Attributes attrs) throws IOException
  {
    StringBuilder line = new StringBuilder(width + 30);
    DicomEntry entry = new DicomEntry(idx++);

    appendPrefix(dis, line, entry);
    appendHeader(dis, line, entry);

    VR vr = dis.vr();
    int valueLength = dis.length();
    boolean isValueLengthUndefined = valueLength == -1;

    // sequence is intended
    if (vr == VR.SQ || isValueLengthUndefined)
    {
      appendKeyword(dis, line, entry);
      LOG.debug("S:{}", line);
      getEntries().add(entry);

      dis.readValue(dis, attrs);

      if (isValueLengthUndefined)
      {
        line.setLength(0);
        entry = new DicomEntry(idx++);

        appendPrefix(dis, line, entry);
        appendHeader(dis, line, entry);
        appendKeyword(dis, line, entry);

        LOG.debug("I:{}", line);
        getEntries().add(entry);
      }

      return;
    }

    int tag = dis.tag();
    byte[] b = dis.readValue();

    line.append(" [");
    if (vr.prompt(b, dis.bigEndian(),
      attrs.getSpecificCharacterSet(),
      width - line.length() - 1, line))
    {
      line.append(']');
      appendKeyword(dis, line, entry);
    }

    LOG.debug("E:{}", line);
    getEntries().add(entry);

    if (tag == Tag.FileMetaInformationGroupLength)
    {
      dis.setFileMetaInformationGroupLength(b);
    }
    else if (tag == Tag.TransferSyntaxUID
      || tag == Tag.SpecificCharacterSet
      || TagUtils.isPrivateCreator(tag))
    {
      attrs.setBytes(tag, vr, b);
    }

    // extract UIDs and Modality from file
    else if (tag == Tag.StudyInstanceUID)
    {
      StringBuilder value = new StringBuilder();
      vr.prompt(b, dis.bigEndian(), attrs.getSpecificCharacterSet(), 120, value);
      stuid = value.toString();
    }
    else if (tag == Tag.SeriesInstanceUID)
    {
      StringBuilder value = new StringBuilder();
      vr.prompt(b, dis.bigEndian(), attrs.getSpecificCharacterSet(), 120, value);
      seuid = value.toString();
    }
    else if (tag == Tag.SOPInstanceUID)
    {
      StringBuilder value = new StringBuilder();
      vr.prompt(b, dis.bigEndian(), attrs.getSpecificCharacterSet(), 120, value);
      siuid = value.toString();
    }
    else if(tag == Tag.Modality)
    {
      StringBuilder value = new StringBuilder();
      vr.prompt(b, dis.bigEndian(), attrs.getSpecificCharacterSet(), 120, value);
      modality = value.toString();
    }

  }

  @Override
  public void readValue(DicomInputStream dis, Sequence seq) throws IOException
  {
    StringBuilder line = new StringBuilder(width);
    DicomEntry entry = new DicomEntry(idx++);

    appendPrefix(dis, line, entry);
    appendHeader(dis, line, entry);
    appendKeyword(dis, line, entry);
    appendNumber(seq.size() + 1, line, entry);

    LOG.debug("SQ:{}", line);
    getEntries().add(entry);

    boolean undeflen = dis.length() == -1;
    dis.readValue(dis, seq);

    if (undeflen)
    {
      line.setLength(0);
      entry = new DicomEntry(idx++);

      appendPrefix(dis, line, entry);
      appendHeader(dis, line, entry);
      appendKeyword(dis, line, entry);
      LOG.debug("SQI:{}", line);
      getEntries().add(entry);
    }
  }

  @Override
  public void readValue(DicomInputStream dis, Fragments frags) throws IOException
  {
    StringBuilder line = new StringBuilder(width + 20);
    DicomEntry entry = new DicomEntry(idx++);

    appendPrefix(dis, line, entry);
    appendHeader(dis, line, entry);
    appendFragment(line, dis, frags.vr(), entry);

    LOG.debug("F:{}", line);
    getEntries().add(entry);
  }

  @Override
  public void startDataset(DicomInputStream dis) throws IOException
  {

  }

  @Override
  public void endDataset(DicomInputStream dis) throws IOException
  {
    // min length is 1.0f
    getEntries().forEach(e ->
    {
      if (e.getLogLength() < 1.0)
      {
        e.setLogLength(1.0f);
      }
    });

    Optional<DicomEntry> maxLengthEntry = getEntries().stream().max(Comparator.comparing(e -> e.getLogLength()));
    Optional<DicomEntry> minLengthEntry = getEntries().stream().min(Comparator.comparing(e -> e.getLogLength()));

    float minV = 1.0f;
    float maxV = 100.0f;

    if (maxLengthEntry.isPresent() && minLengthEntry.isPresent())
    {
      minV = minLengthEntry.get().getLogLength();
      maxV = maxLengthEntry.get().getLogLength();
    }

    LOG.info("Min tag length [{}]", String.format("%6.3e", minV));
    LOG.info("Max tag length [{}]", String.format("%6.3e", maxV));

    float minT = 1.0f;
    float maxT = 70.0f;

    float finalMinV = minV;
    float finalMaxV = 1.2E7f; //maxV;

    LOG.debug("Min scaled length [{}]", String.format("%6.3e", minT));
    LOG.debug("Max scaled length [{}]", String.format("%6.3e", maxT));

    float[] pos = new float[1];
    pos[0] = 0.0f;

    getEntries().forEach(e ->
    {
      double v = e.getLogLength();
      v = (maxT - minT) * (log(v) - log(finalMinV)) / (log(finalMaxV) - minT) + minT;
      e.setLogLength((float) v);
      e.setLogPosition(pos[0]);

      pos[0] = pos[0] + (float) v;
    });

    length = pos[0];

    LOG.info("Total length [{}]", String.format("%6.3e", getLength()));

    getEntries().forEach(e ->
    {
      LOG.debug("[{}:{}:{}] \t level[{}] #[{}]", e.getLogPosition(), TagUtils.toString(e.getTag()), e.getVr(), e.getLevel(), e.getLogLength());
    });


  }

  // appenders

  private void appendPrefix(DicomInputStream dis, StringBuilder line, DicomEntry entry)
  {
    line.append(dis.getTagPosition()).append(": ");

    int level = dis.level();
    while (level-- > 0)
      line.append('>');

    entry.setLogPosition(dis.getTagPosition());
    entry.setLevel(dis.level());
  }

  private void appendHeader(DicomInputStream dis, StringBuilder line, DicomEntry entry)
  {
    line.append(TagUtils.toString(dis.tag())).append(' ');

    VR vr = dis.vr();
    if (vr != null)
      line.append(vr).append(' ');

    line.append('#').append(dis.length());

    entry.setTag(dis.tag());
    entry.setGroup(TagUtils.groupNumber(dis.tag()));
    entry.setElement(TagUtils.elementNumber(dis.tag()));
    entry.setPrivateCreator(TagUtils.isPrivateCreator(dis.tag()));
    entry.setPrivateTag(TagUtils.isPrivateTag(dis.tag()));

    entry.setVr(vr);
    entry.setLogLength(dis.length());
  }

  private void appendKeyword(DicomInputStream dis, StringBuilder line, DicomEntry entry)
  {
    this.dis = dis;
    this.line = line;
    this.entry = entry;
    if (line.length() < width)
    {
      line.append(" ");
      line.append(ElementDictionary.keywordOf(dis.tag(), null));
      if (line.length() > width)
        line.setLength(width);
    }
  }

  private void appendNumber(int number, StringBuilder line, DicomEntry entry)
  {
    if (line.length() < width)
    {
      line.append(" #");
      line.append(number);
      if (line.length() > width)
        line.setLength(width);
    }
  }

  private void appendFragment(StringBuilder line, DicomInputStream dis,
                              VR vr, DicomEntry entry) throws IOException
  {
    byte[] b = dis.readValue();
    line.append(" [");
    if (vr.prompt(b, dis.bigEndian(), null,
      width - line.length() - 1, line))
    {
      line.append(']');
      appendKeyword(dis, line, entry);
    }
  }

  public String getFileName()
  {
    return fileName;
  }

  public String getStuid()
  {
    return stuid;
  }

  public String getSeuid()
  {
    return seuid;
  }

  public String getSiuid()
  {
    return siuid;
  }

  public String getModality()
  {
    return modality;
  }

  public List<DicomEntry> getEntries()
  {
    return entries;
  }

  public float getLength()
  {
    return length;
  }
}
