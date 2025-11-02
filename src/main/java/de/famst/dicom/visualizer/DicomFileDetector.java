package de.famst.dicom.visualizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Utility class to detect DICOM files by checking for the DICM magic number.
 */
public class DicomFileDetector
{
  private static final Logger LOG = LoggerFactory.getLogger(DicomFileDetector.class);

  private static final byte[] DICOM_PREFIX = new byte[]{'D', 'I', 'C', 'M'};
  private static final int DICOM_PREFIX_OFFSET = 128;

  /**
   * Checks if a file is a valid DICOM file by reading the DICM prefix at byte offset 128.
   *
   * @param filePath the path to the file to check
   * @return true if the file contains the DICM prefix, false otherwise
   */
  public static boolean isDCMFile(Path filePath)
  {
    if (filePath == null)
    {
      LOG.warn("File path is null");
      return false;
    }

    if (!filePath.toFile().exists())
    {
      LOG.warn("File does not exist: {}", filePath);
      return false;
    }

    if (!filePath.toFile().isFile())
    {
      LOG.warn("Path is not a file: {}", filePath);
      return false;
    }

    try (FileInputStream inStream = new FileInputStream(filePath.toFile()))
    {
      byte[] buffer = new byte[DICOM_PREFIX.length];

      long skipped = inStream.skip(DICOM_PREFIX_OFFSET);
      if (skipped != DICOM_PREFIX_OFFSET)
      {
        LOG.debug("File too short to be DICOM (only {} bytes): {}", skipped, filePath);
        return false;
      }

      int bytesRead = inStream.read(buffer);
      if (bytesRead != DICOM_PREFIX.length)
      {
        LOG.debug("Could not read DICM prefix from file: {}", filePath);
        return false;
      }

      return Arrays.equals(buffer, DICOM_PREFIX);
    }
    catch (IOException e)
    {
      LOG.warn("Error reading file [{}]: {}", filePath, e.getMessage());
      return false;
    }
  }

}
