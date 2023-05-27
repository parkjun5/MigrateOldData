package org.convert;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

class ProcessTest {

    @Test
    void convertTestData() throws IOException {
        File file = new File("src/test/resources/.testKorbitKRW.csv");
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader buffer = new BufferedReader(inputStreamReader);

        int period =  30;
        Main.processDataFromReader(period, buffer);
    }

    @Test
    void convertRealData() throws IOException {
        Main.main(new String[]{""});
    }

}