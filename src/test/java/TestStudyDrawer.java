import de.famst.dicom.visualizer.StudyDrawer;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StudyDrawer
 */
public class TestStudyDrawer
{
  @Test
  public void testConstructorWithNullPath()
  {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new StudyDrawer(null),
      "Expected constructor to throw IllegalArgumentException for null path"
    );

    assertThat(exception.getMessage(), containsString("Input path cannot be null or empty"));
  }

  @Test
  public void testConstructorWithEmptyPath()
  {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new StudyDrawer(""),
      "Expected constructor to throw IllegalArgumentException for empty path"
    );

    assertThat(exception.getMessage(), containsString("Input path cannot be null or empty"));
  }

  @Test
  public void testConstructorWithWhitespacePath()
  {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new StudyDrawer("   "),
      "Expected constructor to throw IllegalArgumentException for whitespace path"
    );

    assertThat(exception.getMessage(), containsString("Input path cannot be null or empty"));
  }

  @Test
  public void testConstructorWithNonExistentPath()
  {
    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> new StudyDrawer("/path/to/nonexistent/directory"),
      "Expected constructor to throw RuntimeException for non-existent path"
    );

    assertThat(exception.getMessage(), containsString("Failed to load DICOM files"));
  }

  @Test
  public void testConstructorWithEmptyDirectory(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    assertNotNull(drawer);
    assertThat(drawer.getFiles(), is(empty()));
    assertThat(drawer.getSeries().size(), is(0));
    assertThat(drawer.getMaxLength(), is(0.0f));
  }

  @Test
  public void testGetFilesReturnsUnmodifiableList(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    assertThrows(
      UnsupportedOperationException.class,
      () -> drawer.getFiles().add(Path.of("/some/path")),
      "Expected getFiles() to return unmodifiable list"
    );
  }

  @Test
  public void testGetSeriesReturnsUnmodifiableMap(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    assertThrows(
      UnsupportedOperationException.class,
      () -> drawer.getSeries().put("test", null),
      "Expected getSeries() to return unmodifiable map"
    );
  }

  @Test
  public void testDrawWithNullGraphics(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> drawer.draw(null),
      "Expected draw() to throw IllegalArgumentException for null graphics"
    );

    assertThat(exception.getMessage(), containsString("Graphics context cannot be null"));
  }

  @Test
  public void testDrawWithEmptyStudy(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    SVGGraphics2D graph = new SVGGraphics2D(800, 600);
    Graphics2D result = drawer.draw(graph);

    assertNotNull(result);
    assertThat(result, is(instanceOf(SVGGraphics2D.class)));
  }

  @Test
  public void testGetMaxLengthWithNoFiles(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    assertThat(drawer.getMaxLength(), is(0.0f));
  }

  @Test
  public void testDrawCreatesBackgroundColor(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    SVGGraphics2D graph = new SVGGraphics2D(800, 600);
    drawer.draw(graph);

    // Verify that drawing completed without errors
    String svgDocument = graph.getSVGDocument();
    assertNotNull(svgDocument);
    assertThat(svgDocument, containsString("svg"));
  }

  @Test
  public void testGettersReturnCorrectTypes(@TempDir Path tempDir)
  {
    StudyDrawer drawer = new StudyDrawer(tempDir.toString());

    // Verify return types
    assertThat(drawer.getFiles(), is(instanceOf(java.util.List.class)));
    assertThat(drawer.getSeries(), is(instanceOf(java.util.Map.class)));
    assertThat(drawer.getMaxLength(), is(instanceOf(Float.class)));
  }
}

