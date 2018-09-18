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

/**
 *
 * @author njacinto
 */
public class Main {
    public static final String PIVOT_BEST_OF = "bestOf";
    public static final String PIVOT_MEAN = "mean";
    public static final String PIVOT_MIDDLE = "middle";
    public static final String CACHE_PROPERTY_NAME = "cache";
    public static final String MULTITHREAD_PROPERTY_NAME = "multithread";
    public static final String THREADS_PROPERTY_NAME = "thread";
    public static final String PIVOT_PROPERTY_NAME = "pivot";
    public static final String BESTOFSAMPLES_PROPERTY_NAME = "bestOfSamples";
    public static final String PRINTTIME_PROPERTY_NAME = "printTime";
    public static final int CACHE_DEFAULT_VALUE = 1024;
    public static final boolean MULTITHREAD_DEFAULT_VALUE = true;
    public static final boolean PRINTTIME_DEFAULT_VALUE = true;
    public static final int THREADS_DEFAULT_VALUE = 0;
    public static final String PIVOT_DEFAULT_VALUE = PIVOT_BEST_OF;
    public static final int BESTOFSAMPLES_DEFAULT_VALUE = 15;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MedianFinderException, IOException {
        String filename;
        if (args.length > 0 && (filename=args[0]) != null) {
            File file = new File(filename);
            if (file.isFile() && file.canRead()) {
                Configuration conf = getConfiguration();
                if(conf!=null){
                    PivotSelectionStrategy pivotStrategy = getPivotStrategy(conf.pivot, conf.bestOfSamples);
                    MedianFinderStrategy strategy = getStrategy(conf.cache,
                            conf.multithread, conf.threads, pivotStrategy);
                    long timestamp = System.currentTimeMillis();
                    long median = strategy.findMedian(file);
                    //System.out.println(conf.toString());
                    if(conf.printTime){
                        System.out.println("time: " + (System.currentTimeMillis() - timestamp));
                        System.out.println("Median: " + median);
                    } else {
                        System.out.println(median);
                    }
                    return;
                }
            } else {
                System.err.println("File not accessible: " + filename);
            }
        } else {
            System.err.println("Missing filename.");
        }
        System.exit(1);
    }

    private static Configuration getConfiguration() {
        try {
            Configuration conf = new Configuration();
            conf.cache = PropertiesUtil.get(CACHE_PROPERTY_NAME, CACHE_DEFAULT_VALUE);
            conf.multithread = PropertiesUtil.get(MULTITHREAD_PROPERTY_NAME, MULTITHREAD_DEFAULT_VALUE);
            conf.threads = PropertiesUtil.get(THREADS_PROPERTY_NAME, THREADS_DEFAULT_VALUE);
            conf.pivot = System.getProperty(PIVOT_PROPERTY_NAME, PIVOT_DEFAULT_VALUE);
            conf.bestOfSamples = PropertiesUtil.get(BESTOFSAMPLES_PROPERTY_NAME, BESTOFSAMPLES_DEFAULT_VALUE);
            conf.printTime = PropertiesUtil.get(PRINTTIME_PROPERTY_NAME, PRINTTIME_DEFAULT_VALUE);
            if(conf.cache<1){
                printError("Invalid "+CACHE_PROPERTY_NAME+" value "+conf.cache+
                        ". Cache size must be bigger than one.");
                return null;
            }
            if(conf.multithread){
                if(conf.threads<0){
                    printError("Invalid "+THREADS_PROPERTY_NAME+" value "+conf.threads+
                            ". Number of threads must be bigger or equal to zero.");
                    return null;
                }
            }
            switch(conf.pivot){
                case PIVOT_BEST_OF:
                    if(conf.bestOfSamples<1){
                        printError("Invalid "+BESTOFSAMPLES_PROPERTY_NAME+" value "+conf.bestOfSamples+
                                ". Number of samples must be bigger than zero.");
                        return null;
                    }
                    break;
                case PIVOT_MEAN: case PIVOT_MIDDLE:
                    break;
                default:
                    printError("Invalid Pivot strategy: "+conf.pivot);
                    return null;
            }
            return conf;
        } catch(InvalidPropertyValueException ex){
            switch(ex.getProperty()){
                case THREADS_PROPERTY_NAME:
                case CACHE_PROPERTY_NAME:
                case BESTOFSAMPLES_PROPERTY_NAME:
                    printError("The value in property '"+ex.getProperty()+"' must be numeric.");
                    break;
                case MULTITHREAD_PROPERTY_NAME:
                    printError("The value in property '"+ex.getProperty()+"' must be 'true' or 'false'.");
                    break;
                default:
                    printError("Error processing properties: "+ex.getMessage());
            }
        }
        return null;
    }
    private static PivotSelectionStrategy getPivotStrategy(String pivot, int bestOfSamples) {
        switch(pivot){
            case PIVOT_BEST_OF:
                return new PivotSelectionStrategyBestOfN(bestOfSamples);
            case PIVOT_MEAN:
                return new PivotSelectionStrategyMean();
            case PIVOT_MIDDLE:
                return new PivotSelectionStrategyMiddle();
            default:
                return new PivotSelectionStrategyBestOfN(BESTOFSAMPLES_DEFAULT_VALUE);
        }
    }
    private static MedianFinderStrategy getStrategy(int cache, boolean multithread,
            int threads, PivotSelectionStrategy pivotStrategy) {
        if(multithread && threads!=1){
            return new MedianFinderStrategyPivotWithCacheMultiThreaded(cache, threads, pivotStrategy);
        } else {
            return (cache>1) ?
                    new MedianFinderStrategyPivotWithCache(cache, pivotStrategy) :
                    new MedianFinderStrategyPivotWithSingleBeforeAfter(pivotStrategy);
        }
    }

