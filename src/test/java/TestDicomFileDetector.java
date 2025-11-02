import de.famst.dicom.visualizer.DicomFileDetector;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for DicomFileDetector
 */
public class TestDicomFileDetector
{
  @Test
  public void testNullFilePath()
  {
    boolean result = DicomFileDetector.isDCMFile(null);
    assertThat(result, is(false));
  }

  @Test
  public void testNonExistentFile()
  {
    Path nonExistent = Path.of("/path/to/nonexistent/file.dcm");
    boolean result = DicomFileDetector.isDCMFile(nonExistent);
    assertThat(result, is(false));
  }

  @Test
  public void testDirectoryPath(@TempDir Path tempDir)
  {
    boolean result = DicomFileDetector.isDCMFile(tempDir);
    assertThat(result, is(false));
  }

  @Test
  public void testEmptyFile(@TempDir Path tempDir) throws IOException
  {
    Path emptyFile = tempDir.resolve("empty.dcm");
    Files.writeString(emptyFile, "");

    boolean result = DicomFileDetector.isDCMFile(emptyFile);
    assertThat(result, is(false));
  }

  @Test
  public void testFileTooShort(@TempDir Path tempDir) throws IOException
  {
    Path shortFile = tempDir.resolve("short.dcm");
    byte[] shortContent = new byte[100]; // Less than 132 bytes needed
    Files.write(shortFile, shortContent);

    boolean result = DicomFileDetector.isDCMFile(shortFile);
    assertThat(result, is(false));
  }

  @Test
  public void testFileWithoutDICMPrefix(@TempDir Path tempDir) throws IOException
  {
    Path textFile = tempDir.resolve("text.txt");
    byte[] content = new byte[200];
    // Fill with non-DICM content
    Arrays.fill(content, (byte) 'X');
    Files.write(textFile, content);

    boolean result = DicomFileDetector.isDCMFile(textFile);
    assertThat(result, is(false));
  }

  @Test
  public void testFileWithDICMPrefixAtWrongOffset(@TempDir Path tempDir) throws IOException
  {
    Path wrongOffsetFile = tempDir.resolve("wrong_offset.dcm");
    byte[] content = new byte[200];
    // Place DICM at offset 0 instead of 128
    content[0] = 'D';
    content[1] = 'I';
    content[2] = 'C';
    content[3] = 'M';
    Files.write(wrongOffsetFile, content);

    boolean result = DicomFileDetector.isDCMFile(wrongOffsetFile);
    assertThat(result, is(false));
  }

  @Test
  public void testValidDicomFile(@TempDir Path tempDir) throws Exception
  {
    // Create a valid DICOM file
    Attributes dcmAttrs = new Attributes();
    dcmAttrs.setString(Tag.PatientName, VR.PN, "Test^Patient");
    dcmAttrs.setString(Tag.PatientID, VR.LO, "12345");
    dcmAttrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.840.113619.2.1.1.1");
    dcmAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.840.113619.2.1.1.2");
    dcmAttrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage);
    dcmAttrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.840.113619.2.1.1.3");
    dcmAttrs.setString(Tag.Modality, VR.CS, "CT");

    Path validDicomPath = tempDir.resolve("valid.dcm");
    File dicomFile = validDicomPath.toFile();

    try (DicomOutputStream dos = new DicomOutputStream(dicomFile))
    {
      Attributes fmi = dcmAttrs.createFileMetaInformation(UID.ImplicitVRLittleEndian);
      dos.writeDataset(fmi, dcmAttrs);
    }

    boolean result = DicomFileDetector.isDCMFile(validDicomPath);
    assertTrue(result, "Valid DICOM file should be detected");
  }

  @Test
  public void testFileWithCorrectDICMPrefix(@TempDir Path tempDir) throws IOException
  {
    // Manually create a file with DICM prefix at correct offset
    Path dicomFile = tempDir.resolve("manual.dcm");
    byte[] content = new byte[132];

    // Write DICM at offset 128
    content[128] = 'D';
    content[129] = 'I';
    content[130] = 'C';
    content[131] = 'M';

    Files.write(dicomFile, content);

    boolean result = DicomFileDetector.isDCMFile(dicomFile);
    assertTrue(result, "File with DICM prefix at offset 128 should be detected");
  }

  @Test
  public void testFileWithPartialDICMPrefix(@TempDir Path tempDir) throws IOException
  {
    Path partialFile = tempDir.resolve("partial.dcm");
    byte[] content = new byte[132];

    // Write only partial DICM prefix
    content[128] = 'D';
    content[129] = 'I';
    content[130] = 'C';
    content[131] = 'X'; // Wrong last character

    Files.write(partialFile, content);

    boolean result = DicomFileDetector.isDCMFile(partialFile);
    assertFalse(result, "File with incorrect DICM prefix should not be detected");
  }

  @Test
  public void testFileWithSpecialCharactersInName(@TempDir Path tempDir) throws IOException
  {
    // Test file names with special characters
    Path specialFile = tempDir.resolve("test file with spaces & chars!.dcm");
    byte[] content = new byte[132];

    // Write DICM at offset 128
    content[128] = 'D';
    content[129] = 'I';
    content[130] = 'C';
    content[131] = 'M';

    Files.write(specialFile, content);

    boolean result = DicomFileDetector.isDCMFile(specialFile);
    assertTrue(result, "File with special characters in name should be detected if valid");
  }
}

