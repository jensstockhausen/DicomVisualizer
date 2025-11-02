import de.famst.dicom.visualizer.DicomParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DicomParser
 */
public class TestDicomParser
{
    @Test
    public void testFileNotFound()
    {
        String nonExistentFile = "/path/to/nonexistent/file.dcm";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DicomParser.parseFile(nonExistentFile),
            "Expected parseFile() to throw IllegalArgumentException for non-existent file"
        );

        assertThat(exception.getMessage(), containsString("File does not exist"));
        assertThat(exception.getMessage(), containsString(nonExistentFile));
    }

    @Test
    public void testPathIsNotFile(@TempDir Path tempDir)
    {
        String directoryPath = tempDir.toString();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DicomParser.parseFile(directoryPath),
            "Expected parseFile() to throw IllegalArgumentException for directory path"
        );

        assertThat(exception.getMessage(), containsString("Path is not a file"));
        assertThat(exception.getMessage(), containsString(directoryPath));
    }

    @Test
    public void testFileNotReadable(@TempDir Path tempDir) throws IOException
    {
        // Create a file and make it unreadable
        Path unreadableFile = tempDir.resolve("unreadable.dcm");
        Files.writeString(unreadableFile, "test content");
        File file = unreadableFile.toFile();

        // Try to make the file unreadable (this may not work on all systems)
        boolean madeUnreadable = file.setReadable(false);

        if (madeUnreadable && !file.canRead())
        {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DicomParser.parseFile(unreadableFile.toString()),
                "Expected parseFile() to throw IllegalArgumentException for unreadable file"
            );

            assertThat(exception.getMessage(), containsString("File is not readable"));
            assertThat(exception.getMessage(), containsString(unreadableFile.toString()));

            // Clean up - restore read permission
            file.setReadable(true);
        }
        else
        {
            // Skip test if we can't make the file unreadable (e.g., on Windows or as root)
            System.out.println("Skipping unreadable file test - unable to remove read permissions");
        }
    }

    @Test
    public void testInvalidDicomFile(@TempDir Path tempDir) throws IOException
    {
        // Create a file with invalid DICOM content
        Path invalidFile = tempDir.resolve("invalid.dcm");
        Files.writeString(invalidFile, "This is not a valid DICOM file");

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> DicomParser.parseFile(invalidFile.toString()),
            "Expected parseFile() to throw RuntimeException for invalid DICOM file"
        );

        assertThat(exception.getMessage(), containsString("Failed to read DICOM file"));
        assertThat(exception.getMessage(), containsString(invalidFile.toString()));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testEmptyFile(@TempDir Path tempDir) throws IOException
    {
        // Create an empty file
        Path emptyFile = tempDir.resolve("empty.dcm");
        Files.writeString(emptyFile, "");

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> DicomParser.parseFile(emptyFile.toString()),
            "Expected parseFile() to throw RuntimeException for empty file"
        );

        assertThat(exception.getMessage(), containsString("Failed to read DICOM file"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testParserInitialization()
    {
        // Test that null or empty filename is handled
        assertThrows(
            Exception.class,
            () -> DicomParser.parseFile(null),
            "Expected parseFile() to throw exception for null filename"
        );
    }

    @Test
    public void testEmptyStringFilename()
    {
        // Test that empty string filename is handled
        assertThrows(
            Exception.class,
            () -> DicomParser.parseFile(""),
            "Expected parseFile() to throw exception for empty filename"
        );
    }

    @Test
    public void testFileNameWithSpecialCharacters(@TempDir Path tempDir)
    {
        String specialFile = tempDir.resolve("file with spaces & special!chars.dcm").toString();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DicomParser.parseFile(specialFile),
            "Expected parseFile() to throw IllegalArgumentException for non-existent file"
        );

        assertThat(exception.getMessage(), containsString("File does not exist"));
    }


    @Test
    public void testValidDicomFile(@TempDir Path tempDir) throws Exception
    {
        DicomParser parser = DicomTestUtils.createComprehensiveDicomFile(tempDir, "valid.dcm");

        assertNotNull(parser);
        assertNotNull(parser.getFileName());
        assertThat(parser.getFileName(), containsString("valid.dcm"));
        assertNotNull(parser.getEntries());
        assertThat(parser.getEntries().size(), greaterThan(0));
    }
}

