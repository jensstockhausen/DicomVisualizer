package de.famst.dicom.visualizer;

import java.awt.*;

import static java.lang.Math.log;

/**
 * Created by jens on 25.05.17.
 */
public class ColorMapper
{
  private static final float MIN_TAG = 0x0002f;
  private static final float MAX_TAG = 0x7FE0f;

  private static final float HUE_OFFSET = 100.0f;
  private static final float MIN_SAT = 50.0f;
  private static final float MAX_SAT = 100.0f;

  static public float groupToHue(int group)
  {
    if (group < MIN_TAG)
    {
      group = (int) MIN_TAG;
    }

    float hue = (360.0f - 0.0f) *
      (float) ((log(group) - log(MIN_TAG)) / (log(MAX_TAG) - MIN_TAG)) + 0.0f;

    hue = hue + HUE_OFFSET;
    if (hue > 360.0f)
    {
      hue = hue - 360.0f;
    }
    return hue;
  }

  static public float elementToSat(int element)
  {
    return map(element, (float) MIN_TAG, (float) MAX_TAG, MIN_SAT, MAX_SAT);
  }

  static private float map(float value, float inMin, float inMax, float outMin, float outMax)
  {
    return outMin + (outMax - outMin) * ((value - inMin) / (inMax - inMin));
  }

  static public Color HSBtoRGB(float hue, float sat, float bri)
  {
    float maxHue = 360.0f;
    float maxSat = 100.0f;
    float maxBri = 100.0f;

    float calcR = 0.0f;
    float calcG = 0.0f;
    float calcB = 0.0f;

    int calcRi;
    int calcGi;
    int calcBi;

    if (hue > maxHue)
      hue = maxHue;
    if (sat > maxSat)
      sat = maxSat;
    if (bri > maxBri)
      bri = maxBri;

    if (hue < 0)
      hue = 0;
    if (sat < 0)
      sat = 0;
    if (bri < 0)
      bri = 0;

    hue /= maxHue; // h
    sat /= maxSat; // s
    bri /= maxBri; // b

    if (sat == 0)
    {  // saturation == 0
      calcR = calcG = calcB = bri;
    }
    else
    {
      float which = (hue - (int) hue) * 6.0f;
      float f = which - (int) which;
      float p = bri * (1.0f - sat);
      float q = bri * (1.0f - sat * f);
      float t = bri * (1.0f - (sat * (1.0f - f)));

      switch ((int) which)
      {
        case 0:
          calcR = bri;
          calcG = t;
          calcB = p;
          break;
        case 1:
          calcR = q;
          calcG = bri;
          calcB = p;
          break;
        case 2:
          calcR = p;
          calcG = bri;
          calcB = t;
          break;
        case 3:
          calcR = p;
          calcG = q;
          calcB = bri;
          break;
        case 4:
          calcR = t;
          calcG = p;
          calcB = bri;
          break;
        case 5:
          calcR = bri;
          calcG = p;
          calcB = q;
          break;
      }
    }

    calcRi = (int) (255 * calcR);
    calcGi = (int) (255 * calcG);
    calcBi = (int) (255 * calcB);

    return new Color(calcRi, calcGi, calcBi);
  }


}
