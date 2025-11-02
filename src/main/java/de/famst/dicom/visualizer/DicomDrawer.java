package de.famst.dicom.visualizer;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drawer for DICOM files that creates an SVG visualization of DICOM tag structure.
 * Renders tags as colored rectangles based on group and element numbers, with special
 * handling for sequences, pixel data, and private tags.
 */
public class DicomDrawer
{
  private static final Logger LOG = LoggerFactory.getLogger(DicomDrawer.class);

  // Layout constants
  private static final float BORDER_X = 20.0f;
  private static final float BORDER_Y = 5.0f;
  private static final float DRAWING_HEIGHT = 70.0f;
  private static final float LEVEL_HEIGHT = 5.0f;
  private static final float RECTANGLE_GAP = 3.0f;
  private static final float MIN_WIDTH_FOR_RECTANGLE = 3.5f;
  private static final float MIN_WIDTH_FOR_TEXT = 5.0f;

  // Color constants
  private static final float MAX_BRIGHTNESS = 100.0f;
  private static final float ZERO_SATURATION = 0.0f;
  private static final float HUE_SHIFT = 180.0f;
  private static final float MAX_HUE = 360.0f;

  // Font constants
  private static final int MAIN_FONT_SIZE = 22;
  private static final int LABEL_FONT_SIZE = 12;
  private static final String FONT_NAME = "Sans";

  // Stroke constants
  private static final float STROKE_WIDTH = 0.5f;

  // Text positioning constants
  private static final float TEXT_WIDTH_DIVISOR = 1.7f;
  private static final float TEXT_HEIGHT_DIVISOR = 2.2f;
  private static final float TEXT_VERTICAL_POSITION = 0.9f;
  private static final float PRIVATE_TAG_HEIGHT_RATIO = 0.7f;
  private static final float TRIANGLE_HEIGHT_RATIO = 0.8f;

  // Layout offsets
  private static final float SCALE_OFFSET = 1.5f;
  private static final float LABEL_OFFSET = 3.0f;

  private final DicomParser dicomParser;
  private final Graphics2D graph;
  private final int width;
  private final int height;
  private final int verticalOffset;

  /**
   * Creates a new DicomDrawer with no vertical offset.
   *
   * @param dicomParser the DICOM parser containing the parsed data
   * @param graph the graphics context to draw on
   * @param width the width of the drawing area
   * @param height the height of the drawing area
   */
  public DicomDrawer(DicomParser dicomParser, Graphics2D graph, int width, int height)
  {
    this(dicomParser, graph, width, height, 0);
  }

  /**
   * Creates a new DicomDrawer with a specified vertical offset.
   *
   * @param dicomParser the DICOM parser containing the parsed data
   * @param graph the graphics context to draw on
   * @param width the width of the drawing area
   * @param height the height of the drawing area
   * @param verticalOffset the vertical offset for drawing (used when drawing multiple instances)
   * @throws IllegalArgumentException if dicomParser or graph is null, or dimensions are invalid
   */
  public DicomDrawer(DicomParser dicomParser, Graphics2D graph, int width, int height, int verticalOffset)
  {
    if (dicomParser == null)
    {
      throw new IllegalArgumentException("DicomParser cannot be null");
    }
    if (graph == null)
    {
      throw new IllegalArgumentException("Graphics context cannot be null");
    }
    if (width <= 0)
    {
      throw new IllegalArgumentException("Width must be positive, got: " + width);
    }
    if (height <= 0)
    {
      throw new IllegalArgumentException("Height must be positive, got: " + height);
    }

    this.dicomParser = dicomParser;
    this.graph = graph;
    this.width = width;
    this.height = height;
    this.verticalOffset = verticalOffset;
  }

  /**
   * Draws the DICOM visualization onto the graphics context.
   *
   * @return the graphics context with the drawing applied
   */
  public Graphics2D draw()
  {
    LOG.info("Drawing DICOM visualization for {}", dicomParser.getFileName());

    drawBackground();

    float scaleX = calculateHorizontalScale();
    float yPosition = BORDER_Y + verticalOffset;

    AffineTransform originalTransform = graph.getTransform();

    setupFont();
    FontMetrics fontMetrics = graph.getFontMetrics();

    AtomicBoolean multiFrameMode = new AtomicBoolean(false);

    dicomParser.getEntries().forEach(entry ->
    {
      drawEntry(entry, scaleX, yPosition, fontMetrics, multiFrameMode, originalTransform);
    });

    drawLabel(originalTransform);

    return graph;
  }

  /**
   * Draws the background rectangle.
   */
  private void drawBackground()
  {
    graph.setPaint(Color.BLACK);
    graph.fill(new Rectangle2D.Float(0, verticalOffset, width, height));
  }

  /**
   * Calculates the horizontal scale factor based on drawing width and DICOM length.
   */
  private float calculateHorizontalScale()
  {
    return (width - SCALE_OFFSET * BORDER_X) / dicomParser.getLength();
  }

  /**
   * Sets up the font and stroke for drawing.
   */
  private void setupFont()
  {
    graph.setStroke(new BasicStroke(STROKE_WIDTH));
    Font font = new Font(FONT_NAME, Font.PLAIN, MAIN_FONT_SIZE);
    graph.setFont(font);
  }

