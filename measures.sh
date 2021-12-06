#!/bin/bash
mvn clean package

jarPath=./target/PC-monitors-comparison-1.0.1-jar-with-dependencies.jar
outputFile=measures/res

measureTypes=(access time)

workersNumb=(3)
monitorTypes=(2 3 4)
bufferSizes=(100 )

secondsOfMeasuring=1

for bufferSize in "${bufferSizes[@]}"; do
    for producersNumb in "${workersNumb[@]}"; do
        for consumersNumb in "${workersNumb[@]}"; do
            for monitorType in "${monitorTypes[@]}"; do
                for measuresType in "${measureTypes[@]}"; do

                    echo $measuresType $monitorType $producersNumb $consumersNumb $bufferSize $secondsOfMeasuring
                    java -jar $jarPath \
                          $measuresType $monitorType $producersNumb $consumersNumb $bufferSize $secondsOfMeasuring \
                          >> "$outputFile-$measuresType.txt";

                done
            done
        done
    done
done

