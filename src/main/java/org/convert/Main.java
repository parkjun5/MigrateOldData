package org.convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class Main {

    public static final String KORBIT_API_URL = "http://api.bitcoincharts.com/v1/csv/korbitKRW.csv.gz";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        int period = 86400;

        HttpURLConnection conn = createURLConnection();
        ReadableByteChannel channel = Channels.newChannel(conn.getInputStream());
        GZIPInputStream gzipInputStream = new GZIPInputStream(Channels.newInputStream(channel));
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
        BufferedReader buffer = new BufferedReader(inputStreamReader);

        processDataFromReader(period, buffer);

        System.out.println("컨버팅 경과 시간 : " + (System.currentTimeMillis() - startTime));
    }


    private static HttpURLConnection createURLConnection() throws IOException {
        URL url = new URL(KORBIT_API_URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        return conn;
    }

    public static void processDataFromReader(int period, BufferedReader buffer) throws IOException {
        long startTime = 0;
        long lastBlock = 0;
        String line = buffer.readLine();
        OutputFormat outputFormat = new OutputFormat(startTime, period);

        if (line != null) {
            InputFormat input = InputFormat.of(line.split(","));
            startTime = input.timestamp();
            outputFormat = new OutputFormat(startTime, period, input);
            System.out.print("[");
        }

        while ((line = buffer.readLine()) != null) {
            InputFormat input = InputFormat.of(line.split(","));
            long currentBlock = (input.timestamp() - startTime) / period;
            if (currentBlock == lastBlock) {
                outputFormat.addValue(input);
            } else {
                outputFormat.calculateAvg();
                System.out.println(outputFormat + ",");
                lastBlock++;

                if (currentBlock > lastBlock) {

                    for (long index = lastBlock; index < currentBlock; index++) {
                        long currentTime = startTime + period * (currentBlock - 1);
                        outputFormat = new OutputFormat(currentTime, period);
                        System.out.println(outputFormat + ",");
                    }
                    lastBlock = currentBlock;
                    long currentTime = startTime + period * currentBlock;
                    outputFormat = new OutputFormat(currentTime, period, input);
                } else {
                    long currentTime = startTime + period * currentBlock;
                    outputFormat = new OutputFormat(currentTime, period, input);
                }
            }
        }

        outputFormat.calculateAvg();
        System.out.println(outputFormat + "]");
    }

    record InputFormat(
            long timestamp,
            long price,
            BigDecimal size
    ) {

        public static InputFormat of(String[] values) {
            long inputTimeStamp = Long.parseLong(values[0].replaceAll("\\D", ""));
            long inputPrice = Long.parseLong(values[1].split("\\.")[0].replaceAll("\\D", ""));
            BigDecimal inputSize = new BigDecimal(values[2]);
            return new InputFormat(inputTimeStamp, inputPrice, inputSize);
        }
    }

}