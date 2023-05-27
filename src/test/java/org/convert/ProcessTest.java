package org.convert;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessTest {

    @Test
    void convertTestData() throws IOException {
        BufferedReader reader = Main.getFileBufferedReader();
        Main.processDataFromReader(reader, 30);
    }

    @Test
    void convertRealData() throws IOException {
        Main.main(new String[]{""});
    }

    @Test
    void windowSizeError() throws IOException {
        BufferedReader reader = Main.getFileBufferedReader();
        assertThrows(IllegalArgumentException.class, () -> Main.processDataFromReader(reader, 0));
        assertThrows(IllegalArgumentException.class, () -> Main.processDataFromReader(reader, 86_401));
    }

}