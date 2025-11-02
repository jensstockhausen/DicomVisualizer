import de.famst.dicom.visualizer.DicomParser;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomOutputStream;
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

        // Try to make file unreadable (this may not work on all systems)
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
            // Skip test if we can't make file unreadable (e.g., on Windows or as root)
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
        // create a valid DICOM file with all required tags
        Attributes dcmAttrs = new Attributes();

        // Patient Level - Required Tags
        dcmAttrs.setString(Tag.PatientName, VR.PN, "Test^Patient");
        dcmAttrs.setString(Tag.PatientID, VR.LO, "12345");
        dcmAttrs.setString(Tag.PatientBirthDate, VR.DA, "19800101");
        dcmAttrs.setString(Tag.PatientSex, VR.CS, "M");

        // Study Level - Required Tags
        dcmAttrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.840.113619.2.1.1.1");
        dcmAttrs.setString(Tag.StudyDate, VR.DA, "20240101");
        dcmAttrs.setString(Tag.StudyTime, VR.TM, "120000");
        dcmAttrs.setString(Tag.StudyID, VR.SH, "STUDY001");
        dcmAttrs.setString(Tag.AccessionNumber, VR.SH, "ACC001");
        dcmAttrs.setString(Tag.ReferringPhysicianName, VR.PN, "");

        // Series Level - Required Tags
        dcmAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.840.113619.2.1.1.2");
        dcmAttrs.setString(Tag.SeriesNumber, VR.IS, "1");
        dcmAttrs.setString(Tag.Modality, VR.CS, "CT");
        dcmAttrs.setString(Tag.SeriesDate, VR.DA, "20240101");
        dcmAttrs.setString(Tag.SeriesTime, VR.TM, "120000");

        // Image Level - Required Tags
        dcmAttrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage);
        dcmAttrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.840.113619.2.1.1.3");
        dcmAttrs.setString(Tag.InstanceNumber, VR.IS, "1");
        dcmAttrs.setString(Tag.ImageType, VR.CS, "ORIGINAL\\PRIMARY");

        // Additional recommended tags
        dcmAttrs.setString(Tag.Manufacturer, VR.LO, "Test Manufacturer");
        dcmAttrs.setString(Tag.InstitutionName, VR.LO, "Test Institution");

        // Private Tags - Private Creator Block
        // Register private creator in group 0x0009
        dcmAttrs.setString(0x00090010, VR.LO, "TEST_PRIVATE_CREATOR");
        // Private tags in block (0009,1000-10FF)
        dcmAttrs.setString(0x00091001, VR.LO, "Private Test Value 1");
        dcmAttrs.setString(0x00091002, VR.SH, "PRIVATE_ID_123");
        dcmAttrs.setInt(0x00091003, VR.IS, 42);

        // Another private creator in group 0x0011
        dcmAttrs.setString(0x00110010, VR.LO, "CUSTOM_VENDOR_DATA");
        // Private tags in block (0011,1000-10FF)
        dcmAttrs.setString(0x00111001, VR.LO, "Custom Vendor Information");
        dcmAttrs.setString(0x00111002, VR.DA, "20240115");
        dcmAttrs.setDouble(0x00111003, VR.DS, 3.14159);

        // Structured Report - Content Sequence (findings)

        // Finding 1: Measurement
        Attributes finding1 = new Attributes();
        finding1.setString(Tag.ValueType, VR.CS, "NUM");
        finding1.setString(Tag.RelationshipType, VR.CS, "CONTAINS");

        // Concept Name Code Sequence for Finding 1
        Attributes conceptName1 = new Attributes();
        conceptName1.setString(Tag.CodeValue, VR.SH, "121206");
        conceptName1.setString(Tag.CodingSchemeDesignator, VR.SH, "DCM");
        conceptName1.setString(Tag.CodeMeaning, VR.LO, "Distance");
        finding1.newSequence(Tag.ConceptNameCodeSequence, 1).add(conceptName1);

        // Measured Value Sequence
        Attributes measuredValue1 = new Attributes();
        measuredValue1.setString(Tag.NumericValue, VR.DS, "45.3");
        Attributes measurementUnits1 = new Attributes();
        measurementUnits1.setString(Tag.CodeValue, VR.SH, "mm");
        measurementUnits1.setString(Tag.CodingSchemeDesignator, VR.SH, "UCUM");
        measurementUnits1.setString(Tag.CodeMeaning, VR.LO, "millimeter");
        measuredValue1.newSequence(Tag.MeasurementUnitsCodeSequence, 1).add(measurementUnits1);
        finding1.newSequence(Tag.MeasuredValueSequence, 1).add(measuredValue1);

        // Finding 2: Text observation
        Attributes finding2 = new Attributes();
        finding2.setString(Tag.ValueType, VR.CS, "TEXT");
        finding2.setString(Tag.RelationshipType, VR.CS, "CONTAINS");
        finding2.setString(Tag.TextValue, VR.UT, "No significant abnormalities detected");

        Attributes conceptName2 = new Attributes();
        conceptName2.setString(Tag.CodeValue, VR.SH, "121071");
        conceptName2.setString(Tag.CodingSchemeDesignator, VR.SH, "DCM");
        conceptName2.setString(Tag.CodeMeaning, VR.LO, "Finding");
        finding2.newSequence(Tag.ConceptNameCodeSequence, 1).add(conceptName2);

        // Finding 3: Code value (diagnosis)
        Attributes finding3 = new Attributes();
        finding3.setString(Tag.ValueType, VR.CS, "CODE");
        finding3.setString(Tag.RelationshipType, VR.CS, "CONTAINS");

        Attributes conceptName3 = new Attributes();
        conceptName3.setString(Tag.CodeValue, VR.SH, "121069");
        conceptName3.setString(Tag.CodingSchemeDesignator, VR.SH, "DCM");
        conceptName3.setString(Tag.CodeMeaning, VR.LO, "Previous Finding");
        finding3.newSequence(Tag.ConceptNameCodeSequence, 1).add(conceptName3);

        Attributes conceptCode3 = new Attributes();
        conceptCode3.setString(Tag.CodeValue, VR.SH, "R91.1");
        conceptCode3.setString(Tag.CodingSchemeDesignator, VR.SH, "ICD10");
        conceptCode3.setString(Tag.CodeMeaning, VR.LO, "Solitary pulmonary nodule");
        finding3.newSequence(Tag.ConceptCodeSequence, 1).add(conceptCode3);

        // Add all findings to Content Sequence
        Sequence contentSequence = dcmAttrs.newSequence(Tag.ContentSequence, 3);
        contentSequence.add(finding1);
        contentSequence.add(finding2);
        contentSequence.add(finding3);

        Path validDicomPath = tempDir.resolve("valid.dcm");
        File dicomFile = validDicomPath.toFile();

        try (DicomOutputStream dos = new DicomOutputStream(dicomFile)) {
            Attributes fmi = dcmAttrs.createFileMetaInformation(UID.ImplicitVRLittleEndian);
            dos.writeDataset(fmi, dcmAttrs);
        }

        String validDicomFile = validDicomPath.toString();

        DicomParser parser = DicomParser.parseFile(validDicomFile);

        assertNotNull(parser);
        assertNotNull(parser.getFileName());
        assertThat(parser.getFileName(), equalTo(validDicomFile));
        assertNotNull(parser.getEntries());
        assertThat(parser.getEntries().size(), greaterThan(0));
    }
}

