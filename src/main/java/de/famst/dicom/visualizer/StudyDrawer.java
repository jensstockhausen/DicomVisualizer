package de.famst.dicom.visualizer;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Created by jens on 26.05.17.
 */
public class StudyDrawer
{
  private static Logger LOG = LoggerFactory.getLogger(StudyDrawer.class);

  public List<Path> files = new ArrayList<>();

  public Map<String, List<DicomParser>> series = new HashMap<>();

  public float maxLength;

  public StudyDrawer(String in)
  {
    LOG.info("Loading study from [{}]", in);

    // get a list of DICOM files in given folder (and sub folders)
    // assuming they are of one study
    try
    {
      Files.walk(Paths.get(in))
        .filter(Files::isRegularFile)
        .filter(DicomFileDetector::isDCMFile)
        .forEach(p -> {
          LOG.info("Adding file [{}]", p);
          files.add(p);
        });

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    final float maxLength[] = new float[1];

    maxLength[0] = 0.0f;

    // order by SeriesInstanceUID
    files.stream().forEach(p -> {

      DicomParser dicomParser = DicomParser.parseFile(p.toAbsolutePath().toString());

      String seriesUID = dicomParser.getSeuid();

      if (dicomParser.getLength() > maxLength[0])
      {
        maxLength[0] = dicomParser.getLength();
      }

      if (series.get(seriesUID) == null)
      {
        LOG.info("Found Series [{}]", seriesUID);
        series.put(seriesUID, new ArrayList<>());
      }

      series.get(seriesUID).add(dicomParser);
    });

    this.maxLength = maxLength[0];
  }

  private Attributes loadDicom(Path path)
  {
    Attributes dcm;

    LOG.info("opening DICOM file {}", path.toAbsolutePath());

    try (DicomInputStream dis = new DicomInputStream(path.toFile());)
    {
      dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
      dcm = dis.readDataset(-1, -1);
      dis.close();
    }
    catch (IOException e)
    {
      LOG.error("reading DICOM file [{}]", e);
      return null;
    }

    return dcm;
  }


  public Graphics2D draw(SVGGraphics2D graph)
  {
    int offset = 0;

    // background
    graph.setPaint(Color.black);
    graph.setStroke(new BasicStroke(0.5f));
    graph.fill(new Rectangle2D.Float(0, 0, graph.getWidth(), graph.getHeight()));

    Object[] keys = series.keySet().stream().sorted().toArray();

    for (Object seuidObj : keys)
    {
      String seuid = (String)seuidObj;

      LOG.info("Draw series [{}]", seuid);

      graph.setColor(ColorMapper.HSBtoRGB(0.0f, 0.0f, 100.0f));
      graph.drawString(seuid, 0.0f, offset + 20.0f - 3.0f);
      graph.draw(new Line2D.Float(0.0f, offset + 2.0f, graph.getWidth(), offset + 2.0f));

      offset += 20;

      for (DicomParser parser : series.get(seuid))
      {
        LOG.info("Draw instance [{}]", parser.getSiuid());

        int w = (int) parser.getLength();
        int h = 70;

        DicomDrawer dicomDrawer = new DicomDrawer(parser, graph, w, h, offset);
        graph = (SVGGraphics2D) dicomDrawer.draw();

        offset += 70;
      }
    }

    return graph;
  }
}
