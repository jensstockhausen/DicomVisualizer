import de.famst.dicom.visualizer.DicomParser;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class for creating DICOM test files with various configurations.
 * Provides helper methods to generate valid DICOM files for testing purposes.
 */
public final class DicomTestUtils
{
  /**
   * Private constructor to prevent instantiation.
   */
  private DicomTestUtils()
  {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Creates a minimal valid DICOM file with basic required tags.
   *
   * @param tempDir the temporary directory to create the file in
   * @return a DicomParser for the created file
   * @throws Exception if file creation fails
   */
  public static DicomParser createMinimalDicomFile(Path tempDir) throws Exception
  {
    return createMinimalDicomFile(tempDir, "minimal.dcm");
  }

  /**
   * Creates a minimal valid DICOM file with basic required tags.
   *
   * @param tempDir the temporary directory to create the file in
   * @param filename the name of the file to create
   * @return a DicomParser for the created file
   * @throws Exception if file creation fails
   */
  public static DicomParser createMinimalDicomFile(Path tempDir, String filename) throws Exception
  {
    Attributes dcmAttrs = new Attributes();
    dcmAttrs.setString(Tag.PatientName, VR.PN, "Test^Patient");
    dcmAttrs.setString(Tag.PatientID, VR.LO, "12345");
    dcmAttrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.840.113619.2.1.1.1");
    dcmAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.840.113619.2.1.1.2");
    dcmAttrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage);
    dcmAttrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.840.113619.2.1.1.3");
    dcmAttrs.setString(Tag.Modality, VR.CS, "CT");

    return writeDicomFile(tempDir, filename, dcmAttrs);
  }

  /**
   * Creates a comprehensive DICOM file with all required tags, private tags, and sequences.
   *
   * @param tempDir the temporary directory to create the file in
   * @return a DicomParser for the created file
   * @throws Exception if file creation fails
   */
  public static DicomParser createComprehensiveDicomFile(Path tempDir) throws Exception
  {
    return createComprehensiveDicomFile(tempDir, "comprehensive.dcm");
  }

  /**
   * Creates a comprehensive DICOM file with all required tags, private tags, and sequences.
   *
   * @param tempDir the temporary directory to create the file in
   * @param filename the name of the file to create
   * @return a DicomParser for the created file
   * @throws Exception if file creation fails
   */
  public static DicomParser createComprehensiveDicomFile(Path tempDir, String filename) throws Exception
  {
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
    addPrivateTags(dcmAttrs);

    // Structured Report - Content Sequence (findings)
    addStructuredReportSequence(dcmAttrs);

    return writeDicomFile(tempDir, filename, dcmAttrs);
  }

  /**
   * Creates a DICOM file with a simple sequence (no private tags or SR).
   *
   * @param tempDir the temporary directory to create the file in
   * @return a DicomParser for the created file
   * @throws Exception if file creation fails
   */
  public static DicomParser createDicomFileWithSequence(Path tempDir) throws Exception
  {
    return createDicomFileWithSequence(tempDir, "with_sequence.dcm");
  }

  /**
   * Creates a DICOM file with a simple sequence (no private tags or SR).
   *
   * @param tempDir the temporary directory to create the file in
   * @param filename the name of the file to create
   * @return a DicomParser for the created file
   * @throws Exception if file creation fails
   */
  public static DicomParser createDicomFileWithSequence(Path tempDir, String filename) throws Exception
  {
    Attributes dcmAttrs = new Attributes();
    dcmAttrs.setString(Tag.PatientName, VR.PN, "Test^Patient");
    dcmAttrs.setString(Tag.PatientID, VR.LO, "12345");
    dcmAttrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.840.113619.2.1.1.1");
    dcmAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.840.113619.2.1.1.2");
    dcmAttrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage);
    dcmAttrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.840.113619.2.1.1.3");
    dcmAttrs.setString(Tag.Modality, VR.CS, "CT");

    // Add a simple sequence
    Attributes seqItem = new Attributes();
    seqItem.setString(Tag.CodeValue, VR.SH, "121206");
    seqItem.setString(Tag.CodingSchemeDesignator, VR.SH, "DCM");
    seqItem.setString(Tag.CodeMeaning, VR.LO, "Test Code");
    dcmAttrs.newSequence(Tag.ContentSequence, 1).add(seqItem);

    return writeDicomFile(tempDir, filename, dcmAttrs);
  }

  /**
   * Adds private tags to the given attributes.
   *
   * @param dcmAttrs the attributes to add private tags to
   */
  private static void addPrivateTags(Attributes dcmAttrs)
  {
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
  }

  /**
   * Adds a structured report content sequence to the given attributes.
   *
   * @param dcmAttrs the attributes to add the sequence to
   */
  private static void addStructuredReportSequence(Attributes dcmAttrs)
  {
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
    org.dcm4che3.data.Sequence contentSequence = dcmAttrs.newSequence(Tag.ContentSequence, 3);
    contentSequence.add(finding1);
    contentSequence.add(finding2);
    contentSequence.add(finding3);
  }

  /**
   * Writes a DICOM file with the given attributes.
   *
   * @param tempDir the temporary directory to create the file in
   * @param filename the name of the file to create
   * @param dcmAttrs the DICOM attributes to write
   * @return a DicomParser for the created file
   * @throws IOException if writing fails
   */
  private static DicomParser writeDicomFile(Path tempDir, String filename, Attributes dcmAttrs) throws IOException
  {
    Path dicomPath = tempDir.resolve(filename);
    File dicomFile = dicomPath.toFile();

    try (DicomOutputStream dos = new DicomOutputStream(dicomFile))
    {
      Attributes fmi = dcmAttrs.createFileMetaInformation(UID.ImplicitVRLittleEndian);
      dos.writeDataset(fmi, dcmAttrs);
    }

    return DicomParser.parseFile(dicomPath.toString());
  }
}

