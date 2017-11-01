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
      Arguments.of(+30, 100.0f),
      Arguments.of(+100, 91.96f),
      Arguments.of(+1000, 67.46f),
      Arguments.of(+10000, 42.96f),
      Arguments.of(+530000, 0.71f)
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
      Arguments.of(+0, 49.99f),
      Arguments.of(-5, 49.99f),
      Arguments.of(+30, 49.99f),
      Arguments.of(+100, 50.00f),
      Arguments.of(+1000, 50.09f),
      Arguments.of(+10000, 50.95f),
      Arguments.of(+530000, 100.59f)
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
