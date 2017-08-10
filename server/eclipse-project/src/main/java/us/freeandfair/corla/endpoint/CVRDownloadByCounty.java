/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 27, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.endpoint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.stream.JsonWriter;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.CastVoteRecord.RecordType;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.CastVoteRecordQueries;
import us.freeandfair.corla.util.SparkHelper;

/**
 * The ballot manifest download endpoint.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class CVRDownloadByCounty implements Endpoint {
  /**
   * {@inheritDoc}
   */
  @Override
  public EndpointType endpointType() {
    return EndpointType.GET;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/cvr/county";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String endpoint(final Request the_request, final Response the_response) {
    String result = "";
    int status = HttpStatus.OK_200;
    
    if (validateParameters(the_request)) {
      final Set<Integer> county_set = new HashSet<Integer>();
      for (final String s : the_request.queryParams()) {
        county_set.add(Integer.valueOf(s));
      }
      try {
        Persistence.beginTransaction();
        final OutputStream os = SparkHelper.getRaw(the_response).getOutputStream();
        final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        final JsonWriter jw = new JsonWriter(bw);
        jw.beginArray();
        for (final Integer county : county_set) {
          final Stream<CastVoteRecord> matches = 
              CastVoteRecordQueries.getMatching(county, RecordType.UPLOADED);
          matches.forEach((the_cvr) -> {
            try {
              jw.jsonValue(Main.GSON.toJson(the_cvr));
              Persistence.currentSession().evict(the_cvr);
            } catch (final IOException e) {
              // ignore, there's nothing we can do about it and it probably
              // means the HTTP connection broke
            } 
          });
        }
        jw.endArray();
        jw.flush();
        jw.close();
        try {
          Persistence.commitTransaction(); 
        } catch (final RollbackException e) {
          Persistence.rollbackTransaction();
        } 
      } catch (final IOException | PersistenceException e) {
        status = HttpStatus.INTERNAL_SERVER_ERROR_500;
        result = "Unable to stream response";
      }
    } else {
      status = HttpStatus.NOT_FOUND_404;
      result = "Invalid county ID specified";
    }
    the_response.status(status);
    return result;
  }
  
  /**
   * Validates the parameters of a request. For this endpoint, 
   * the paramter names must all be integers.
   * 
   * @param the_request The request.
   * @return true if the parameters are valid, false otherwise.
   */
  private boolean validateParameters(final Request the_request) {
    boolean result = true;
    
    for (final String s : the_request.queryParams()) {
      try {
        Integer.parseInt(s);
      } catch (final NumberFormatException e) {
        result = false;
        break;
      }
    }
    
    return result;
  }
}
