/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 25, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Joey Dodds <jdodds@galois.com>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.csv;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.model.BallotManifestInfo;
import us.freeandfair.corla.model.BallotStyle;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.Choice;
import us.freeandfair.corla.model.Contest;

/**
 * @description <description>
 * @explanation <explanation>
 * @bon OPTIONAL_BON_TYPENAME
 */
public class ColoradoBallotManifestParser implements BallotManifestParser {
  /**
   * The column containing the county ID.
   */
  private static final int COUNTY_ID_COLUMN = 0;
  
  /**
   * The column containing the scanner ID.
   */
  private static final int SCANNER_ID_COLUMN = 1;
  
  /**
   * The column containing the batch number.
   */
  private static final int BATCH_NUMBER_COLUMN = 2;
  
  /**
   * The column containing the number of ballots in the batch.
   */
  private static final int NUM_BALLOTS_COLUMN = 3;
  
  /**
   * The column containing the storage location.
   */
  private static final int BATCH_LOCATION_COLUMN = 4;

  /**
   * A flag indicating whether parse() has been run or not.
   */
  private boolean my_parse_status;
  
  /**
   * A flag indicating whether or not a parse was successful.
   */
  private boolean my_parse_success;
  
  /**
   * The parser to be used.
   */
  private final CSVParser my_parser;
  
  /**
   * The list of ballot manifest information parsed from the supplied data.
   */
  private final List<BallotManifestInfo> my_manifest_info = 
      new ArrayList<BallotManifestInfo>();
  
  
  /**
   * Construct a new Colorado ballot manifest parser using the specified Reader.
   * 
   * @param the_reader The reader from which to read the CSV to parse.
   * @exception IOException if an error occurs while constructing the parser.
   */
  public ColoradoBallotManifestParser(final Reader the_reader) 
      throws IOException {
    my_parser = new CSVParser(the_reader, CSVFormat.DEFAULT);
  }
  
  /**
   * Construct a new Colorado ballot manifest parser using the specified String.
   * 
   * @param the_string The CSV string to parse.
   * @exception IOException if an error occurs while constructing the parser.
   */
  public ColoradoBallotManifestParser(final String the_string)
      throws IOException {
    my_parser = CSVParser.parse(the_string, CSVFormat.DEFAULT);
  }
  
  /**
   * Strip the '="..."' from a column.
   * 
   * @param the_value The value to strip.
   * @return the stripped value, as a String, or the original String if it 
   * does not have the '="..."' form.
   */
  private String stripEqualQuotes(final String the_value) {
    String result = the_value;
    if (the_value.startsWith("=\"") && the_value.endsWith("\"")) {
      result = the_value.substring(0, the_value.length() - 1).replaceFirst("=\"", "");
    }
    return result;
  }
  
  /**
   * Extracts ballot manifest information from a single CSV line.
   * 
   * @param the_line The CSV line.
   * @param the_timestamp The timestamp to apply to the result.
   * @return the extracted information.
   */
  private BallotManifestInfo extractBMI(final CSVRecord the_line,
                                        final long the_timestamp) {
    try {
      return new BallotManifestInfo(the_timestamp, 
                                    the_line.get(COUNTY_ID_COLUMN),
                                    the_line.get(SCANNER_ID_COLUMN),
                                    the_line.get(BATCH_NUMBER_COLUMN),
                                    Integer.valueOf(the_line.get(NUM_BALLOTS_COLUMN)),
                                    the_line.get(BATCH_LOCATION_COLUMN));
    } catch (final NumberFormatException e) {
      return null;
    } catch (final ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }
  
  /**
   * Parse the supplied data export. If it has already been parsed, this
   * method returns immediately.
   * 
   * @return true if the parse was successful, false otherwise
   */
  @Override
  public synchronized boolean parse() {
    if (my_parse_status) {
      // no need to parse if we've already parsed
      return my_parse_success;
    }
    
    boolean result = true; // presume the parse will succeed
    final Iterator<CSVRecord> records = my_parser.iterator();
    final long timestamp = System.currentTimeMillis();
    
    try {
      // we expect the first line to be the headers, which we currently discard
      records.next();
      
      // subsequent lines contain ballot manifest info
      while (records.hasNext()) {
        final CSVRecord bmi_line = records.next();
        final BallotManifestInfo bmi = extractBMI(bmi_line, timestamp);
        if (bmi == null) {
          // we don't record the ballot manifest record since it didn't parse
          Main.LOGGER.error("Could not parse malformed ballot manifest record (" + 
                            bmi_line + ")");
          result = false;          
        } else {
          my_manifest_info.add(bmi);
        }
      }
    } catch (final NoSuchElementException e) {
      Main.LOGGER.error("Could not parse CVR file because it had a malformed header");
      result = false;
    }
    
    // TODO if we had any kind of parse error, do we scrap the whole import? 
    my_parse_success = result;
    my_parse_status = true;
    return result;
  }

  /**
   * @return the CVRs parsed from the supplied data export.
   */
  @Override
  public synchronized List<BallotManifestInfo> ballotManifestInfo() {
    return Collections.unmodifiableList(my_manifest_info);
  }
  
  /**
   * 
   * <description>
   * <explanation>
   * @param
   */
  //@ behavior
  //@   requires P;
  //@   ensures Q;
  /*@ pure @
   */
  public static void main(final String... the_args) throws IOException {
    final Reader r = new FileReader("/Unsorted/bmi.csv");
    final ColoradoBallotManifestParser thing = new ColoradoBallotManifestParser(r);
    System.err.println(thing.parse());
    System.err.println(thing.ballotManifestInfo());
  }
}
