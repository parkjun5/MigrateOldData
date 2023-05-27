package org.convert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static org.convert.Main.WINDOW_SIZE;

public class OutputFormat {
    private final long start;
    private final long end;
    private Long open;
    private Long close;
    private Long high;
    private Long low;
    private Long average;
    private BigDecimal weightedAverage;
    private BigDecimal volume;
    private int count;
    private long sum;
    private BigDecimal weightedSum;
    private BigDecimal volumeSum;

    public OutputFormat(long start) {
        this.start = start;
        this.end = start + WINDOW_SIZE - 1;
        this.volume = BigDecimal.ZERO;
        count = 1;
    }

    public OutputFormat(long start, Main.InputFormat value) {
        long price = value.price();
        BigDecimal size = value.size();

        this.start = start;
        this.end = start + WINDOW_SIZE - 1;
        this.count = 1;
        this.open = price;
        this.close = price;
        this.high = price;
        this.low = price;
        this.average = price;
        this.weightedAverage = new BigDecimal(price);
        this.volume = size;
        this.sum = price;
        this.weightedSum = size.multiply(BigDecimal.valueOf(price));
        this.volumeSum = size;
    }

    public void addValue(Main.InputFormat input) {
        long price = input.price();
        BigDecimal size = input.size();

        if (price > this.high) this.high = price;
        if (this.low > price) this.low = price;
        this.count++;
        this.close = price;
        this.volume = this.volume.add(size);
        this.sum += price;
        this.weightedSum = this.weightedSum.add(size.multiply(BigDecimal.valueOf(price)));
        this.volumeSum = this.volumeSum.add(size);
    }

    public void calculateAvg() {
        this.average = sum / count;
        this.weightedAverage = weightedSum.divide(volumeSum, 0, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
        String weighedVal = weightedAverage != null ? numberFormat.format(weightedAverage.intValue()) : null;
        return "{\n" +
                "\t\"start\": " + start + ",\n" +
                "\t\"end\": " + end + ",\n" +
                "\t\"open\": \"" + open + "\",\n" +
                "\t\"close\": \"" + close + "\",\n" +
                "\t\"high\": \"" + high + "\",\n" +
                "\t\"low\": \"" + low + "\",\n" +
                "\t\"average\": \"" + average + "\",\n" +
                "\t\"weighted_average\": \"" + weighedVal + "\",\n" +
                "\t\"volume\": \"" + decimalFormat.format(volume) + "\",\n" +
                '}';
    }
}
