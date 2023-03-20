# portx-challenge

This challenge was developed as per the provided specifications.

## High-level architecture

We have the backend application which has dependency on a MySQL database and a Kafka broker.
We also have a standalone client application which consumes the backend API.

- Backend application
- MySQL database
- Kafka broker
- Client application

Both the backend and client applications are built on Java 8 and SpringBoot, plus a number of other frameworks.
Both applications use maven as a build tool and dependency management.

## Build and run the backend application

Clone GitHub repository locally:

`https://github.com/joaquinrojkind/portx-challenge`

From the terminal window in the project's root directory execute the following command:

`docker-compose up`

This will build and run the application on docker and will download images and run containers for MySQL and Kafka.
The backend runs on port 8080, make sure it's not in use.

## Create Kafka topic

From a terminal window access the docker container where Kafka is running:

`docker exec -it broker bash`

Create the topic:

`kafka-topics --create --topic transaction-created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1`

Verify the topic:

`kafka-topics --bootstrap-server=localhost:9092 --list`

## Build and run the client application

From a new terminal window clone GitHub repository locally:

`https://github.com/joaquinrojkind/portx-challenge-client`

With the backend already running, from the client's project root directory run the following command:

`mvn clean install`

Run the client:

`mvn spring-boot:run`

The default request count to be executed is <b>12</b>. Optionally, add the `paymentRequestCount` param in the command line for a custom count.
The maximum request count allowed is <b>30</b>.
Here's an example:

`mvn spring-boot:run -Dspring-boot.run.arguments="paymentRequestCount=5"`

Once the client application is started it will execute the requests and shut down.
Check the logs in the terminal to preview all the relevant info about each request, such as request body, response status, response body, etc.

## Verify the database

With a MySQL database client such as Sequel Pro, connect to the database and verify the table data.
Connection params are as follows:

- Host: `127.0.0.1`
- Port: `3306`
- Username: `root`
- Password: `123456`

## Verify the Kafka broker

From a terminal window access the docker container where Kafka is running:

`docker exec -it broker bash`

Read the existing events:

`kafka-console-consumer --topic transaction-created --from-beginning --bootstrap-server localhost:9092`

## Further testing with Postman

Use the Postman collection in `src/main/resources/testing` in order to test the following:

- Check status endpoint.
- Failure in the accept payment endpoint (to force failure send a non-existing user or account id in the payload)
- Idempotency in the accept payment endpoint (check optional header).
- Sending ids of users and accounts in order to reference existing entities instead of creating new ones.

Example of payload with one account with id only and one account brand new:
```
    "sender": {
       "id": 6
    },
    "receiver": {
        "accountType": "checking",
        "accountNumber": "89826654"
    }
```

## Automated tests

### Unit tests

The main service class which holds most of the business logic has been thoroughly tested with unit tests. All dependencies are mocked hence the tests are self-contained.
The test class is:

`src/test/java/com/portx/payment/PaymentServiceImplTest.java`

From the backend project's root directory run the following command:

`mvn clean test`

This will run the tests and will print the results to the console.

### Integration tests

I would most definitely add integration tests to test the API, but it would require quite some more time so I decided to leave it as a TODO item and share the conceptual approach in here.

The idea is that the integration tests would leverage the application context with a test profile so that we would be testing the actual API. 

The database could be replaced by an in memory database and a migration script to create the tables, all of this automated. 

For the Kafka broker we could mock the Kafka Service class that would be invoked but would do nothing since there's nothing being returned by this service, although we could capture and assert the message that the application is passing to this class.

The main purpose of the testing however is to verify the behaviour of the API endpoints in a couple of different scenarios (with and without idempotency key, with and without entity ids in the payload, etc.)

## Questions from the challenge

1) Our sales team tells us that some of the customers will be sending around 1 million payments per day, mostly during business hours, and that they will perform around 10 million queries per day. Which steps will you take to make sure the application can handle this load?



2) How does your system guarantee that when accepting a payment the payment is saved locally and the message is sent to the topic? What would happen if a container running the application is restarted after saving the payment to the DB and before sending the message to the topic?

