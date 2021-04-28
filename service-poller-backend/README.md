# service-poller-backend

## Running
### Maven
```sh
mvn exec:java
```

### IntelliJ
Right click Main.java and then Run.

## HTTP API

### Fetch a service by Id

`GET /api/services/<uuid>`

Responses:
* `200`: Service found. Resonse body contains a JSON.
* `400`: Bad request. Check if the Id is a valid UUID.
* `404`: Service was not found

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
* `200`: Service add. Response body contains a JSON with the created service.
* `400`: Bad request. Check if the body has a valid JSON with the required fields. 
