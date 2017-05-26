package de.famst.dicom.visualizer;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.log;

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

        dicomParser.entries.forEach(e ->
        {
            float x = borderX + (e.getLogPosition() * scaleX);

            float w = e.getLogLength() * scaleX;
            float h0 = (dHeight - 2 * borderX);
            float h = h0 - (hLevel * e.getLevel());

            float hue = ColorMapper.groupToHue(e.getGroup());
            float sat = ColorMapper.elementToSat(e.getElement());
            float bri = 100.0f;

            graph.setColor(ColorMapper.HSBtoRGB(hue, sat, bri));

            //fill(hue, sat, 100.0f);
            //stroke(hue, sat, 100.0f);
            //strokeWeight(1.0f);

            graph.setStroke(new BasicStroke(0.5f));

            if (w > 3.5f)
            {
                graph.draw(new Rectangle2D.Float(x, y, w - 3.0f, h));
                graph.fill(new Rectangle2D.Float(x, y, w - 3.0f, h));
            }
            else
            {
                graph.draw(new Line2D.Float(x, y, x, y + h));
            }
        });


        SVGGraphics2D svg = (SVGGraphics2D) graph;
        return svg.getSVGDocument();
    }


}