    private static void printHelp(){
        System.out.println("Usage: java -jar edianFinder-1.0.jar -D"+CACHE_PROPERTY_NAME+"="+CACHE_DEFAULT_VALUE
                +" -D"+MULTITHREAD_PROPERTY_NAME+"="+MULTITHREAD_DEFAULT_VALUE
                +" -D"+THREADS_PROPERTY_NAME+"="+THREADS_DEFAULT_VALUE
                +" -D"+PIVOT_PROPERTY_NAME+"="+PIVOT_DEFAULT_VALUE
                +" -D"+BESTOFSAMPLES_PROPERTY_NAME+"="+BESTOFSAMPLES_DEFAULT_VALUE
                +" -D"+PRINTTIME_PROPERTY_NAME+"="+PRINTTIME_DEFAULT_VALUE
                +"\n\tProperties:"
                +"\n\t\t"+CACHE_PROPERTY_NAME+" - size of the memory cache. In case of multithreading the cache will be multiplied by the number of threads. The defualt is "+CACHE_DEFAULT_VALUE+"."
                +"\n\t\t"+MULTITHREAD_PROPERTY_NAME+" - true to use multithread, false otherwise. The default is "+MULTITHREAD_DEFAULT_VALUE+"."
                +"\n\t\t"+THREADS_PROPERTY_NAME+" - number of threads. Zero means to use the number of threads available on the processor. The default is "+THREADS_DEFAULT_VALUE+"."
                +"\n\t\t"+PIVOT_PROPERTY_NAME+" - strategy used to find the initial pivot. The options are:"
                +"\n\t\t       - 'mean' will use the mean of the values."
                +"\n\t\t       - 'midle' will use the value at the middle of the file as pivot."
                +"\n\t\t       - 'bestOf' will take several numbers from the file and use the median of them as pivot."
                +"\n\t\t     The default vaule is "+PIVOT_DEFAULT_VALUE+"."
                +"\n\t\t"+BESTOFSAMPLES_PROPERTY_NAME+" - sets the number of samples to be fetch, when using the pivot strategy 'bestOf'. The default is "+BESTOFSAMPLES_DEFAULT_VALUE+"."
                +"\n\t\t"+PRINTTIME_PROPERTY_NAME+" - prints the execution time. The default is "+PRINTTIME_DEFAULT_VALUE+"."
        );
        System.out.println();
    }

    private static void printError(String message){
        System.out.println();
        System.out.println(message);
        System.out.println();
        printHelp();
    }

    private static class Configuration {
        public int cache;
        public boolean multithread;
        public int threads;
        public String pivot;
        public int bestOfSamples;
        public boolean printTime;

        @Override
        public String toString() {
            return "Configuration{" + "cache=" + cache + ", multithread=" + multithread +
                    ", threads=" + threads + ", pivot=" + pivot + ", bestOfSamples=" + bestOfSamples +
                    ", printTime=" + printTime + '}';
        }
    }
}
