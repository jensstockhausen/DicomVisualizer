package de.famst.dicom.visualizer;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by jens on 16.05.17.
 */
public class Main
{
    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static MainApp app = null;

    public static void main(String args[])
    {
        Options options = new Options();
        options.addOption("i", true, "input file");
        options.addOption("o", true, "output file");

        CommandLineParser parser = new DefaultParser();

        try
        {
            CommandLine cmd = parser.parse( options, args);

            if ((cmd.hasOption("i") && cmd.hasOption("o")))
            {
                String in = cmd.getOptionValue("i");
                String out = cmd.getOptionValue("o");

                DicomParser dicomParser = new DicomParser(in);
                DicomDrawer dicomDrawer = new DicomDrawer(dicomParser);

                String svg = dicomDrawer.drawToSVG();

                try
                {
                    FileUtils.writeStringToFile(new File(out), svg, StandardCharsets.UTF_8);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                MainApp.run(in);

            }

        }
        catch (ParseException e)
        {
            LOG.error("Error ", e);
        }

    }


}
