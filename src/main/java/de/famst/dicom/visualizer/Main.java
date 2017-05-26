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
        options.addOption("o", true, "output file");

        CommandLineParser parser = new DefaultParser();

        try
        {
            CommandLine cmd = parser.parse(options, args);

            if ((cmd.hasOption("i") && cmd.hasOption("o")))
            {
                String in = cmd.getOptionValue("i");
                String out = cmd.getOptionValue("o");

                DicomParser dicomParser = new DicomParser(in);

                int w = (int) dicomParser.length;
                int h = 70;

                SVGGraphics2D graph = new SVGGraphics2D(w, h);
                DicomDrawer dicomDrawer = new DicomDrawer(dicomParser, graph, w, h);

                LOG.info("Draw");

                graph = (SVGGraphics2D) dicomDrawer.draw();

                try
                {
                    LOG.info("Save");

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
