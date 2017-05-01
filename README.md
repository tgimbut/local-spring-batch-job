# run sample with single-threaded step
./gradlew bootRun -Pargs="inputFile=data/beers.csv outputFile=build/rating.csv" -PsingleThreadStep

# run sample with multi-threaded step
./gradlew bootRun -Pargs="inputFile=data/beers.csv outputFile=build/rating.csv" -PmultiThreadStep

# run sample with local partitioned step
./gradlew bootRun -Pargs="inputFile=data/beers.csv outputFile=build/rating.csv" -PlocalPartitionedStep
