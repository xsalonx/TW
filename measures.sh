#!/bin/bash
mvn clean package

jarPath=./target/PC-monitors-comparison-1.0.1-jar-with-dependencies.jar
outputFile=measures/res

measureTypes=(access time)

workersNumb=(3 6 10 12)
bufferSizes=(100 1000 10000)

secondsOfMeasuring=6

for bufferSize in "${bufferSizes[@]}"; do
    for producersNumb in "${workersNumb[@]}"; do
        for consumersNumb in "${workersNumb[@]}"; do
            for measuresType in "${measureTypes[@]}"; do

              echo $measuresType $producersNumb $consumersNumb $bufferSize $secondsOfMeasuring
              java -jar $jarPath \
                    $measuresType $producersNumb $consumersNumb $bufferSize $secondsOfMeasuring \
                    >> "$outputFile-$measuresType.txt";

            done
        done
    done
done

