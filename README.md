# LargeFileProcessingSample
Sample app to process large files using spring batch

Application exposes 2 endpoints:
1. GET  - /average-temperatures with input param city. Returns the average temperatures for each year. Sample: /average-temperatures?city=Wrocław
2. POST - /update-data triggers reload of file datasource from /resources/large_file.csv

Few assumptions:
* At start up the contents of /resources/example_file.csv are used.
* Data in GET endpoint is cached, cache is force refreshed after data update.
* To test with larger files /resoures/largie_file.csv can be generated with sample data running the [GenerateDataTest.java](..%2Frecruitment%2Flarge-file-challenge%2Fsrc%2Ftest%2Fjava%2Fcom%2Fdaycode%2FGenerateDataTest.java)
* Project uses java 17


To run the application You can use commands:  
``mvnw clean install spring-boot:run -Dfile.encoding=UTF-8 -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8"`` <br>
or <br>
``./mvnw clean install spring-boot:run -Dfile.encoding=UTF-8 -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8"``
or <br>
``mvn clean install spring-boot:run -Dfile.encoding=UTF-8 -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8"``

