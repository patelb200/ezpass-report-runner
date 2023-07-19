# EZ-Pass Report Command

Utility that scrapes and displays the vehicles, transponders and transactions from PA EZ-Pass site.

## Usage

### Arguments

```text
 -u <arg>           Account username
 -p <arg>           Account password
 -startDate <arg>   Report start date in YYYYMMDD format
 -endDate <arg>     Report end date in YYYYMMDD format
```

### Example

``` shell
java -jar .\ezpass-report-command-1-jar-with-dependencies.jar -u user -p pass -startDate 20230501 -endDate 20230530
```

## Build

### Jar

```shell
mvn package -pl ezpass-report-command -am
```

### Native image

In order to build this program to a native executable (in this example linux x86_64), use the docker image (`graalvm_maven.Dockerfile`) in the root
directory. 

1. Build image

```shell
docker build -t graalvm_maven:latest -f "C:\..\graalvm_maven.Dockerfile" .
```

2. Start a container using the image and mount the volume for `ezpass-report-runner` repository

```shell
docker container run -it --rm -v C:\..\ezpass-report-runner:/ezpass-report-runner graalvm_maven:latest bash
```

3. Run the following command

```shell
cd /ezpass-report-runner
mvn package -Pnative -pl ezpass-report-command -am
```

4. Run the native executable

```shell
cd ./ezpass-report-command/target
./ezpass-report-command -u user -p pass -startDate 20230705 -endDate 20230718
```
