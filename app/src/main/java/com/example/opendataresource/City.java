package com.example.opendataresource;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class City {
    @SerializedName("id")
    private long id;
    @SerializedName("name")
    private String name;
    @SerializedName("country")
    private String country;
    @SerializedName("coord")
    private CityCoordinates coord;


    @Override
    public String toString() {
        return "Student [id=" + id + ", name=" + name + ", country=" + country
                + ", lon=" + coord + "]";
    }


    static class CitiesListDeserializer implements JsonDeserializer<List<City>> {

        private Set<String> forbiddenCities;

        CitiesListDeserializer(String... forbiddenCities) {
            this.forbiddenCities = new HashSet<>(Arrays.asList(forbiddenCities));
        }

        @Override
        public List<City> deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException {
            List<City> list = new ArrayList<>();
            for(JsonElement e : json.getAsJsonArray()) {
                if(forbiddenCities.contains(e.getAsJsonObject().get("name").getAsString())) {
                    list.add((City) context.deserialize(e, City.class));
                }
            }
            return list;
        }
    }
}
