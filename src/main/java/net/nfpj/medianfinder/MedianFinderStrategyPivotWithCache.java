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
 * Finds the median using the pivot and keeping a cache of numbers left and
 * right of the pivot. The cache will contain the numbers before and after the
 * pivot, allowing to jump several numbers in each iteration.
 *
 * @author njacinto
 */
public class MedianFinderStrategyPivotWithCache implements MedianFinderStrategy {

    public static final MedianFinderStrategyPivotWithCache INSTANCE = new MedianFinderStrategyPivotWithCache();
    protected static final int DEFAULT_NUMBERS_CACHE_SIZE = 1024;

    protected final PivotSelectionStrategy pivotSelection;
    protected final int numbersCacheSize;

    public MedianFinderStrategyPivotWithCache() {
        this(DEFAULT_NUMBERS_CACHE_SIZE, new PivotSelectionStrategyBestOfN(15));
//        this(DEFAULT_NUMBERS_CACHE_SIZE, PivotSelectionStrategyMiddle.INSTANCE);
//        this(DEFAULT_NUMBERS_CACHE_SIZE, PivotSelectionStrategyMean.INSTANCE);
    }

    public MedianFinderStrategyPivotWithCache(PivotSelectionStrategy pivotSelection) {
        this(DEFAULT_NUMBERS_CACHE_SIZE, pivotSelection);
    }

    public MedianFinderStrategyPivotWithCache(int numbersCacheSize) {
        this(numbersCacheSize, new PivotSelectionStrategyBestOfN(15));
    }

    public MedianFinderStrategyPivotWithCache(int numbersCacheSize, PivotSelectionStrategy pivotSelection) {
        if (pivotSelection == null) {
            throw new IllegalArgumentException("PivotSelectionStrategy cannot be null.");
        }
        this.pivotSelection = pivotSelection;
        this.numbersCacheSize = numbersCacheSize > 1 ? numbersCacheSize & ~1 : DEFAULT_NUMBERS_CACHE_SIZE;
    }

