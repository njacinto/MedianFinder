
/*
 * Copyright (C) 2015.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.nfpj.medianfinder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author njacinto
 */
public class DataFileGenerator {

    public static final String FILE_NAME = "test05.dat";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        createFile(FILE_NAME, 2000000000);
//        createFileSeq("medianFinderTestData02.txt", 99, 0, -1, 2);
    }

    public static void createFile(final String fileName, int count) throws IOException {
        if (!(new File(fileName).exists())) {
            try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
                byte[] buff = new byte[4096];
                for (long i = 0; i < count; ++i) {
                    int j = 0;
                    long num = i;
                    buff[j++] = 49;
                    while (num > 0) {
                        buff[j++] = (byte) ((num % 10) + 48);
                        num /= 10;
                    }
                    buff[j++] = 10;
                    file.write(buff, 0, j);
                }
            }
        }
    }

    public static long createFileSeqMix(final String fileName, int count, int interval) throws IOException {
        if (!(new File(fileName).exists())) {
            if (interval < 1) {
                interval = 1;
            }
            if (count < 10) {
                count = 10;
            }
            int[] arr = new int[count];
            int acc = 1;
            for (int i = 0; i < count; i++) {
                arr[i] = acc;
                acc += interval;
            }
            long ret = (count & 1) == 1 ? arr[(count >> 1)]
                    : (arr[(count >> 1) - 1] + arr[(count >> 1)]) / 2;
            final int mixLen = count >> 2;
            for (int i = 0; i < mixLen; i++) {
                int tmp = arr[i];
                arr[i] = arr[i + mixLen * 2];
                arr[i + mixLen * 2] = tmp;
                tmp = arr[i + mixLen];
                arr[i + mixLen] = arr[i + mixLen * 3];
                arr[i + mixLen * 3] = tmp;
            }
            try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
                for (int i = 0; i < count; ++i) {
                    byte[] b = String.format("%d\n", arr[i]).getBytes();
                    file.write(b, 0, b.length);
                }
            }
            return ret;
        }
        return 0;
    }

    public static long createFileSeq(final String fileName, int start, int end,
            int interval, int numlen) throws IOException {
        if (!(new File(fileName).exists())) {
            if (interval == 0) {
                interval = 1;
            }
            final int len = ((end > start) ? (end - start) / interval : (start - end) / -interval) + 1;
            int[] arr = new int[len];
            for (int i = 0, num = start; i < len; i++, num += interval) {
                arr[i] = num;
            }
            long ret = (len & 1) == 1 ? arr[(len >> 1)]
                    : (arr[(len >> 1) - 1] + arr[(len >> 1)]) / 2;
            try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
                for (int i = 0; i < len; ++i) {
                    byte[] b = String.format("%0" + numlen + "d\n", arr[i]).getBytes();
                    file.write(b, 0, b.length);
                }
            }
            return ret;
        }
        return 0;
    }
}
