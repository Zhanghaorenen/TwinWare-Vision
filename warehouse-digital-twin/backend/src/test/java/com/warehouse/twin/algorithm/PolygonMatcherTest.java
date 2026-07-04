package com.warehouse.twin.algorithm;
import com.fasterxml.jackson.databind.ObjectMapper; import org.junit.jupiter.api.Test; import java.util.List; import static org.assertj.core.api.Assertions.assertThat;
class PolygonMatcherTest {private final PolygonMatcher matcher=new PolygonMatcher(new ObjectMapper());
    @Test void centerInsidePolygonMatches(){assertThat(matcher.matches(List.of(120d,90d,200d,160d),"[[100,80],[220,80],[220,180],[100,180]]")).isTrue();}
    @Test void distantBoxDoesNotMatch(){assertThat(matcher.matches(List.of(400d,400d,500d,500d),"[[100,80],[220,80],[220,180],[100,180]]")).isFalse();}}
