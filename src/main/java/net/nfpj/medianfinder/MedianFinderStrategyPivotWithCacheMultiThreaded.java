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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Finds the median using the pivot and keeping a cache of numbers left and
 * right of the pivot. The cache will contain the numbers before and after the
 * pivot, allowing to jump several numbers in each iteration.
 *
 * @author njacinto
 */
public class MedianFinderStrategyPivotWithCacheMultiThreaded extends MedianFinderStrategyPivotWithCache {

    public static final MedianFinderStrategyPivotWithCacheMultiThreaded INSTANCE = new MedianFinderStrategyPivotWithCacheMultiThreaded();
    //
    protected final int numThreads;

    public MedianFinderStrategyPivotWithCacheMultiThreaded() {
        this(DEFAULT_NUMBERS_CACHE_SIZE, Runtime.getRuntime().availableProcessors(),
                new PivotSelectionStrategyBestOfN(15));
//        this(DEFAULT_NUMBERS_CACHE_SIZE, DEFAULT_NUMBER_OF_THREADS, PivotSelectionStrategyMiddle.INSTANCE);
//        this(DEFAULT_NUMBERS_CACHE_SIZE, DEFAULT_NUMBER_OF_THREADS, PivotSelectionStrategyMean.INSTANCE);
    }

    public MedianFinderStrategyPivotWithCacheMultiThreaded(int numbersCacheSize) {
        this(numbersCacheSize, Runtime.getRuntime().availableProcessors(),
                new PivotSelectionStrategyBestOfN(15));
    }

    public MedianFinderStrategyPivotWithCacheMultiThreaded(int numbersCacheSize, int numberOfThreads) {
        this(numbersCacheSize, numberOfThreads, new PivotSelectionStrategyBestOfN(15));
    }

    public MedianFinderStrategyPivotWithCacheMultiThreaded(PivotSelectionStrategy pivotSelection) {
        this(DEFAULT_NUMBERS_CACHE_SIZE, Runtime.getRuntime().availableProcessors(), pivotSelection);
    }

    public MedianFinderStrategyPivotWithCacheMultiThreaded(int numbersCacheSize, int numberOfThreads,
            PivotSelectionStrategy pivotSelection) {
        super(numbersCacheSize, pivotSelection);
        this.numThreads = (numberOfThreads > 0) ? numberOfThreads : Runtime.getRuntime().availableProcessors();
    }

