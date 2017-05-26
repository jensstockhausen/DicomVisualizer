package de.famst.dicom.visualizer;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by jens on 25.05.17.
 */
public class DicomDrawer
{
    DicomParser dicomParser;

    Graphics2D graph;
    int width;
    int height;

    public DicomDrawer(DicomParser dicomParser)
    {
        this.dicomParser = dicomParser;

        width = (int) (dicomParser.length / 1.0f);
        height = 70;
    }

    public String drawToSVG()
    {
        graph = new SVGGraphics2D(width, height);

        graph.setPaint(Color.black);
        graph.fill(new Rectangle2D.Float(0, 0, width, height));

        float borderX = 10;
        float borderY = 5;

        float dHeight = 70;
        float hLevel = 5;

        float oHeight = 10;

        float scaleX = (width - 2 * borderX) / dicomParser.length;
        float y = borderY;

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

            // pixel as black rectangle and P
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
                graph.setStroke(new BasicStroke(0.5f));
                graph.draw(new Rectangle2D.Float(x, y, w - 3.0f, h));

                if (w > 5.0f)
                {
                    Font font = new Font("Sans", Font.PLAIN, 22);
                    graph.setFont(font);

                    float pWidth = (float) graph.getFontMetrics().stringWidth("P");
                    float pHeight = (float) graph.getFontMetrics().getHeight();

                    graph.drawString("P", x + w / 2.0f - 9.0f, y + h * 0.9f - pHeight / 2.2f);
                }
            }

            // tag as rectangle
            else
            {
                graph.setColor(ColorMapper.HSBtoRGB(hue, sat, bri));
                graph.setStroke(new BasicStroke(0.5f));

                if (w > 3.5f)
                {
                    graph.draw(new Rectangle2D.Float(x, y, w - 3.0f, h));
                    graph.fill(new Rectangle2D.Float(x, y, w - 3.0f, h));
                } else
                {
                    graph.draw(new Line2D.Float(x, y, x, y + h));
                }
            }

            // start of SQ as triangle

            if (e.getVr() == VR.SQ)
            {
                Path2D.Double tri = new Path2D.Double();

                tri.moveTo(x, y + h0);
                tri.lineTo(x - hLevel, y + h0 + hLevel * 0.8f);
                tri.lineTo(x + hLevel, y + h0 + hLevel * 0.8f);
                tri.lineTo(x, y + h0);

                graph.fill(tri);
            }

            // marc private tags
            if (e.isPrivateTag())
            {
                hue = ColorMapper.groupToHue(e.getGroup());

                hue = hue + 180.0f;
                if (hue > 360.0f)
                {
                    hue = hue - 360.0f;
                }

                graph.setColor(ColorMapper.HSBtoRGB(hue, sat, 100.0f));
                graph.fill(new Rectangle2D.Float(x, y + h0 + hLevel, w, hLevel * 0.7f));
            }
        });

        SVGGraphics2D svg = (SVGGraphics2D) graph;
        return svg.getSVGDocument();
    }


}
