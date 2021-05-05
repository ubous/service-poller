# service-poller-backend

This backend service does an HTTP GET request for each registered service periodically keeps track of their status.

A service status can be:
* "OK" - if the request succeeds, even if it returns an error code.
* "FAIL" - if the request fails, e.g. timeout.
* "UNKNOWN" - the service has not been queried yet, e.g. recently added.

Services can be added, update, and deleted using a REST HTTP API.

Currently, this service uses a file as persistent storage for services and their status.

## Tech
This service is built in Java 11 using [Vert.x](https://vertx.io/) with [Vert.x-Web](https://vertx.io/docs/vertx-web/java/).

## Running
### Maven
```sh
mvn exec:java
```

### IntelliJ
Right click Main.java and then Run.

## HTTP API

### Fetch a service by Id

`GET /api/services/<service-id>`

Responses:
* `200`: Service found. Resonse body contains a JSON.
* `400`: Bad request. Check if the Id is a valid UUID.
* `404`: Service not found.

### Fetch all services

`GET /api/services/`

Responses:
* `200`: Service found. Response body contains a JSON with a list of services.

### Add a service

`POST /api/services/`

Body: should contain a JSON object with the following fields:
* `name`
* `url`: must be a valid URL

Responses:
* `200`: Service added. Response body contains a JSON with the created service.
* `400`: Bad request. Check if the body has a valid JSON with the required fields. 

### Update a service

`PUT /api/services/`

Body: should contain a JSON object with the following fields:
* `name`
* `url`: must be a valid URL

Responses:
* `200`: Service updated. Response body contains a JSON with the updated service.
* `400`: Bad request. Check if the body has a valid JSON with the required fields.

### Remove a service

`DELETE /api/services/<service-id>`

Responses:
* `200`: Service add. Response body is empty.
* `400`: Bad request. Check if the Id is a valid UUID.
* `404`: Service not found.
