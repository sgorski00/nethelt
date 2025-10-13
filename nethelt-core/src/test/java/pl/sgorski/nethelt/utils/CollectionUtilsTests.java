package pl.sgorski.nethelt.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class CollectionUtilsTests {

  @Test
  void shouldReturnTrue_Null() {
    assertTrue(CollectionUtils.isEmpty(null));
  }

  @Test
  void shouldReturnTrue_EmptyList() {
    List<String> list = List.of();

    boolean result = CollectionUtils.isEmpty(list);

    assertTrue(result);
  }

  @Test
  void shouldReturnTrue_EmptySet() {
    Set<String> set = Set.of();

    boolean result = CollectionUtils.isEmpty(set);

    assertTrue(result);
  }

  @Test
  void shouldReturnFalse_NonEmptyList() {
    List<String> list = List.of("item");

    boolean result = CollectionUtils.isEmpty(list);

    assertFalse(result);
  }

  @Test
  void shouldReturnFalse_NonEmptySet() {
    Set<String> set = Set.of("item");

    boolean result = CollectionUtils.isEmpty(set);

    assertFalse(result);
  }
}
