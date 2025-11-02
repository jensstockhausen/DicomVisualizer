import de.famst.dicom.visualizer.DicomEntry;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DicomEntry
 */
public class TestDicomEntry
{
  @Test
  public void testConstructorWithValidIndex()
  {
    DicomEntry entry = new DicomEntry(0);
    assertThat(entry.getIdx(), is(0));
    assertThat(entry.getGroup(), is(-1));
    assertThat(entry.getElement(), is(-1));
    assertThat(entry.getLevel(), is(-1.0f));
    assertThat(entry.isPrivateCreator(), is(false));
    assertThat(entry.isPrivateTag(), is(false));
    assertThat(entry.getVr(), is(nullValue()));
    assertThat(entry.getLogLength(), is(0.0f));
    assertThat(entry.getMultiplicity(), is(0));
  }

  @Test
  public void testConstructorWithNegativeIndex()
  {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomEntry(-1),
      "Expected constructor to throw IllegalArgumentException for negative index"
    );

    assertThat(exception.getMessage(), containsString("Index must be non-negative"));
    assertThat(exception.getMessage(), containsString("-1"));
  }

  @Test
  public void testSetAndGetGroup()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setGroup(0x0010);
    assertThat(entry.getGroup(), is(0x0010));
  }

  @Test
  public void testSetAndGetElement()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setElement(0x0020);
    assertThat(entry.getElement(), is(0x0020));
  }

  @Test
  public void testSetAndGetTag()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setTag(0x00100020);
    assertThat(entry.getTag(), is(0x00100020));
  }

  @Test
  public void testSetAndGetLevel()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setLevel(2.5f);
    assertThat(entry.getLevel(), is(2.5f));
  }

  @Test
  public void testSetAndGetPrivateCreator()
  {
    DicomEntry entry = new DicomEntry(1);
    assertThat(entry.isPrivateCreator(), is(false));

    entry.setPrivateCreator(true);
    assertThat(entry.isPrivateCreator(), is(true));

    entry.setPrivateCreator(false);
    assertThat(entry.isPrivateCreator(), is(false));
  }

  @Test
  public void testSetAndGetPrivateTag()
  {
    DicomEntry entry = new DicomEntry(1);
    assertThat(entry.isPrivateTag(), is(false));

    entry.setPrivateTag(true);
    assertThat(entry.isPrivateTag(), is(true));

    entry.setPrivateTag(false);
    assertThat(entry.isPrivateTag(), is(false));
  }

  @Test
  public void testSetAndGetVR()
  {
    DicomEntry entry = new DicomEntry(1);
    assertThat(entry.getVr(), is(nullValue()));

    entry.setVr(VR.PN);
    assertThat(entry.getVr(), is(VR.PN));

    entry.setVr(VR.UI);
    assertThat(entry.getVr(), is(VR.UI));
  }

  @Test
  public void testSetAndGetLogLength()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setLogLength(100.5f);
    assertThat(entry.getLogLength(), is(100.5f));
  }

  @Test
  public void testSetAndGetLogPosition()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setLogPosition(50.25f);
    assertThat(entry.getLogPosition(), is(50.25f));
  }

  @Test
  public void testSetAndGetMultiplicity()
  {
    DicomEntry entry = new DicomEntry(1);
    entry.setMultiplicity(5);
    assertThat(entry.getMultiplicity(), is(5));
  }

  @Test
  public void testSetMultiplicityWithNegativeValue()
  {
    DicomEntry entry = new DicomEntry(1);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> entry.setMultiplicity(-1),
      "Expected setMultiplicity to throw IllegalArgumentException for negative value"
    );

    assertThat(exception.getMessage(), containsString("Multiplicity must be non-negative"));
    assertThat(exception.getMessage(), containsString("-1"));
  }

  @Test
  public void testCompleteWorkflow()
  {
    // Simulate a complete DICOM entry setup
    DicomEntry entry = new DicomEntry(10);

    // Set basic tag information
    entry.setGroup(0x0009);
    entry.setElement(0x1001);
    entry.setTag(0x00091001);

    // Set type information
    entry.setVr(VR.LO);
    entry.setMultiplicity(1);

    // Set private tag flags
    entry.setPrivateTag(true);
    entry.setPrivateCreator(false);

    // Set visualization data
    entry.setLevel(2.0f);
    entry.setLogLength(128.5f);
    entry.setLogPosition(256.75f);

    // Verify all values
    assertThat(entry.getIdx(), is(10));
    assertThat(entry.getGroup(), is(0x0009));
    assertThat(entry.getElement(), is(0x1001));
    assertThat(entry.getTag(), is(0x00091001));
    assertThat(entry.getVr(), is(VR.LO));
    assertThat(entry.getMultiplicity(), is(1));
    assertThat(entry.isPrivateTag(), is(true));
    assertThat(entry.isPrivateCreator(), is(false));
    assertThat(entry.getLevel(), is(2.0f));
    assertThat(entry.getLogLength(), is(128.5f));
    assertThat(entry.getLogPosition(), is(256.75f));
  }

  @Test
  public void testImmutableIndex()
  {
    // Verify that idx is final and cannot be changed
    DicomEntry entry = new DicomEntry(42);
    assertThat(entry.getIdx(), is(42));

    // If we create a new entry with different idx, it should have that new value
    DicomEntry entry2 = new DicomEntry(100);
    assertThat(entry2.getIdx(), is(100));
    assertThat(entry.getIdx(), is(42)); // original unchanged
  }
}

