# scheduler-batch-executor

- This service is used Listen the batch request from the activeMQ. Requests are produced to activeMQ via payment-batch-integration-outbound-service if the batch type is ACH.
	-	Once it listens the batch reqest, calculate the nexte execution date and  warehouse the batch request via schedule-batch-service.
	-	If the next execution date is todays date, then submit the request back to payment-batch-integration-outbound-service to submit it to Core like CGI.
	    This service recieve the response back from payment-batch-integration-outbound-service and update the status to  scheduler ware house table and also OOTB table.
 	- This service also have an API to display the Batch-order history client-api/v1/scheduled-batch-orders/history based on the query paramenter like executiondaterange, status, etc.

 	- This service also handles the REcurring batch payments. Next exeuction date of recurring payment is handled at this service.



#Getting Started
* [Extend and build](https://community.backbase.com/documentation/ServiceSDK/latest/extend_and_build)

## Dependencies

Requires a running Eureka registry, by default on port 8080.

## Configuration

Service configuration is under `src/main/resources/application.yaml`.

## Running

To run the service in development mode, use:
- `mvn spring-boot:run`

To run the service from the built binaries, use:
- `java -jar target/scheduler-batch-executor-1.1.0-SNAPSHOT.jar`

## Authorization

Requests to this service are authorized with a Backbase Internal JWT, therefore you must access this service via the 
Backbase Gateway after authenticating with the authentication service.

For local development, an internal JWT can be created from http://jwt.io, entering `JWTSecretKeyDontUseInProduction!` 
as the secret in the signature to generate a valid signed JWT.

## Community Documentation

Add links to documentation including setup, config, etc.

## Jira Project

Add link to Jira project.

## Confluence Links
Links to relevant confluence pages (design etc).

