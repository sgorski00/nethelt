package pl.sgorski.nethelt.core.utils;

public class CollectionUtils {

  /**
   * Checks if the given Iterable is null or contains no elements.
   *
   * @param collection the Iterable to check
   * @return true if the collection is null or empty, false otherwise
   */
  public static boolean isEmpty(Iterable<?> collection) {
    return collection == null || !collection.iterator().hasNext();
  }
}
