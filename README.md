![MSS architecture](https://ibb.co/p4fRrbm)
### Dependencies:
* Java 17
* Spring Boot 3.1.2
* MongoDB
* Redis
* Kafka
* React

### Run the services:
* Add environment variables to your application:
```
KAFKA_HOST=localhost:9092;MONGO_DB=step_stats;MONGO_HOST=localhost;MONGO_PASSWORD=password;MONGO_PORT=27017;MONGO_USERNAME=admin
```
* After installing all the dependencies, run the MongDB, Kafka and Redis servers in the background.
##### Run the Step service:
* Install and build: ```cd steps && ./gradlew clean && ./gradlew build```
* Run the service: ```./gradlew bootRun```
##### Run the Leaderboard service:
* Install and build: ```cd leaderboard && ./gradlew clean && ./gradlew build```
* Run the service: ```./gradlew bootRun```
##### Run the Client:
* Install and build: ```cd client && npm install```
* Run the service: ```npm start```
