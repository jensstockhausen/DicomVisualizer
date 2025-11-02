package de.famst.dicom.visualizer;


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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Drawer for DICOM studies that visualizes all series and instances within a study directory.
 * Scans a directory for DICOM files, organizes them by series, and creates an SVG visualization.
 */
public class StudyDrawer
{
  private static final Logger LOG = LoggerFactory.getLogger(StudyDrawer.class);

  private static final int SERIES_HEADER_HEIGHT = 20;
  private static final int INSTANCE_HEIGHT = 70;
  private static final float HEADER_VERTICAL_OFFSET = 20.0f;
  private static final float HEADER_PADDING = 3.0f;
  private static final float LINE_VERTICAL_OFFSET = 2.0f;
  private static final float STROKE_WIDTH = 0.5f;

  private final List<Path> files = new ArrayList<>();
  private final Map<String, List<DicomParser>> series = new HashMap<>();
  private float maxLength;

  /**
   * Creates a new StudyDrawer and loads all DICOM files from the specified directory.
   *
   * @param inputPath the path to the directory containing DICOM files
   * @throws IllegalArgumentException if the input path is null or empty
   */
  public StudyDrawer(String inputPath)
  {
    if (inputPath == null || inputPath.trim().isEmpty())
    {
      throw new IllegalArgumentException("Input path cannot be null or empty");
    }

    LOG.info("Loading study from [{}]", inputPath);

    loadDicomFiles(inputPath);
    organizeBySeries();
  }

  /**
   * Loads all DICOM files from the specified directory and subdirectories.
   */
  private void loadDicomFiles(String inputPath)
  {
    try
    {
      Files.walk(Paths.get(inputPath))
        .filter(Files::isRegularFile)
        .filter(DicomFileDetector::isDCMFile)
        .forEach(path -> {
          LOG.info("Adding file [{}]", path);
          files.add(path);
        });

      if (files.isEmpty())
      {
        LOG.warn("No DICOM files found in [{}]", inputPath);
      }
    }
    catch (IOException e)
    {
      LOG.error("Error walking directory [{}]: {}", inputPath, e.getMessage(), e);
      throw new RuntimeException("Failed to load DICOM files from: " + inputPath, e);
    }
  }

  /**
   * Organizes the loaded DICOM files by SeriesInstanceUID and calculates the maximum length.
   */
  private void organizeBySeries()
  {
    AtomicReference<Float> maxLengthRef = new AtomicReference<>(0.0f);

    files.forEach(path -> {
      try
      {
        DicomParser dicomParser = DicomParser.parseFile(path.toAbsolutePath().toString());
        String seriesUID = dicomParser.getSeuid();

        if (dicomParser.getLength() > maxLengthRef.get())
        {
          maxLengthRef.set(dicomParser.getLength());
        }

        series.computeIfAbsent(seriesUID, key -> {
          LOG.info("Found Series [{}]", key);
          return new ArrayList<>();
        }).add(dicomParser);
      }
      catch (Exception e)
      {
        LOG.error("Failed to parse DICOM file [{}]: {}", path, e.getMessage(), e);
      }
    });

    this.maxLength = maxLengthRef.get();
    LOG.info("Found {} series with max length {}", series.size(), maxLength);
  }

  /**
   * Gets the list of DICOM files in this study.
   *
   * @return an unmodifiable list of file paths
   */
  public List<Path> getFiles()
  {
    return Collections.unmodifiableList(files);
  }

  /**
   * Gets the map of series UIDs to their DICOM parsers.
   *
   * @return an unmodifiable map of series
   */
  public Map<String, List<DicomParser>> getSeries()
  {
    return Collections.unmodifiableMap(series);
  }

  /**
   * Gets the maximum length across all DICOM instances in the study.
   *
   * @return the maximum length
   */
  public float getMaxLength()
  {
    return maxLength;
  }

  /**
   * Draws the complete study visualization onto the provided SVG graphics context.
   * Each series is drawn with a header, followed by all instances in that series.
   *
   * @param graph the SVG graphics context to draw on
   * @return the updated graphics context
   */
  public Graphics2D draw(SVGGraphics2D graph)
  {
    if (graph == null)
    {
      throw new IllegalArgumentException("Graphics context cannot be null");
    }

    int offset = 0;

    // Draw background
    graph.setPaint(Color.BLACK);
    graph.setStroke(new BasicStroke(STROKE_WIDTH));
    graph.fill(new Rectangle2D.Float(0, 0, graph.getWidth(), graph.getHeight()));

    // Sort series by UID for consistent ordering
    List<String> sortedSeriesUIDs = new ArrayList<>(series.keySet());
    Collections.sort(sortedSeriesUIDs);

    for (String seriesUID : sortedSeriesUIDs)
    {
      LOG.info("Drawing series [{}]", seriesUID);

      // Draw series header
      graph.setColor(ColorMapper.HSBtoRGB(0.0f, 0.0f, 100.0f));
      graph.drawString(seriesUID, 0.0f, offset + HEADER_VERTICAL_OFFSET - HEADER_PADDING);
      graph.draw(new Line2D.Float(0.0f, offset + LINE_VERTICAL_OFFSET, graph.getWidth(), offset + LINE_VERTICAL_OFFSET));

      offset += SERIES_HEADER_HEIGHT;

      // Draw all instances in this series
      List<DicomParser> parsers = series.get(seriesUID);
      for (DicomParser parser : parsers)
      {
        LOG.info("Drawing instance [{}]", parser.getSiuid());

        int width = (int) parser.getLength();
        int height = INSTANCE_HEIGHT;

        DicomDrawer dicomDrawer = new DicomDrawer(parser, graph, width, height, offset);
        graph = (SVGGraphics2D) dicomDrawer.draw();

        offset += INSTANCE_HEIGHT;
      }
    }

    return graph;
  }
}
