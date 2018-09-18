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
 * Uses the mean to find the pivot. The sum of all elements on the file must not
 * exceed Long.MAX
 *
 * @author njacinto
 */
public class PivotSelectionStrategyMean implements PivotSelectionStrategy {

    public static final PivotSelectionStrategyMean INSTANCE = new PivotSelectionStrategyMean();

    //
    public PivotSelectionStrategyMean() {
    }

    @Override
    public long getPivot(File file, RandomAccessFile in) throws IOException {
        final byte[] readBuff = new byte[Constants.BUFFER_SIZE];
        int eolIdx, stIdx;
        int readLen;
        int buffInitPos = 0;
        long acc = 0, count = 0;
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
                        acc += NumberUtil.toLong(readBuff, stIdx, eolIdx - 1);
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
            acc += NumberUtil.toLong(readBuff, 0, buffInitPos - 1);
        }
        if (count > 0) {
            buffInitPos = 0;
            long countLeft = 0, countRight = 0;
            long prev = Long.MIN_VALUE;
            long next = Long.MAX_VALUE;
            long mean = acc / count;
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
                            if (mean > num) {
                                countLeft++;
                                if (num > prev) {
                                    prev = num;
                                }
                            } else if (mean < num) {
                                countRight++;
                                if (num < next) {
                                    next = num;
                                }
                            } else { // if equal just return it
                                return num;
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
                if (mean > num) {
                    countLeft++;
                    if (num > prev) {
                        prev = num;
                    }
                } else if (mean < num) {
                    countRight++;
                    if (num < next) {
                        next = num;
                    }
                } else { // if equal just return it
                    return num;
                }
            }
            return (countLeft > countRight) ? prev : next;
        }
        return Long.MIN_VALUE;
    }

    @Override
    public String toString() {
        return "PivotSelectionStrategyMean{" + '}';
    }

}
