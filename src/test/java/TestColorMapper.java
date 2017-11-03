import de.famst.dicom.visualizer.ColorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

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



}
