/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 25, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A ballot style has an identifier and a list of contests on the ballot.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@Entity
@Table(name = "ballot_style")
public class BallotStyle implements Serializable {  
  /**
   * The serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The table of objects that have been created.
   */
  private static final Map<BallotStyle, BallotStyle> CACHE = 
      new HashMap<BallotStyle, BallotStyle>();
  
  /**
   * The table of objects by ID.
   */
  private static final Map<Long, BallotStyle> BY_ID =
      new HashMap<Long, BallotStyle>();
  
  /**
   * The current ID number to be used.
   */
  private static long current_id;
  
  /**
   * The ballot style database ID.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  @SuppressWarnings("PMD.ImmutableField")
  private long my_id = getID();

  /**
   * The ballot style ID.
   */
  private final String my_identifier;
  
  /**
   * The list of contests on a ballot of this style.
   */
  @ElementCollection
  @Cascade({CascadeType.ALL})
  @JsonAdapter(ContestJsonAdapter.class)
  private final List<Contest> my_contests;
  
  /**
   * Constructs an empty ballot style, solely for persistence.
   */
  protected BallotStyle() {
    my_identifier = "";
    my_contests = null;
  }
  
  /**
   * Constructs a new ballot style.
   * 
   * @param the_name The ballot style ID.
   * @param the_contests The list of contests on a ballot of this style.
   */
  protected BallotStyle(final String the_name, final List<Contest> the_contests) {
    my_identifier = the_name;
    // TODO: clone to make immutable
    my_contests = the_contests;
  }
  
  /**
   * @return the next ID
   */
  private static synchronized long getID() {
    return current_id++;
  }
  
  /**
   * Get all ballot styles that match the specified ID (normally, we would expect
   * there to only be one, but malformed data can cause that not to happen).
   * 
   * @param the_id The ID. If this is null, all ballot styles are returned.
   * @return the requested ballot styles.
   */
  public static synchronized Collection<BallotStyle> getMatching(final String the_id) {
    final Set<BallotStyle> result = new HashSet<BallotStyle>();
    for (final BallotStyle bs : CACHE.keySet()) {
      if (the_id == null || the_id.equals(bs.identifier())) {
        result.add(bs);
      }
    }
    return result;
  }
  
  /**
   * @return all known ballot styles.
   */
  public static synchronized Collection<BallotStyle> getAll() {
    return new HashSet<BallotStyle>(CACHE.keySet());
  }

  /**
   * Returns a ballot style with the specified parameters.
   * 
   * @param the_name The ballot style name.
   * @param the_contests The list of contests on a ballot of this style.
   */
  public static synchronized BallotStyle instance(final String the_name, 
                                                  final List<Contest> the_contests) {
    BallotStyle result = new BallotStyle(the_name, the_contests);
    if (CACHE.containsKey(result)) {
      result = CACHE.get(result);
    } else {
      CACHE.put(result, result);
      BY_ID.put(result.id(), result);
    }
    return result;
  }
  
  /**
   * Returns the ballot style with the specified ID.
   * 
   * @param the_id The ID.
   * @return the ballot style, or null if it doesn't exist.
   */
  public static synchronized BallotStyle byID(final long the_id) {
    return BY_ID.get(the_id);
  }

  /**
   * @return the database ID.
   */
  public long id() {
    return my_id;
  }

  /**
   * @return the ballot style identifier.
   */
  public String identifier() {
    return my_identifier;
  }
  
  /**
   * @return the contests on a ballot of this style.
   */
  public List<Contest> contests() {
    return Collections.unmodifiableList(my_contests);
  }

  /**
   * @return a String representation of this contest.
   */
  @Override
  public String toString() {
    return "BallotStyle [identifier=" + my_identifier + ", contests=" +
           my_contests + "]";
  }

  /**
   * Compare this object with another for equivalence.
   * 
   * @param the_other The other object.
   * @return true if the objects are equivalent, false otherwise.
   */
  @Override
  public boolean equals(final Object the_other) {
    boolean result = true;
    if (the_other instanceof BallotStyle) {
      final BallotStyle other_style = (BallotStyle) the_other;
      result &= other_style.identifier().equals(identifier());
      result &= other_style.contests().equals(contests());
    } else {
      result = false;
    }
    return result;
  }
  
  /**
   * @return a hash code for this object.
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
  
  /**
   * JSON adapter for the internal contest list.
   */
  public static final class ContestJsonAdapter extends TypeAdapter<List<Contest>> {
    /**
     * Writes a list of contests as an array of contest IDs.
     * 
     * @param the_writer The JSON writer.
     * @param the_list The list of contests to write.
     */ 
    @Override
    public void write(final JsonWriter the_writer, final List<Contest> the_list) 
        throws IOException {
      the_writer.beginArray();
      for (final Contest c : the_list) {
        the_writer.value(c.id());
      }
      the_writer.endArray();
    }
    
    /**
     * Reads a list of contests from an array of contest IDs.
     */
    @Override
    public List<Contest> read(final JsonReader the_reader) throws IOException {
      final List<Contest> result = new ArrayList<Contest>();
      the_reader.beginArray();
      while (the_reader.hasNext()) {
        final Contest c = Contest.byID(the_reader.nextLong());
        if (c == null) {
          throw new IOException("invalid contest ID");
        } else {
          result.add(c);
        }
      }
      the_reader.endArray();
      return result;
    }
  }
}