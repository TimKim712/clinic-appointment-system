# Clinic Appointment System

> Clinic appointment system for scheduling medical appointments for CMPE 172. Contains logic for booking appointments, concurrent handling, a mock external notification system, and detailed logging and health checks.

## Set Up

### Pre-Requisites
- Docker
- Java 17
- Maven
- PostgreSQL


### Start the Database Instance
```bash
cd src
docker-compose up -d
```
This starts up the local database instance; the username and password for the database should both be "postgres". Further modifications to the database can be done with the PostgreSQL CLI.

### Run the application
```bash
cd .. #should be in the project's root directory
mvn spring-boot:run
```
This should start up the application on `localhost:8080` as well as run `data.sql`, which creates the schema and populates the local database. The local database should also be on port 5433. 



