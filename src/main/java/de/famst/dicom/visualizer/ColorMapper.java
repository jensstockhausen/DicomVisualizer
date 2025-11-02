package de.famst.dicom.visualizer;

import java.awt.*;

import static java.lang.Math.log;

/**
 * Utility class for mapping DICOM tag components (group and element) to colors.
 * Provides color generation based on DICOM tag structure for visualization purposes.
 */
public final class ColorMapper
{
  private static final float MIN_TAG = 0x0002f;
  private static final float MAX_TAG = 0x7FE0f;

  private static final float HUE_OFFSET = 100.0f;
  private static final float MIN_SAT = 80.0f;
  private static final float MAX_SAT = 100.0f;

  private static final float MAX_HUE = 360.0f;
  private static final float MAX_SAT_PERCENT = 100.0f;
  private static final float MAX_BRIGHTNESS = 100.0f;
  private static final float HUE_SECTIONS = 6.0f;
  private static final int RGB_MAX = 255;

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private ColorMapper()
  {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Maps a DICOM group number to a hue value (0-360).
   * Uses logarithmic scaling to distribute colors across the hue spectrum.
   *
   * @param group the DICOM group number
   * @return the hue value in degrees (0-360)
   */
  public static float groupToHue(int group)
  {
    int normalizedGroup = Math.max(1, group);

    float hue = MAX_HUE * (float) (log(normalizedGroup) / log(MAX_TAG));
    hue += HUE_OFFSET;

    if (hue > MAX_HUE)
    {
      hue -= MAX_HUE;
    }

    return hue;
  }

  /**
   * Maps a DICOM element number to a saturation value (80-100).
   * Uses linear mapping to vary saturation based on element number.
   *
   * @param element the DICOM element number
   * @return the saturation value (80-100)
   */
  public static float elementToSat(int element)
  {
    return map(element, MIN_TAG, MAX_TAG, MIN_SAT, MAX_SAT);
  }

  /**
   * Maps a value from one range to another range linearly.
   *
   * @param value the value to map
   * @param inMin the minimum of the input range
   * @param inMax the maximum of the input range
   * @param outMin the minimum of the output range
   * @param outMax the maximum of the output range
   * @return the mapped value in the output range
   */
  private static float map(float value, float inMin, float inMax, float outMin, float outMax)
  {
    return outMin + (outMax - outMin) * ((value - inMin) / (inMax - inMin));
  }

  /**
   * Converts HSB (Hue, Saturation, Brightness) color values to RGB Color object.
   * This implementation uses a custom HSB to RGB conversion algorithm.
   *
   * @param hue the hue value (0-360 degrees)
   * @param sat the saturation value (0-100 percent)
   * @param bri the brightness value (0-100 percent)
   * @return a Color object representing the RGB values
   */
  public static Color HSBtoRGB(float hue, float sat, float bri)
  {
    // Clamp values to valid ranges
    hue = clamp(hue, 0.0f, MAX_HUE);
    sat = clamp(sat, 0.0f, MAX_SAT_PERCENT);
    bri = clamp(bri, 0.0f, MAX_BRIGHTNESS);

    // Normalize to 0-1 range
    hue /= MAX_HUE;
    sat /= MAX_SAT_PERCENT;
    bri /= MAX_BRIGHTNESS;

    float calcR;
    float calcG;
    float calcB;

    if (sat == 0.0f)
    {
      // Achromatic (gray)
      calcR = calcG = calcB = bri;
    }
    else
    {
      float which = (hue - (int) hue) * HUE_SECTIONS;
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
        default:
          // Should never happen, but default to gray
          calcR = calcG = calcB = bri;
          break;
      }
    }

    int calcRi = (int) (RGB_MAX * calcR);
    int calcGi = (int) (RGB_MAX * calcG);
    int calcBi = (int) (RGB_MAX * calcB);

    return new Color(calcRi, calcGi, calcBi);
  }

  /**
   * Clamps a value between a minimum and maximum.
   *
   * @param value the value to clamp
   * @param min the minimum value
   * @param max the maximum value
   * @return the clamped value
   */
  private static float clamp(float value, float min, float max)
  {
    if (value < min)
    {
      return min;
    }

    return Math.min(value, max);
  }
}
