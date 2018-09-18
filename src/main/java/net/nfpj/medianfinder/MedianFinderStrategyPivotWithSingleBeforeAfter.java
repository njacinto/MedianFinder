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
 * Finds the median using the pivot and keeping the number before and after the
 * pivot. The numbers will be used to move the pivot on the following iterations
 * until the median is found.
 *
 * @author njacinto
 */
public class MedianFinderStrategyPivotWithSingleBeforeAfter implements MedianFinderStrategy {

    public static final MedianFinderStrategyPivotWithSingleBeforeAfter INSTANCE
            = new MedianFinderStrategyPivotWithSingleBeforeAfter();

    private final PivotSelectionStrategy pivotSelection;

    public MedianFinderStrategyPivotWithSingleBeforeAfter() {
        this(PivotSelectionStrategyBestOfN.BEST_OF_7);
    }

    public MedianFinderStrategyPivotWithSingleBeforeAfter(PivotSelectionStrategy pivotSelection) {
        if (pivotSelection == null) {
            throw new IllegalArgumentException("PivotSelectionStrategy cannot be null.");
        }
        this.pivotSelection = pivotSelection;
    }

    @Override
    public long findMedian(File file) throws MedianFinderException, IOException {
        RandomAccessFile in = new RandomAccessFile(file, "r");
        final byte[] readBuff = new byte[Constants.BUFFER_SIZE];
        long pivot, prev, next;
        int buffInitPos = 0;
        long countLeft = 0, countRight = 0, count = 0;
        //
        pivot = pivotSelection.getPivot(file, in);
        //
        int eolIdx, stIdx;
        int dupCount;
        int readLen;
        boolean medianFound = false;
        while (!medianFound) {
            prev = Long.MIN_VALUE;
            next = Long.MAX_VALUE;
            dupCount = 0;
            countLeft = 0;
            countRight = 0;
            count = 0;
            buffInitPos = 0;
            in.seek(0);
            while ((readLen = in.read(readBuff, buffInitPos, readBuff.length - buffInitPos)) != -1) {
                readLen += buffInitPos;
                stIdx = eolIdx = 0;
                buffInitPos = 0;
                while (eolIdx < readLen) {
                    for (; eolIdx < readLen && readBuff[eolIdx] != Constants.EOL; eolIdx++);
                    if (stIdx < eolIdx) {
                        if (eolIdx < readLen) {
                            count++;
                            long num = NumberUtil.toLong(readBuff, stIdx, eolIdx - 1);
                            //System.out.println(num);
                            if (pivot > num) {
                                //System.out.println(num);
                                countLeft++;
                                if (num > prev) {
                                    prev = num;
                                }
                            } else if (pivot < num) {
                                countRight++;
                                if (num < next) {
                                    next = num;
                                }
                            } else if (dupCount > 0) {
                                throw new DuplicateNumberException("Duplicate number found: " + num);
                            } else {
                                dupCount++;
                            }
                            stIdx = ++eolIdx;
                        } else if (stIdx < readLen) {
                            buffInitPos = readLen - stIdx;
                            System.arraycopy(readBuff, stIdx, readBuff, 0, buffInitPos);
                        }
                    } else {
                        stIdx = ++eolIdx;
                    }
                }
            }
            if (buffInitPos > 0) {
                count++;
                long num = NumberUtil.toLong(readBuff, 0, buffInitPos - 1);
                if (pivot > num) {
                    countLeft++;
                    if (num > prev) {
                        prev = num;
                    }
                } else if (pivot < num) {
                    countRight++;
                    if (num < next) {
                        next = num;
                    }
                }
            }
            if ((count & 1) == 0 && ((countLeft - countRight) == 1 || (countRight - countLeft) == 1)) {
                pivot = ((countLeft > countRight) ? (pivot + prev) : (pivot + next)) / 2;
                medianFound = true;
            } else if (countLeft > countRight) {
                pivot = prev;
            } else if (countLeft < countRight) {
                pivot = next;
            } else {
                medianFound = true;
            }
        }
//        System.out.println("Pivot = " + pivot + " count = " + count
//                + " countLeft = " + countLeft + " countRight = " + countRight);
        return pivot;
    }

    @Override
    public String toString() {
        return "MedianFinderStrategyPivotWithSingleBeforeAfter{" + "pivotSelection=" + pivotSelection + '}';
    }
}
