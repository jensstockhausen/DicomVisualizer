package de.famst.dicom.visualizer;

import org.apache.commons.io.FilenameUtils;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

import java.io.File;

/**
 * Created by jens on 13.05.17.
 */
public class MainApp extends PApplet
{
    private static Logger LOG = LoggerFactory.getLogger(MainApp.class);

    private static DicomParser dicomParser = null;

    public static void run(String fileName)
    {
        dicomParser = new DicomParser(fileName);
        PApplet.main("de.famst.dicom.visualizer.MainApp");
    }

    private String picFileName;

    @Override
    public void settings()
    {
        File dcmFile = new File(dicomParser.fileName);
        picFileName = FilenameUtils.getBaseName(dcmFile.getAbsolutePath()) + ".jpg";

        LOG.info("Writing to  [{}]", picFileName);

        int width = (int) (dicomParser.length / 1.5f);

        size(width, 70);
        smooth(4);
    }

    @Override
    public void setup()
    {
        clear();
        colorMode(HSB, 360, 100, 100);
    }

    @Override
    public void draw()
    {
        background(0);

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

            float hue = getHueForEntry(e);
            float sat = getSatForEntry(e);

            fill(hue, sat, 100.0f);
            stroke(hue, sat, 100.0f);
            strokeWeight(1.0f);

            if (w > 3.5f)
            {
                rect(x, y, w - 3.0f, h);
            } else
            {
                line(x, y, x, y + h);
            }

        });


        dicomParser.entries.forEach(e ->
        {
            float x = borderX + (e.getLogPosition() * scaleX);

            float w = e.getLogLength() * scaleX;

            float h0 = (dHeight - 2 * borderX);
            float h = h0 - (hLevel * e.getLevel());

            float hue = getHueForEntry(e);
            float sat = getSatForEntry(e);

            fill(hue, sat, 100.0f);
            stroke(hue, sat, 100.0f);
            strokeWeight(1.0f);

            if (e.getVr() == VR.SQ)
            {
                fill(hue, sat, 100.0f);
                noStroke();
                triangle(
                    x, y + h0,
                    x - hLevel, y + h0 + hLevel * 0.8f,
                    x + hLevel, y + h0 + hLevel * 0.8f);
            }

            if (e.getTag() == Tag.PixelData)
            {
                fill(hue, 0.0f, 0.0f);
                stroke(hue, 0.0f, 100.0f);
                rect(x, y, w, h);

                fill(hue, 0.0f, 100.0f);
                textAlign(CENTER, CENTER);
                textSize(32.0f);
                text("P", x, y, w, h * 0.9f);
            }

            if (e.isPrivateTag())
            {
                hue = hue + 180.0f;
                if (hue > 360.0f)
                {
                    hue = hue - 360.0f;
                }

                fill(hue, sat, 100.0f);
                stroke(hue, sat, 100.0f);

                rect(x, y + h0 + hLevel, w, hLevel * 0.7f);
            }
        });

        //save(picFileName);
        //exit();
    }

    private float getSatForEntry(DicomEntry e)
    {
        return map(e.getElement(), (float) 0x0002, (float) 0x7FE0, 50.0f, 100.0f);
    }

    private float getHueForEntry(DicomEntry e)
    {
        float hue = (360.0f - 0.0f) * (log(e.getGroup()) - log((float) 0x0002)) / (log((float) 0x7FE0) - (float) 0x0002) + 0.0f;

        hue = hue + 100.0f;
        if (hue > 360.0f)
        {
            hue = hue - 360.0f;
        }
        return hue;
    }

}
