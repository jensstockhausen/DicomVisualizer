package de.famst.dicom.visualizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class DicomFileDetector
{
    private static Logger LOG = LoggerFactory.getLogger(DicomFileDetector.class);

    public static boolean isDCMFile(Path filePath)
    {
        try (FileInputStream inStream = new FileInputStream(filePath.toFile()))
        {
            byte[] tag = new byte[]{'D', 'I', 'C', 'M'};
            byte[] buffer = new byte[4];

            if (128 != inStream.skip(128))
            {
                LOG.error("reading bytes");
                return false;
            }

            if (4 != inStream.read(buffer))
            {
                LOG.error("reading bytes");
                return false;
            }

            inStream.close();

            for (int i = 0; i < 4; i++)
            {
                if (buffer[i] != tag[i])
                {
                    return false;
                }
            }

        }
        catch (IOException e)
        {
            LOG.warn("error reading file [{}]", e);
            return false;
        }

        return true;
    }

}
