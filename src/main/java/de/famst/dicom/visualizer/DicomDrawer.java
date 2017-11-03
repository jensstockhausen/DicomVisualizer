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
    LOG.info("Init draw");

    // background
    graph.setPaint(Color.black);
    graph.fill(new Rectangle2D.Float(0, 0 + vOffset, width, height));

    float borderX = 20;
    float borderY = 5;

    float dHeight = 70;
    float hLevel = 5;

    float scaleX = (width - 1.5f * borderX) / dicomParser.length;
    float y = borderY + vOffset;

    boolean[] multiFrameMode = new boolean[1];
    multiFrameMode[0] = false;

    graph.setStroke(new BasicStroke(0.5f));
    Font font = new Font("Sans", Font.PLAIN, 22);
    graph.setFont(font);

    float pWidth = (float) graph.getFontMetrics().stringWidth("P");
    float pHeight = (float) graph.getFontMetrics().getHeight();

    AffineTransform transform = graph.getTransform();

    dicomParser.entries.forEach(e ->
    {
      float x = borderX + (e.getLogPosition() * scaleX);

      float w = e.getLogLength() * scaleX;
      float h0 = (dHeight - 4 * borderY);
      float h = h0 - (hLevel * e.getLevel());

      float hue = ColorMapper.groupToHue(e.getGroup());
      float sat = ColorMapper.elementToSat(e.getElement());
      float bri = 100.0f;

      LOG.trace("[{} {} {}] -> HSB [{} {} {}]", e.getGroup(), e.getElement(), e.getLogLength(), hue, sat, bri);

      StringBuilder sb = new StringBuilder();

      graph.translate(x, y);

      // pixel data as black rectangle and P
      if ((e.getTag() == Tag.PixelData) ||
        ((e.getTag() == Tag.Item) && multiFrameMode[0]))
      {
        sb.append("PX_");

        if (e.getLogLength() == 1.0f)
        {
          multiFrameMode[0] = true;
          sb.append("MF_");
        }

        graph.setColor(ColorMapper.HSBtoRGB(hue, 0.0f, 0.0f));
        graph.fill(new Rectangle2D.Float(x, y, w - 3.0f, h));

        graph.setColor(ColorMapper.HSBtoRGB(hue, 0.0f, 100.0f));
        graph.draw(new Rectangle2D.Float(0.0f, 0.0f, w - 3.0f, h));

        if (w > 5.0f)
        {
          graph.drawString("P", w / 2.0f - pWidth / 1.7f, h * 0.9f - pHeight / 2.2f);
        }
      }

      // tag as rectangle
      else
      {
        graph.setColor(ColorMapper.HSBtoRGB(hue, sat, bri));

        if (w > 3.5f)
        {
          sb.append("R_");

          graph.draw(new Rectangle2D.Float(0.0f, 0.0f, w - 3.0f, h));
          graph.fill(new Rectangle2D.Float(0.0f, 0.0f, w - 3.0f, h));
        }
        else
        {
          sb.append("r_");

          graph.draw(new Line2D.Float(0.0f, 0.0f, 0.0f, h));
        }
      }

      // annotate below
      graph.translate(0.0f, h0);

      // start of SQ as triangle
      if (e.getVr() == VR.SQ)
      {
        sb.append("SQ_");

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
        sb.append("PT_");

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

      LOG.trace("{}", sb);
    });

    graph.setFont(new Font("Sans", Font.PLAIN, 12));

    graph.translate(borderX, vOffset + dHeight - 3.0f);

    graph.setColor(ColorMapper.HSBtoRGB(0.0f, 0.0f, 100.0f));
    graph.drawString(dicomParser.modality + " - " + dicomParser.siuid, 0.0f, 0.0f);

    graph.setTransform(transform);

    return graph;
  }


}
