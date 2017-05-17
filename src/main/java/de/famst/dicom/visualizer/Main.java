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
        options.addOption("i", true, "display current time");

        CommandLineParser parser = new DefaultParser();

        try
        {
            CommandLine cmd = parser.parse( options, args);

            if (cmd.hasOption("i"))
            {
                MainApp.run(cmd.getOptionValue("i"));
            }

        }
        catch (ParseException e)
        {
            LOG.error("Error ", e);
        }

    }


}
