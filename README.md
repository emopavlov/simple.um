# simple.um
A simple user management for Acme Corporation


### Build and run project
simple.um is a web project built with Spring Boot. To run the project you would need to have Maven and JDK 1.8 installed. Some resources are downloaded from remote location, therefore, internet access is required as well.

To build and run the project execute the following command from the project top folder:
> mvn spring-boot:run

The server will be listening on port 8080. Access application at <http://localhost:8080>

###TODO
* Refresh main user table after a change to a user
* Improve errors displayed on error form
* Successful operation notifications
* Switch to user IDs instead of email as a user identifier
* Fix date is unit tests
* Corporation logo
* User images