  /**
   * Draws a single DICOM entry.
   */
  private void drawEntry(DicomEntry entry, float scaleX, float yPosition, FontMetrics fontMetrics,
                         AtomicBoolean multiFrameMode, AffineTransform originalTransform)
  {
    float xPosition = BORDER_X + (entry.getLogPosition() * scaleX);
    float entryWidth = entry.getLogLength() * scaleX;
    float maxHeight = DRAWING_HEIGHT - 4 * BORDER_Y;
    float entryHeight = maxHeight - (LEVEL_HEIGHT * entry.getLevel());

    float hue = ColorMapper.groupToHue(entry.getGroup());
    float saturation = ColorMapper.elementToSat(entry.getElement());

    LOG.trace("Drawing entry [{},{},{}] -> HSB [{},{},{}]",
              entry.getGroup(), entry.getElement(), entry.getLogLength(),
              hue, saturation, MAX_BRIGHTNESS);

    graph.translate(xPosition, yPosition);

    if (isPixelDataOrMultiFrameItem(entry, multiFrameMode))
    {
      drawPixelData(entry, entryWidth, entryHeight, hue, fontMetrics, multiFrameMode);
    }
    else
    {
      drawRegularTag(entry, entryWidth, entryHeight, hue, saturation);
    }

    graph.translate(0.0f, maxHeight);

    if (entry.getVr() == VR.SQ)
    {
      drawSequenceMarker();
    }

    if (entry.isPrivateTag())
    {
      drawPrivateTagMarker(entry, entryWidth, hue, saturation);
    }

    graph.setTransform(originalTransform);
  }

  /**
   * Checks if the entry is pixel data or a multi-frame item.
   */
  private boolean isPixelDataOrMultiFrameItem(DicomEntry entry, AtomicBoolean multiFrameMode)
  {
    return (entry.getTag() == Tag.PixelData) ||
           ((entry.getTag() == Tag.Item) && multiFrameMode.get());
  }

  /**
   * Draws pixel data as a black rectangle with a "P" marker.
   */
  private void drawPixelData(DicomEntry entry, float width, float height, float hue,
                             FontMetrics fontMetrics, AtomicBoolean multiFrameMode)
  {
    if (entry.getLogLength() == 1.0f)
    {
      multiFrameMode.set(true);
    }

    // Draw black filled rectangle
    graph.setColor(ColorMapper.HSBtoRGB(hue, ZERO_SATURATION, ZERO_SATURATION));
    graph.fill(new Rectangle2D.Float(0.0f, 0.0f, width - RECTANGLE_GAP, height));

    // Draw white outline
    graph.setColor(ColorMapper.HSBtoRGB(hue, ZERO_SATURATION, MAX_BRIGHTNESS));
    graph.draw(new Rectangle2D.Float(0.0f, 0.0f, width - RECTANGLE_GAP, height));

    // Draw "P" label if there's enough space
    if (width > MIN_WIDTH_FOR_TEXT)
    {
      float pWidth = fontMetrics.stringWidth("P");
      float pHeight = fontMetrics.getHeight();
      float textX = width / 2.0f - pWidth / TEXT_WIDTH_DIVISOR;
      float textY = height * TEXT_VERTICAL_POSITION - pHeight / TEXT_HEIGHT_DIVISOR;
      graph.drawString("P", textX, textY);
    }
  }

  /**
   * Draws a regular DICOM tag as a colored rectangle or line.
   */
  private void drawRegularTag(DicomEntry entry, float width, float height, float hue, float saturation)
  {
    Color color = ColorMapper.HSBtoRGB(hue, saturation, MAX_BRIGHTNESS);
    graph.setColor(color);

    if (width > MIN_WIDTH_FOR_RECTANGLE)
    {
      // Draw as rectangle
      Rectangle2D.Float rect = new Rectangle2D.Float(0.0f, 0.0f, width - RECTANGLE_GAP, height);
      graph.draw(rect);
      graph.fill(rect);
    }
    else
    {
      // Draw as thin line
      graph.draw(new Line2D.Float(0.0f, 0.0f, 0.0f, height));
    }
  }

  /**
   * Draws a triangle marker for sequence (SQ) tags.
   */
  private void drawSequenceMarker()
  {
    Path2D.Double triangle = new Path2D.Double();
    triangle.moveTo(0.0f, 0.0f);
    triangle.lineTo(-LEVEL_HEIGHT, LEVEL_HEIGHT * TRIANGLE_HEIGHT_RATIO);
    triangle.lineTo(+LEVEL_HEIGHT, LEVEL_HEIGHT * TRIANGLE_HEIGHT_RATIO);
    triangle.lineTo(0.0f, 0.0f);
    graph.fill(triangle);
  }

  /**
   * Draws a marker for private tags using a complementary color.
   */
  private void drawPrivateTagMarker(DicomEntry entry, float width, float hue, float saturation)
  {
    float complementaryHue = hue + HUE_SHIFT;
    if (complementaryHue > MAX_HUE)
    {
      complementaryHue -= MAX_HUE;
    }

    graph.setColor(ColorMapper.HSBtoRGB(complementaryHue, saturation, MAX_BRIGHTNESS));
    graph.fill(new Rectangle2D.Float(0.0f, LEVEL_HEIGHT, width, LEVEL_HEIGHT * PRIVATE_TAG_HEIGHT_RATIO));
  }

  /**
   * Draws the label at the bottom with modality and instance UID.
   */
  private void drawLabel(AffineTransform originalTransform)
  {
    graph.setFont(new Font(FONT_NAME, Font.PLAIN, LABEL_FONT_SIZE));
    graph.translate(BORDER_X, verticalOffset + DRAWING_HEIGHT - LABEL_OFFSET);
    graph.setColor(ColorMapper.HSBtoRGB(0.0f, ZERO_SATURATION, MAX_BRIGHTNESS));

    String label = dicomParser.getModality() + " - " + dicomParser.getSiuid();
    graph.drawString(label, 0.0f, 0.0f);

    graph.setTransform(originalTransform);
  }

}