    @Override
    public long findMedian(File file) throws MedianFinderException, IOException {
        final long fileSize = file.length();
        int numPartitions = (fileSize<4096) ? 1 : this.numThreads;
        final Data[] dataList = new Data[numPartitions];
        final long avgBlockSize = fileSize / numPartitions;
        //
        boolean medianFound = false;
        long pivot = 0;
        //
        ExecutorService executor = Executors.newFixedThreadPool(numPartitions);
        //
        final RandomAccessFile[] in = new RandomAccessFile[numPartitions];
        try {
            for (int i = 0; i < numPartitions; i++) {
                in[i] = new RandomAccessFile(file, "r");
            }
            pivot = pivotSelection.getPivot(file, in[0]);
            //
            long pos = 0;
            for (int i = 0; i < numPartitions - 1; ++i) {
                dataList[i] = new Data(pos, findEndOfBlock(in[0], avgBlockSize + pos), numbersCacheSize);
                pos = dataList[i].limit;
            }
            dataList[numPartitions - 1] = new Data(pos, file.length(), numbersCacheSize);
            //
            final long allPositiveLimit = numbersCacheSize * 4;
            final long allNegativeLimit = -allPositiveLimit;
            long[] cache = new long[numbersCacheSize * 4];
            Future[] tasks = new Future[numPartitions];
            long count, countLeft, countRight, dupCount;
//            long countIt = 0;
            while (!medianFound) {
//                countIt++;
                count = 0;
                countLeft = 0;
                countRight = 0;
                dupCount = 0;
                final long p = pivot;
                for (int i = 0; i < numPartitions; i++) {
                    if (dataList[i].check) {
                        final int idx = i;
                        tasks[i] = executor.submit(() -> {
                            try {
                                countLeftRight(in[idx], dataList[idx], p);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                }

                for (int i = 0; i < numPartitions; i++) {
                    try {
                        tasks[i].get();
                    } catch (ExecutionException ex) {
                        if (ex.getCause() instanceof MedianFinderException) {
                            throw (MedianFinderException) ex.getCause();
                        } else if (ex.getCause() instanceof RuntimeException) {
                            throw (RuntimeException) ex.getCause();
                        } else {
                            throw new RuntimeException(ex);
                        }
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    count += dataList[i].count;
                    countLeft += dataList[i].countLeft;
                    countRight += dataList[i].countRight;
                    dupCount += dataList[i].dupCount;
                }
                if (dupCount > 1) {
                    throw new DuplicateNumberException("Duplicate number found.");
                }
                //
                if ((count & 1) == 0 && ((countLeft - countRight) == 1 || (countRight - countLeft) == 1)) {
                    long other;
                    if (countLeft > countRight) {
                        other = Long.MIN_VALUE;
                        for (int i = 0; i < numPartitions; i++) {
                            other = NumberUtil.getBiggerThan(other, dataList[i].cache, 0, dataList[i].prevNd);
                        }
                    } else {
                        other = Long.MAX_VALUE;
                        for (int i = 0; i < numPartitions; i++) {
                            other = NumberUtil.getSmallerThan(other, dataList[i].cache,
                                    dataList[i].nextSt + 1, dataList[i].cache.length);
                        }
                    }
                    pivot = (pivot + other) / 2;
                    medianFound = true;
                } else if (countLeft > countRight) {
                    //                System.out.println("Pivot = " + pivot
                    //                        + " Prev = " + prev + " count = " + count
                    //                        + " countLeft = " + countLeft + " countRight = " + countRight);
                    int maxIdx = dataList[0].prevNdLim;
                    int cacheIdx = 0;
                    for (int i = 0; i < numPartitions; i++) {
                        //
                        dataList[i].check = dataList[i].countRight != dataList[i].count;
                        if (dataList[i].prevNd > 0) {
                            System.arraycopy(dataList[i].cache, 0, cache, cacheIdx, dataList[i].prevNd);
                            cacheIdx += dataList[i].prevNd;
                        }
                        dataList[i].prevNdLim = dataList[i].nextStLim = dataList[i].cache.length - 1;
                    }
                    Arrays.sort(cache, 0, cacheIdx);
                    int idx = (int) (cacheIdx - ((countLeft - countRight) >> 1));
                    if (idx > allNegativeLimit) {
                        int minIdx = cacheIdx - maxIdx;
                        if (minIdx < 0) {
                            minIdx = 0;
                        }
                        pivot = cache[idx < 0 ? 0 : idx < cacheIdx ? idx : cacheIdx - 1];
                    } else {
                        pivot = cache[0];
                        for (int i = 0; i < numPartitions; i++) {
                            dataList[i].check = true;
                            dataList[i].prevNdLim = dataList[i].nextStLim = numbersCacheSize >> 1;
                        }
                    }
                } else if (countLeft < countRight) {
                    //                System.out.println("Pivot = " + pivot
                    //                        + " Next = " + next + " count = " + count
                    //                        + " countLeft = " + countLeft + " countRight = " + countRight);
                    int maxIdx = dataList[0].cache.length - dataList[0].nextStLim;
                    int cacheIdx = 0;
                    for (int i = 0; i < numPartitions; i++) {
                        //
                        dataList[i].check = dataList[i].countLeft != dataList[i].count;
                        final int nextSt = dataList[i].nextSt + 1;
                        if (nextSt < dataList[i].cache.length) {
                            System.arraycopy(dataList[i].cache, nextSt, cache, cacheIdx,
                                    dataList[i].cache.length - nextSt);
                            cacheIdx += dataList[i].cache.length - nextSt;
                        }
                        dataList[i].prevNdLim = dataList[i].nextStLim = 1;
                    }
                    Arrays.sort(cache, 0, cacheIdx);
                    int idx = (int) ((countRight - countLeft) >> 1) - 1;
                    if (idx < allPositiveLimit) {
                        if (maxIdx > cacheIdx) {
                            maxIdx = cacheIdx;
                        }
                        pivot = cache[idx < maxIdx ? idx > -1 ? idx : 0 : maxIdx - 1];
                    } else {
                        pivot = cache[cacheIdx - 1];
                        for (int i = 0; i < numPartitions; i++) {
                            dataList[i].check = true;
                            dataList[i].prevNdLim = dataList[i].nextStLim = numbersCacheSize >> 1;
                        }
                    }
                } else {
                    medianFound = true;
                }
            }
            //System.out.println("CountIt: " + countIt);
        } finally {
            executor.shutdownNow();
            for (int i = 0; i < numPartitions; i++) {
                if (in[i] != null) {
                    in[i].close();
                }
            }
        }
        return medianFound ? pivot : -1;
    }

    @Override
    public String toString() {
        return "MedianFinderStrategyPivotWithCacheMultiThreaded{" + "numThreads=" + numThreads + '}';
    }



    private void countLeftRight(RandomAccessFile in, Data dta, long pivot) throws MedianFinderException, IOException {
        final byte[] readBuff = new byte[Constants.BUFFER_SIZE];
        int buffInitPos = 0;
        dta.clearCounters();
        //
        int eolIdx, stIdx;
        int readLen;
        long remaining = dta.limit - dta.pos;
        in.seek(dta.pos);
        while (remaining > 0 && (readLen = in.read(readBuff, buffInitPos,
                (readBuff.length - buffInitPos < remaining) ? readBuff.length - buffInitPos : (int) remaining)) != -1) {
            remaining -= readLen;
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
    }

    private static long findEndOfBlock(RandomAccessFile in, long pos) throws IOException {
        byte[] arr = new byte[Constants.BUFFER_SIZE];
        in.seek(pos);
        int len = in.read(arr);
        if (len > 0) {
            int eolIdx = 0;
            for (; eolIdx < len && arr[eolIdx] != Constants.EOL; eolIdx++);
            if (eolIdx < len) {
                return pos + eolIdx;
            }
        }
        return -1;
    }

}
