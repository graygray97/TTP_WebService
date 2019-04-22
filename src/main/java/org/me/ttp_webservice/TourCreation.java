/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.ttp_webservice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import static com.ttp.ttp_commons.Constants.getGoogle_map_api_key;
import com.ttp.ttp_commons.Location;
import com.ttp.ttp_commons.Path;
import static com.ttp.ttp_commons.TtpCalculation.runSolutions;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import com.ttp.ttp_commons.TourInput;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Graham
 */
@WebService(serviceName = "TourCreation")
@Stateless()
public class TourCreation {

    /**
     * Web service operation
     * @param tourInputJson
     * @return 
     */
    @WebMethod(operationName = "createTour", action = "createTour")
    public String createTour(@WebParam(name = "modeAndLocations") String tourInputJson) {
        try{
            Gson gson = new Gson();
            TourInput tourInputs = gson.fromJson(tourInputJson, TourInput.class);
            try{
                String[] locs = tourInputs.getLocations();
                String key = getGoogle_map_api_key();
                String mode = tourInputs.getMode();
                List<Path> results = runSolutions(locs, key, mode);
                return gson.toJson(results);
            } catch (IOException ex){
                return "IOException: " + ex.toString();
            } catch (ApiException ex) {
               return "ApiException: " + ex.toString();
            } catch (InterruptedException ex) {
                return "InterruptedException: " + ex.toString();
            }
        } catch (JsonSyntaxException ex ) {
            return "JsonSyntaxException: " + ex.toString();
        }
    }

    /**
     * Web service operation
     * @param pathParams
     * @return 
     */
    @WebMethod(operationName = "getFullPath", action = "getFullPath")
    public String getFullPath(@WebParam(name = "pathParams") String pathParams) {
        try{
            Gson gson = new Gson();
            TourInput input = gson.fromJson(pathParams, TourInput.class);
            GeoApiContext mGeoApicontext = new GeoApiContext.Builder()
                .apiKey(getGoogle_map_api_key())
                .build();
            String[] locStartCoords = input.getLocations()[0].split(",");
            Location locStart = new Location(new LatLng(Double.parseDouble(locStartCoords[0]), Double.parseDouble(locStartCoords[1])));
            String[] locEndCoords = input.getLocations()[1].split(",");
            Location locEnd = new Location(new LatLng(Double.parseDouble(locEndCoords[0]), Double.parseDouble(locEndCoords[1])));
            Path path = new Path(locStart, locEnd);
            path.CalculateDirections(mGeoApicontext, input.getMode(), false);
            return gson.toJson(path);       
        }catch(JsonSyntaxException | NumberFormatException | ApiException | InterruptedException | IOException e){
            return e.toString();
        }
    }
}
