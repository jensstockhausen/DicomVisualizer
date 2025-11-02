import de.famst.dicom.visualizer.ColorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestColorMapper
{

  static private Stream<? extends Arguments> groupToHueValues()
  {
    return Stream.of(
      Arguments.of(+0, 100.0f),
      Arguments.of(-5, 100.0f),
      Arguments.of(+30, 192.97f),
      Arguments.of(+100, 225.89f),
      Arguments.of(+1000, 288.83f),
      Arguments.of(+10000, 351.78f),
      Arguments.of(+530000, 100.32f)
    );
  }

  @ParameterizedTest(name = "{0} maps to {1}")
  @MethodSource("groupToHueValues")
  public void mappingGroupToHue(int group, float expectedHue)
  {
    float hue = ColorMapper.groupToHue(group);
    assertThat((double) hue, is(closeTo(expectedHue, 1e-2)));
  }

  static private Stream<? extends Arguments> elementToSatValues()
  {
    return Stream.of(
      Arguments.of(+0, 79.99f),
      Arguments.of(-5, 79.99f),
      Arguments.of(+30, 79.99f),
      Arguments.of(+100, 80.00f),
      Arguments.of(+1000, 80.04f),
      Arguments.of(+10000, 80.38f),
      Arguments.of(+530000, 100.23f)
    );
  }

  @ParameterizedTest(name = "{0} maps to {1}")
  @MethodSource("elementToSatValues")
  public void mappingElementToSat(int element, float expectedSat)
  {
    float sat = ColorMapper.elementToSat(element);
    assertThat((double)sat, is(closeTo(expectedSat, 1e-2)));
  }

  @Test
  public void testHSBtoRGBWithValidValues()
  {
    // Test pure red
    Color red = ColorMapper.HSBtoRGB(0.0f, 100.0f, 100.0f);
    assertThat(red.getRed(), is(255));
    assertThat(red.getGreen(), is(0));
    assertThat(red.getBlue(), is(0));

    // Test pure green
    Color green = ColorMapper.HSBtoRGB(120.0f, 100.0f, 100.0f);
    assertThat(green.getRed(), is(0));
    assertThat(green.getGreen(), is(255));
    assertThat(green.getBlue(), is(0));

    // Test pure blue
    Color blue = ColorMapper.HSBtoRGB(240.0f, 100.0f, 100.0f);
    assertThat(blue.getRed(), is(0));
    assertThat(blue.getGreen(), is(0));
    assertThat(blue.getBlue(), is(255));
  }

  @Test
  public void testHSBtoRGBWithGray()
  {
    // Saturation = 0 should produce gray
    Color gray = ColorMapper.HSBtoRGB(0.0f, 0.0f, 50.0f);
    assertThat(gray.getRed(), is(gray.getGreen()));
    assertThat(gray.getGreen(), is(gray.getBlue()));
    assertThat((double) gray.getRed(), is(closeTo(127, 1)));
  }

  @Test
  public void testHSBtoRGBWithBlack()
  {
    // Brightness = 0 should produce black
    Color black = ColorMapper.HSBtoRGB(180.0f, 100.0f, 0.0f);
    assertThat(black.getRed(), is(0));
    assertThat(black.getGreen(), is(0));
    assertThat(black.getBlue(), is(0));
  }

  @Test
  public void testHSBtoRGBWithWhite()
  {
    // Saturation = 0 and Brightness = 100 should produce white
    Color white = ColorMapper.HSBtoRGB(0.0f, 0.0f, 100.0f);
    assertThat(white.getRed(), is(255));
    assertThat(white.getGreen(), is(255));
    assertThat(white.getBlue(), is(255));
  }

  @Test
  public void testHSBtoRGBClampsValues()
  {
    // Values beyond valid range should be clamped
    Color clamped1 = ColorMapper.HSBtoRGB(400.0f, 150.0f, 150.0f);
    assertNotNull(clamped1);

    Color clamped2 = ColorMapper.HSBtoRGB(-50.0f, -10.0f, -20.0f);
    assertThat(clamped2.getRed(), is(0));
    assertThat(clamped2.getGreen(), is(0));
    assertThat(clamped2.getBlue(), is(0));
  }

  @Test
  public void testHSBtoRGBAllHueSections()
  {
    // Test all 6 sections of the hue wheel
    for (int section = 0; section < 6; section++)
    {
      float hue = section * 60.0f + 30.0f; // Middle of each section
      Color color = ColorMapper.HSBtoRGB(hue, 100.0f, 100.0f);
      assertNotNull(color);

      // Verify that at least one color component is at maximum
      int max = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
      assertThat(max, is(255));
    }
  }

  @Test
  public void testGroupToHueWrapAround()
  {
    // Test that hue wraps around correctly after exceeding 360
    float hue = ColorMapper.groupToHue(530000);
    assertThat(hue, is(greaterThanOrEqualTo(0.0f)));
    assertThat(hue, is(lessThanOrEqualTo(360.0f)));
  }

  @Test
  public void testPrivateConstructorThrowsException() throws NoSuchMethodException
  {
    Constructor<ColorMapper> constructor = ColorMapper.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    InvocationTargetException exception = assertThrows(
      InvocationTargetException.class,
      constructor::newInstance,
      "Expected private constructor to throw AssertionError"
    );

    assertThat(exception.getCause(), instanceOf(AssertionError.class));
    assertThat(exception.getCause().getMessage(), containsString("Utility class should not be instantiated"));
  }

  @Test
  public void testColorConsistency()
  {
    // Same input should produce same output
    Color color1 = ColorMapper.HSBtoRGB(180.0f, 80.0f, 90.0f);
    Color color2 = ColorMapper.HSBtoRGB(180.0f, 80.0f, 90.0f);

    assertThat(color1.getRed(), is(color2.getRed()));
    assertThat(color1.getGreen(), is(color2.getGreen()));
    assertThat(color1.getBlue(), is(color2.getBlue()));
  }

  @Test
  public void testHueToSaturationIntegration()
  {
    // Test that we can generate colors from DICOM tags
    int group = 0x0010;
    int element = 0x0020;

    float hue = ColorMapper.groupToHue(group);
    float sat = ColorMapper.elementToSat(element);

    Color color = ColorMapper.HSBtoRGB(hue, sat, 100.0f);
    assertNotNull(color);

    // Verify color is not black
    int sum = color.getRed() + color.getGreen() + color.getBlue();
    assertThat(sum, is(greaterThan(0)));
  }
}