    @Override
    public long findMedian(File file) throws MedianFinderException, IOException {
        RandomAccessFile in = new RandomAccessFile(file, "r");
        final byte[] readBuff = new byte[Constants.BUFFER_SIZE];
        long pivot;
        Data dta = new Data(0, file.length(), numbersCacheSize);
        int buffInitPos = 0;
        long countScans = 0;
        //
        pivot = pivotSelection.getPivot(file, in);
        //
        int eolIdx, stIdx;
        int readLen;
        boolean medianFound = false;
        while (!medianFound) {
            dta.clearCounters();
            buffInitPos = 0;
            in.seek(0);
            countScans++;
            while ((readLen = in.read(readBuff, buffInitPos, readBuff.length - buffInitPos)) != -1) {
                readLen += buffInitPos;
                stIdx = eolIdx = 0;
                buffInitPos = 0;
                while (eolIdx < readLen) {
                    for (; eolIdx < readLen && readBuff[eolIdx] != Constants.EOL; eolIdx++);
                    if (stIdx < eolIdx) {
                        if (eolIdx < readLen) {
                            dta.count++;
                            long num = NumberUtil.toLong(readBuff, stIdx, eolIdx - 1);
                            //System.out.println(num);
                            if (pivot > num) {
                                //System.out.println(num);
                                dta.countLeft++;
                                dta.updateCacheLeft(num);
                            } else if (pivot < num) {
                                dta.countRight++;
                                dta.updateCacheRight(num);
                            } else if (dta.dupCount > 0) {
                                throw new DuplicateNumberException("Duplicate number found: " + num);
                            } else {
                                ++dta.dupCount;
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
                dta.count++;
                long num = NumberUtil.toLong(readBuff, 0, buffInitPos - 1);
                if (pivot > num) {
                    dta.countLeft++;
                    dta.updateCacheLeft(num);
                } else if (pivot < num) {
                    dta.countRight++;
                    dta.updateCacheRight(num);
                } else if (dta.dupCount > 0) {
                    throw new DuplicateNumberException("Duplicate number found: " + num);
                } else {
                    ++dta.dupCount;
                }
            }
            if ((dta.count & 1) == 0 && ((dta.countLeft - dta.countRight) == 1 || (dta.countRight - dta.countLeft) == 1)) {
                long num = 0;
                if (dta.countLeft > dta.countRight) {
                    num = dta.prevNd == dta.prevNdLim ? dta.cache[dta.prevNd-1]
                            : NumberUtil.getBigger(dta.cache, 0, dta.prevNd);
                } else {
                    num = dta.nextSt == dta.nextStLim ? dta.cache[dta.nextSt + 1]
                            : NumberUtil.getSmaller(dta.cache, dta.nextSt + 1, dta.cache.length);
                }
                pivot = (pivot + num) / 2;
                medianFound = true;
            } else if (dta.countLeft > dta.countRight) {
                //                System.out.println("Pivot = " + pivot
                //                        + " Prev = " + prev + " count = " + count
                //                        + " countLeft = " + countLeft + " countRight = " + countRight);
                int idx = (int) (dta.prevNd - ((dta.countLeft - dta.countRight) >> 1));
                if (dta.prevNd < dta.prevNdLim) {
                    Arrays.sort(dta.cache, 0, dta.prevNd);
                }
                pivot = dta.cache[idx > 0 ? idx : 0];
                dta.prevNdLim = dta.nextStLim = dta.cache.length - 1;
            } else if (dta.countLeft < dta.countRight) {
                //                System.out.println("Pivot = " + pivot
                //                        + " Next = " + next + " count = " + count
                //                        + " countLeft = " + countLeft + " countRight = " + countRight);
                int idx = (int) (dta.nextSt + 1 + ((dta.countRight - dta.countLeft) >> 1));
                if (dta.nextSt >= dta.nextStLim) {
                    Arrays.sort(dta.cache, dta.nextSt, dta.cache.length);
                }
                pivot = dta.cache[idx < dta.cache.length ? idx : dta.cache.length - 1];
                dta.prevNdLim = dta.nextStLim = 1;
            } else {
                medianFound = true;
            }
        }
//        System.out.println("Pivot = " + pivot + " count = " + dta.count
//                + " countLeft = " + dta.countLeft + " countRight = " + dta.countRight
//                + " File scans = " + countScans);
        return pivot;
    }

    @Override
    public String toString() {
        return "MedianFinderStrategyPivotWithCache{" + "pivotSelection=" + pivotSelection + ", numbersCacheSize=" + numbersCacheSize + '}';
    }

    //
    protected static class Data {

        final long pos, limit;
        final long[] cache;
        int prevNdLim, nextStLim;
        int prevNd, nextSt;
        long count = 0;
        long countLeft = 0;
        long countRight = 0;
        long dupCount = 0;
        boolean check = true;

        Data(long pos, long limit, int numbersCacheSize) {
            this(pos, limit, numbersCacheSize, numbersCacheSize >> 1);
        }

        Data(long pos, long limit, int numbersCacheSize, int leftSize) {
            this.pos = pos;
            this.limit = limit;
            this.cache = new long[numbersCacheSize];
            resetCacheLeftRight(leftSize);
        }

        void resetCacheLeftRight(int leftSize) {
            prevNdLim = leftSize;
            nextStLim = cache.length - leftSize;
            prevNd = 0;
            nextSt = cache.length - 1;
        }

        void clearCounters() {
            prevNd = 0;
            nextSt = cache.length - 1;
            count = 0;
            countLeft = 0;
            countRight = 0;
            dupCount = 0;
        }

        public void updateCacheLeft(long num) {
            if (prevNd < prevNdLim) {
                cache[prevNd++] = num;
                if (prevNd == prevNdLim) {
                    Arrays.sort(cache, 0, prevNdLim);
                }
            } else {
                int idx = Arrays.binarySearch(cache, 0, prevNdLim, num);
                if (idx < 0) {
                    idx = -(idx + 2);
                    if (idx > -1) {
                        if (idx > 0) {
                            System.arraycopy(cache, 1, cache, 0, idx);
                        }
                        cache[idx] = num;
                    }
                }
            }
        }

        public void updateCacheRight(long num) {
            if (nextSt >= nextStLim) {
                cache[nextSt--] = num;
                if (nextSt < nextStLim) {
                    Arrays.sort(cache, nextStLim, cache.length);
                }
            } else {
                int idx = Arrays.binarySearch(cache, nextStLim, cache.length, num);
                if (idx < 0) {
                    idx = -(idx + 1);
                    if (idx < cache.length) {
                        if (idx > nextStLim) {
                            System.arraycopy(cache, idx, cache,
                                    idx + 1, cache.length - idx - 1);
                        }
                        cache[idx] = num;
                    }
                }
            }
        }
    }
}
