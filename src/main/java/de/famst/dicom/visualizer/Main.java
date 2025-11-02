package de.famst.dicom.visualizer;

import org.apache.commons.cli.*;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class for DICOM visualization tool.
 * Processes DICOM files and generates SVG visualizations.
 */
public class Main
{
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private static final int BASE_HEIGHT = 70;
  private static final int SERIES_SPACING = 20;

  public static void main(String[] args)
  {
    LOG.info("Start");

    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();

    try
    {
      CommandLine cmd = parser.parse(options, args);
      processCommand(cmd);
    }
    catch (ParseException e)
    {
      LOG.error("Error parsing command line arguments", e);
      printUsage(options);
    }

    LOG.info("End");
  }

  /**
   * Creates command line options.
   */
  private static Options createOptions()
  {
    Options options = new Options();
    options.addOption("i", "input", true, "input file");
    options.addOption("p", "path", true, "input path");
    options.addOption("o", "output", true, "output file");
    return options;
  }

  /**
   * Prints usage information.
   */
  private static void printUsage(Options options)
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("dicom-visualizer", options);
  }

  /**
   * Processes the command line arguments and delegates to appropriate handler.
   */
  private static void processCommand(CommandLine cmd)
  {
    if (cmd.hasOption("i") && cmd.hasOption("o"))
    {
      processSingleFile(cmd.getOptionValue("i"), cmd.getOptionValue("o"));
    }
    else if (cmd.hasOption("p") && cmd.hasOption("o"))
    {
      processFolder(cmd.getOptionValue("p"), cmd.getOptionValue("o"));
    }
    else
    {
      LOG.error("Invalid combination of options. Use -i/-o for single file or -p/-o for folder.");
    }
  }

  /**
   * Processes a single DICOM file and generates SVG output.
   */
  private static void processSingleFile(String inputPath, String outputPath)
  {
    try
    {
      DicomParser dicomParser = DicomParser.parseFile(inputPath);
      int width = (int) dicomParser.length;
      int height = BASE_HEIGHT;

      SVGGraphics2D graph = new SVGGraphics2D(width, height);
      DicomDrawer dicomDrawer = new DicomDrawer(dicomParser, graph, width, height);

      LOG.info("Drawing single file");
      graph = (SVGGraphics2D) dicomDrawer.draw();

      saveSvg(graph, outputPath);
    }
    catch (Exception e)
    {
      LOG.error("Error processing single file: {}", inputPath, e);
    }
  }

  /**
   * Processes a folder of DICOM files and generates SVG output.
   */
  private static void processFolder(String inputPath, String outputPath)
  {
    try
    {
      StudyDrawer studyDrawer = new StudyDrawer(inputPath);
      int width = (int) studyDrawer.maxLength;
      int height = BASE_HEIGHT * studyDrawer.files.size() + SERIES_SPACING * studyDrawer.series.size();

      SVGGraphics2D graph = new SVGGraphics2D(width, height);

      LOG.info("Drawing folder");
      graph = (SVGGraphics2D) studyDrawer.draw(graph);

      saveSvg(graph, outputPath);
    }
    catch (Exception e)
    {
      LOG.error("Error processing folder: {}", inputPath, e);
    }
  }

  /**
   * Saves the SVG graphics to a file.
   */
  private static void saveSvg(SVGGraphics2D graph, String outputPath)
  {
    try
    {
      LOG.info("Saving to [{}]", outputPath);

      String svgDocument = graph.getSVGDocument();
      Path path = Paths.get(outputPath);
      Files.writeString(path, svgDocument);

      LOG.info("Successfully saved SVG to [{}]", outputPath);
    }
    catch (IOException e)
    {
      LOG.error("Error saving SVG to [{}]", outputPath, e);
    }
  }



}
