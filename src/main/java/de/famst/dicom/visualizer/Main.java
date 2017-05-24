package de.famst.dicom.visualizer;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        options.addOption("i", true, "input DICOM file");
        options.addOption("o", true, "output folder");

        CommandLineParser parser = new DefaultParser();

        try
        {
            CommandLine cmd = parser.parse( options, args);

            if ((cmd.hasOption("i")) && (cmd.hasOption("o")))
            {
                MainApp.run(cmd.getOptionValue("i"), cmd.getOptionValue("o"));
            }

        }
        catch (ParseException e)
        {
            LOG.error("Error ", e);
        }

    }


}
