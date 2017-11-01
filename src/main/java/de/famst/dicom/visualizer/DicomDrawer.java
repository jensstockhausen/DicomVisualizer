package de.famst.dicom.visualizer;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by jens on 25.05.17.
 */
public class DicomDrawer
{
  private static Logger LOG = LoggerFactory.getLogger(DicomDrawer.class);

  DicomParser dicomParser;

  Graphics2D graph;

  int width;
  int height;
  int vOffset;

  public DicomDrawer(DicomParser dicomParser, Graphics2D graph, int width, int height)
  {
    this.dicomParser = dicomParser;
    this.graph = graph;
    this.width = width;
    this.height = height;
    this.vOffset = 0;
  }

  public DicomDrawer(DicomParser dicomParser, Graphics2D graph, int width, int height, int vOffset)
  {
    this.dicomParser = dicomParser;
    this.graph = graph;
    this.width = width;
    this.height = height;
    this.vOffset = vOffset;
  }

  public Graphics2D draw()
  {
    // background
    graph.setPaint(Color.black);
    graph.fill(new Rectangle2D.Float(0, 0 + vOffset, width, height));

    float borderX = 10;
    float borderY = 5;

    float dHeight = 70;
    float hLevel = 5;

    float scaleX = (width - 2 * borderX) / dicomParser.length;
    float y = borderY + vOffset;

    boolean[] multiFrameMode = new boolean[1];
    multiFrameMode[0] = false;

    dicomParser.entries.forEach(e ->
    {
      float x = borderX + (e.getLogPosition() * scaleX);

      float w = e.getLogLength() * scaleX;
      float h0 = (dHeight - 2 * borderX);
      float h = h0 - (hLevel * e.getLevel());

      float hue = ColorMapper.groupToHue(e.getGroup());
      float sat = ColorMapper.elementToSat(e.getElement());
      float bri = 100.0f;

      LOG.info("[{} {}] -> HSB [{} {} {}]", e.getGroup(), e.getElement(), hue, sat, bri);

      AffineTransform transform = graph.getTransform();

      graph.translate(x, y);

      graph.setStroke(new BasicStroke(0.5f));
      Font font = new Font("Sans", Font.PLAIN, 22);
      graph.setFont(font);

      // pixel data as black rectangle and P
      if ((e.getTag() == Tag.PixelData) ||
        ((e.getTag() == Tag.Item) && multiFrameMode[0]))
      {
        if (e.getLogLength() == 1.0f)
        {
          multiFrameMode[0] = true;
        }

        graph.setColor(ColorMapper.HSBtoRGB(hue, 0.0f, 0.0f));
        graph.fill(new Rectangle2D.Float(x, y, w - 3.0f, h));

        graph.setColor(ColorMapper.HSBtoRGB(hue, 0.0f, 100.0f));
        graph.draw(new Rectangle2D.Float(0.0f, 0.0f, w - 3.0f, h));

        if (w > 5.0f)
        {
          float pWidth = (float) graph.getFontMetrics().stringWidth("P");
          float pHeight = (float) graph.getFontMetrics().getHeight();

          graph.drawString("P", w / 2.0f - pWidth / 1.7f, h * 0.9f - pHeight / 2.2f);
        }
      }

      // tag as rectangle
      else
      {
        graph.setColor(ColorMapper.HSBtoRGB(hue, sat, bri));

        if (w > 3.5f)
        {
          graph.draw(new Rectangle2D.Float(0.0f, 0.0f, w - 3.0f, h));
          graph.fill(new Rectangle2D.Float(0.0f, 0.0f, w - 3.0f, h));
        }
        else
        {
          graph.draw(new Line2D.Float(0.0f, 0.0f, 0.0f, h));
        }
      }

      // annotate below
      graph.translate(0.0f, h0);

      // start of SQ as triangle
      if (e.getVr() == VR.SQ)
      {
        Path2D.Double tri = new Path2D.Double();
        tri.moveTo(0.0f, 0.0f);
        tri.lineTo(-hLevel, hLevel * 0.8f);
        tri.lineTo(+hLevel, hLevel * 0.8f);
        tri.lineTo(0.0f, 0.0f);

        graph.fill(tri);
      }

      // mark private tags
      if (e.isPrivateTag())
      {
        hue = ColorMapper.groupToHue(e.getGroup());

        hue = hue + 180.0f;
        if (hue > 360.0f)
        {
          hue = hue - 360.0f;
        }

        graph.setColor(ColorMapper.HSBtoRGB(hue, sat, 100.0f));
        graph.fill(new Rectangle2D.Float(0.0f, hLevel, w, hLevel * 0.7f));
      }

      graph.setTransform(transform);

    });

    return graph;
  }


}
