package com.astier.bts.client_tcp_prof.configuration;

import com.astier.bts.client_tcp_prof.modeles.ConfigAES;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class LectureJson {
    public static ConfigAES getConfigAES() throws FileNotFoundException {
        Gson gson = new Gson();
        FileReader fileReader = new FileReader("configuration.json");
        JsonReader jsonReader = new JsonReader(fileReader);
        return gson.fromJson(jsonReader, ConfigAES.class);
    }
}
