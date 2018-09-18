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

/**
 *
 * @author njacinto
 */
public final class NumberUtil {

    public static final int ZERO_ASCII = '0';

    private NumberUtil() {
    }

    public static long toLong(byte[] arr, int firstCharIdx, int lastCharIdx) {
        long l = 0;
        long m = 1;
        for (; lastCharIdx > firstCharIdx; m *= 10) {
            l += m * (arr[lastCharIdx--] - ZERO_ASCII);
        }
        return (arr[firstCharIdx] == '-') ? -l : l + (m * (arr[lastCharIdx] - ZERO_ASCII));
    }

    public static long getSmaller(long[] arr, int from, int to) {
        long num = arr[from];
        for (int i = from + 1; i < to; i++) {
            if (num > arr[i]) {
                num = arr[i];
            }
        }
        return num;
    }

    public static long getSmallerThan(long num, long[] arr, int from, int to) {
        for (int i = from; i < to; i++) {
            if (num > arr[i]) {
                num = arr[i];
            }
        }
        return num;
    }

    public static long getBigger(long[] arr, int from, int to) {
        long num = arr[from];
        for (int i = from + 1; i < to; i++) {
            if (num < arr[i]) {
                num = arr[i];
            }
        }
        return num;
    }

    public static long getBiggerThan(long num, long[] arr, int from, int to) {
        for (int i = from; i < to; i++) {
            if (num < arr[i]) {
                num = arr[i];
            }
        }
        return num;
    }

    /**
     * Extracts the first number from the buffer.
     *
     * @param readBuff the buffer with the characters representing the numbers
     * @param len the number of valid characters on the buffer
     * @param fromBeginning if false, then number will start after the first end of
     *              line, otherwise it will consider the the number starts at
     *              the beginning of the buffer.
     * @return
     */
    public static long getFirstNumberFromBuffer(final byte[] readBuff, int len, boolean fromBeginning) {
        int stIdx = 0, ndIdx = 0;
        // find end of line for beginning of number
        if (!fromBeginning) {
            for (; stIdx < len && readBuff[stIdx] != Constants.EOL; stIdx++);
            stIdx++;
        }
        for (; stIdx < len && readBuff[stIdx] == Constants.EOL; stIdx++);
        // find end of line for beginning of number
        for (ndIdx = stIdx; ndIdx < len && readBuff[ndIdx] != Constants.EOL; ndIdx++);
        ndIdx--;
        if (ndIdx < len) {
            return NumberUtil.toLong(readBuff, stIdx, ndIdx);
        }
        return Long.MIN_VALUE;
    }

    /**
     * Extracts the last number of the buffer.
     *
     * @param readBuff the buffer with the numbers
     * @param len the number of valid characters on the buffer
     * @param fromEnd if false it will look for the end of line to make sure it
     *              has a full number, otherwise it will consider that the number
     *              ends at len.
     * @return
     */
    public static long getLastNumberFromBuffer(final byte[] readBuff, int len, boolean fromEnd) {
        int ndIdx = len - 1, stIdx = 0;
        // find end of line for beginning of number
        if (!fromEnd) {
            for (; ndIdx >= 0 && readBuff[ndIdx] != Constants.EOL; ndIdx--);
            ndIdx--;
        }
        for (; ndIdx >= 0 && readBuff[ndIdx] == Constants.EOL; ndIdx--);
        // find end of line for beginning of number
        for (stIdx = ndIdx; stIdx >= 0 && readBuff[stIdx] != Constants.EOL; stIdx--);
        stIdx++;
        if (stIdx >= 0) {
            return NumberUtil.toLong(readBuff, stIdx, ndIdx);
        }
        return Long.MIN_VALUE;
    }
}
