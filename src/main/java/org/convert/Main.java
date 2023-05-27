package org.convert;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class Main {

    public static final String KORBIT_API_URL = "http://api.bitcoincharts.com/v1/csv/korbitKRW.csv.gz";
    private static final int MAX_PARAM = 86_400;
    private static final int MIN_PARAM = 30;

    public static int WINDOW_SIZE;

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();

        BufferedReader reader = getAPIBufferedReader();
        processDataFromReader(reader, MAX_PARAM);

        System.out.println("컨버팅 경과 시간 : " + (System.currentTimeMillis() - startTime));
    }

    private static BufferedReader getAPIBufferedReader() throws IOException {
        HttpURLConnection conn = createURLConnection();
        ReadableByteChannel channel = Channels.newChannel(conn.getInputStream());
        GZIPInputStream gzipInputStream = new GZIPInputStream(Channels.newInputStream(channel));
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    public static BufferedReader getFileBufferedReader() throws FileNotFoundException {
        File file = new File("src/test/resources/.testKorbitKRW.csv");
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        return  new BufferedReader(inputStreamReader);
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

    public static void processDataFromReader(BufferedReader buffer, int windSize) throws IOException {
        initPeriod(windSize);

        long startTime = 0;
        long lastBlock = 0;
        String line = buffer.readLine();
        OutputFormat outputFormat = new OutputFormat(startTime);

        if (line != null) {
            InputFormat input = InputFormat.of(line.split(","));
            startTime = input.timestamp();
            outputFormat = new OutputFormat(startTime, input);
            System.out.print("[");
        }

        while ((line = buffer.readLine()) != null) {
            InputFormat input = InputFormat.of(line.split(","));
            long currentBlock = (input.timestamp() - startTime) / WINDOW_SIZE;
            if (currentBlock == lastBlock) {
                outputFormat.addValue(input);
            } else {
                outputFormat.calculateAvg();
                System.out.println(outputFormat + ",");
                lastBlock++;

                if (currentBlock > lastBlock) {
                    for (long index = lastBlock; index < currentBlock; index++) {
                        long currentTime = startTime + WINDOW_SIZE * (currentBlock - 1);
                        outputFormat = new OutputFormat(currentTime);
                        System.out.println(outputFormat + ",");
                    }

                    lastBlock = currentBlock;
                }

                long currentTime = startTime + WINDOW_SIZE * currentBlock;
                outputFormat = new OutputFormat(currentTime, input);
            }
        }

        outputFormat.calculateAvg();
        System.out.println(outputFormat + "]");
    }

    private static void initPeriod(int period) {
        if (period > MAX_PARAM || period < MIN_PARAM) {
            throw new IllegalArgumentException("기간은 30초 이상 1일(86400)이하여야 합니다.");
        }

        WINDOW_SIZE = period;
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