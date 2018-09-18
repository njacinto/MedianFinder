#!/bin/bash
# settings
#JARNAME=MedianFinder-1.0.jar
JARNAME=target/MedianFinder-1.0.jar
#
CACHE_DEFAULT=1024
THREADS_DEFAULT=0
PIVOT_DEFAULT=bestOf
BESTOFSAMPLES_DEFAULT=15
#
TRUE_VALUE=true
FALSE_VALUE=false
#
CACHE_PARAM=-Dcache
MULTITHREAD_PARAM=-Dmultithread
THREADS_PARAM=-Dthreads
PIVOT_PARAM=-Dpivot
BESTOFSAMPLES_PARAM=-DbestOfSamples
PRINTTIME_PARAM=-DprintTime
#
function printHelp {
  echo ""
  echo "Usage: run.sh -c <cache_size> -s -t <number_of_threads> -p <mean/middle/bestOf> -b <number_of_samples> -d"
  echo -e "\tOptions:"
  echo -e "\t\t-c / --cache -> cache size. Default $CACHE_DEFAULT "
  echo -e "\t\t-s / --singlethread -> use single thread version."
  echo -e "\t\t-t / --threads -> number of threads. Zero to use available threads. Default $THREADS_DEFAULT "
  echo -e "\t\t-p / --pivot -> pivot selection strategy. Valid values are mean, middle, bestOf. Default $PIVOT_DEFAULT "
  echo -e "\t\t-b / --bestOfSample -> number of samples in case of pivot strategy equal to bestOf. Must be bigger than 0. Default $BESTOFSAMPLES_DEFAULT "
  echo -e "\t\t-d / --printTime -> prints the execution time."
  echo ""
}
#
cache=$CACHE_DEFAULT
multithread=$TRUE_VALUE
threads=$THREADS_DEFAULT
pivot=$PIVOT_DEFAULT
bestOfSamples=$BESTOFSAMPLES_DEFAULT
printTime=$FALSE_VALUE
file=

while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    -h|--help)
      printHelp
      exit
    ;;
    -c|--cache)
      cache="$2"
      shift
      shift
    ;;
    -s|--singlethread)
      multithread=$FALSE_VALUE
      shift
    ;;
    -t|--threads)
      threads="$2"
      shift 
      shift 
    ;;
    -p|--pivot)
      if [ "$2" = 'bestOf' ] || [ "$2" = 'mean' ] || [ "$2" = 'middle' ]; then
        pivot="$2"
      else
        echo -e "\nInvalid pivot: $2 ."
	printHelp
	exit
      fi
      shift 
      shift 
    ;;
    -b|--bestOfSamples)
      if [ "$2" -gt "0" ]; then
        bestOfSamples="$2"
      else
        echo -e "\nInvalid bestOfSamples value. Must be bigger than 0."
	printHelp
	exit
      fi
      shift 
      shift 
    ;;
    -d|--printTime)
      printTime=$TRUE_VALUE
      shift  
    ;;
    *)  # others
      if ! [[ ${1:0:1} == '-' ]]; then
	file=$1
      else
	echo -e "\nInvalid option: $1"
	printHelp
	exit
      fi
      shift
    ;;
  esac
done

#echo "Arguments: $cache - $multithread - $threads - $pivot - $bestOfSamples - $printTime - $file"

java "$CACHE_PARAM=$cache" "$MULTITHREAD_PARAM=$multithread" \
"$THREADS_PARAM=$threads" "$PIVOT_PARAM=$pivot" "$BESTOFSAMPLES_PARAM=$bestOfSamples" \
"$PRINTTIME_PARAM=$printTime" -jar "$JARNAME" "$file"

res=$?
#echo "$res"
if ! [ $res = "0" ]; then
  echo -e "\nError executing command."
  printHelp
fi
