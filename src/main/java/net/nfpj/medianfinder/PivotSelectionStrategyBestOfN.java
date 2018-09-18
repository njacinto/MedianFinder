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
import java.util.Arrays;

/**
 * Collects several numbers from the file, using the size and the number of
 * samples to collect, and returns the median of them.
 *
 * @author njacinto
 */
public class PivotSelectionStrategyBestOfN implements PivotSelectionStrategy {

    public static final int DEFAULT_N = 3;
    //
    public static final PivotSelectionStrategyBestOfN BEST_OF_3 = new PivotSelectionStrategyBestOfN(3);
    public static final PivotSelectionStrategyBestOfN BEST_OF_5 = new PivotSelectionStrategyBestOfN(5);
    public static final PivotSelectionStrategyBestOfN BEST_OF_7 = new PivotSelectionStrategyBestOfN(7);
    //
    private static final int DEFAULT_MIN_STEP_SIZE = 64;
    //
    private final int n;

    public PivotSelectionStrategyBestOfN() {
        this(DEFAULT_N);
    }

    /**
     *
     * @param n number of numbers to collect. Must be an odd number bigger than
     * one
     */
    public PivotSelectionStrategyBestOfN(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("N cannot be smaller than one");
        }
        this.n = ((n & 1) == 1) ? n : n+1;
    }

    @Override
    public long getPivot(File file, RandomAccessFile in) throws IOException {
        if (n > 1) {
            final byte[] readBuff = new byte[Constants.BUFFER_SIZE];
            final long[] values = new long[n];
            final long fileSize = file.length();
            long step = fileSize / (n - 1);
            if (step > DEFAULT_MIN_STEP_SIZE) {
                int i = 0;
                int read;
                in.seek(0);
                if ((read = in.read(readBuff)) != -1) {
                    long num = NumberUtil.getFirstNumberFromBuffer(readBuff, read, true);
                    if (num != Long.MIN_VALUE) {
                        values[i++] = num;
                    }
                }
                for (long pos = step; pos < fileSize && i < n - 1; pos += step) {
                    in.seek(pos);
                    while ((read = in.read(readBuff)) != -1) {
                        long num = NumberUtil.getFirstNumberFromBuffer(readBuff, read, false);
                        if (num != Long.MIN_VALUE) {
                            values[i++] = num;
                            break;
                        }
                    }
                }
                in.seek((fileSize > readBuff.length ? fileSize - readBuff.length : 0) + 1);
                if ((read = in.read(readBuff)) != -1) {
                    long num = NumberUtil.getLastNumberFromBuffer(readBuff, read, true);
                    if (num != Long.MIN_VALUE) {
                        values[i++] = num;
                    }
                }
                if (i > 0) {
                    Arrays.sort(values, 0, i);
                    return values[i / 2];
                }
            }
        }
        return PivotSelectionStrategyMiddle.INSTANCE.getPivot(file, in);
    }

    @Override
    public String toString() {
        return "PivotSelectionStrategyBestOfN{" + "n=" + n + '}';
    }
}
