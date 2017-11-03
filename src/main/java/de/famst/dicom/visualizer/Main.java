package de.famst.dicom.visualizer;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by jens on 16.05.17.
 */
public class Main
{
  private static Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String args[])
  {
    LOG.info("Start");

    Options options = new Options();
    options.addOption("i", true, "input file");
    options.addOption("p", true, "input path");
    options.addOption("o", true, "output file");

    CommandLineParser parser = new DefaultParser();

    try
    {
      CommandLine cmd = parser.parse(options, args);

      // one single dicom file
      if ((cmd.hasOption("i") && cmd.hasOption("o")))
      {
        String in = cmd.getOptionValue("i");
        String out = cmd.getOptionValue("o");

        DicomParser dicomParser = DicomParser.parseFile(in);

        int w = (int) dicomParser.length;
        int h = 70;

        SVGGraphics2D graph = new SVGGraphics2D(w, h);
        DicomDrawer dicomDrawer = new DicomDrawer(dicomParser, graph, w, h);

        LOG.info("Draw");

        graph = (SVGGraphics2D) dicomDrawer.draw();

        try
        {
          LOG.info("Save to [{}]", out);

          String svgDocument = graph.getSVGDocument();
          FileUtils.writeStringToFile(new File(out), svgDocument, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }

      }

      // folder of dicom files
      if ((cmd.hasOption("p") && cmd.hasOption("o")))
      {
        String in = cmd.getOptionValue("p");
        String out = cmd.getOptionValue("o");

        StudyDrawer studyDrawer = new StudyDrawer(in);

        int w = (int) studyDrawer.maxLength;
        int h = 70 * studyDrawer.files.size() + 20 * studyDrawer.series.size();

        SVGGraphics2D graph = new SVGGraphics2D(w, h);

        LOG.info("Draw");

        graph = (SVGGraphics2D) studyDrawer.draw(graph);

        try
        {
          LOG.info("Save to [{}]", out);

          String svgDocument = graph.getSVGDocument();
          FileUtils.writeStringToFile(new File(out), svgDocument, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }

      }

    }
    catch (ParseException e)
    {
      LOG.error("Error ", e);
    }

    LOG.info("End");
  }


}
