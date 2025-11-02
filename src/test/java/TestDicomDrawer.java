import de.famst.dicom.visualizer.DicomDrawer;
import de.famst.dicom.visualizer.DicomParser;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DicomDrawer
 */
public class TestDicomDrawer
{
  @Test
  public void testConstructorWithNullDicomParser()
  {
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomDrawer(null, graph, 800, 600),
      "Expected constructor to throw IllegalArgumentException for null DicomParser"
    );

    assertThat(exception.getMessage(), containsString("DicomParser cannot be null"));
  }

  @Test
  public void testConstructorWithNullGraphics(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomDrawer(parser, null, 800, 600),
      "Expected constructor to throw IllegalArgumentException for null graphics"
    );

    assertThat(exception.getMessage(), containsString("Graphics context cannot be null"));
  }

  @Test
  public void testConstructorWithZeroWidth(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomDrawer(parser, graph, 0, 600),
      "Expected constructor to throw IllegalArgumentException for zero width"
    );

    assertThat(exception.getMessage(), containsString("Width must be positive"));
  }

  @Test
  public void testConstructorWithNegativeWidth(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomDrawer(parser, graph, -100, 600),
      "Expected constructor to throw IllegalArgumentException for negative width"
    );

    assertThat(exception.getMessage(), containsString("Width must be positive"));
    assertThat(exception.getMessage(), containsString("-100"));
  }

  @Test
  public void testConstructorWithZeroHeight(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomDrawer(parser, graph, 800, 0),
      "Expected constructor to throw IllegalArgumentException for zero height"
    );

    assertThat(exception.getMessage(), containsString("Height must be positive"));
  }

  @Test
  public void testConstructorWithNegativeHeight(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DicomDrawer(parser, graph, 800, -50),
      "Expected constructor to throw IllegalArgumentException for negative height"
    );

    assertThat(exception.getMessage(), containsString("Height must be positive"));
    assertThat(exception.getMessage(), containsString("-50"));
  }

  @Test
  public void testConstructorWithoutVerticalOffset(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 800, 600);
    assertNotNull(drawer);
  }

  @Test
  public void testConstructorWithVerticalOffset(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 800, 600, 100);
    assertNotNull(drawer);
  }

  @Test
  public void testDrawReturnsGraphics(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 800, 600);
    Graphics2D result = drawer.draw();

    assertNotNull(result);
    assertThat(result, is(instanceOf(SVGGraphics2D.class)));
  }

  @Test
  public void testDrawGeneratesValidSVG(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 800, 600);
    drawer.draw();

    String svgDocument = graph.getSVGDocument();
    assertNotNull(svgDocument);
    assertThat(svgDocument, containsString("svg"));
    assertThat(svgDocument, not(isEmptyString()));
  }

  @Test
  public void testDrawWithVerticalOffset(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 1200);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 800, 600, 200);
    Graphics2D result = drawer.draw();

    assertNotNull(result);
    String svgDocument = ((SVGGraphics2D) result).getSVGDocument();
    assertNotNull(svgDocument);
  }

  @Test
  public void testDrawWithSmallDimensions(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(100, 100);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 100, 100);
    Graphics2D result = drawer.draw();

    assertNotNull(result);
  }

  @Test
  public void testDrawWithLargeDimensions(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createMinimalDicomFile(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(2000, 2000);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 2000, 2000);
    Graphics2D result = drawer.draw();

    assertNotNull(result);
  }

  @Test
  public void testDrawWithDicomFileContainingSequences(@TempDir Path tempDir) throws Exception
  {
    DicomParser parser = DicomTestUtils.createDicomFileWithSequence(tempDir);
    SVGGraphics2D graph = new SVGGraphics2D(800, 600);

    DicomDrawer drawer = new DicomDrawer(parser, graph, 800, 600);
    Graphics2D result = drawer.draw();

    assertNotNull(result);
    String svgDocument = ((SVGGraphics2D) result).getSVGDocument();
    assertNotNull(svgDocument);
  }
}

