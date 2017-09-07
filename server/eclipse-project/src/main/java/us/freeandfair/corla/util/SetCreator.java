/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Sep 1, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@galois.com>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class that creates a set from a sequence.
 * 
 * @author Daniel M. Zimmerman
 * @verson 0.0.1
 */
public final class SetCreator {
  /**
   * Private constructor to prevent instantiation.
   */
  private SetCreator() {
    // do nothing
  }
   
  /**
   * Constructs a set from the specified sequence of values.
   * 
   * @param the_values The values.
   * @return a set containing the specified values.
   */
  @SafeVarargs
  public static <T> Set<T> setOf(final T... the_values) {
    return new HashSet<T>(Arrays.asList(the_values));
  }
}