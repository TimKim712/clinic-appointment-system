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

### Check to make sure the database is properly populated
```bash
docker exec -it clinic-appointment-db psql -U postgres -d clinic_appointment
```
This connects to the postgres database instance from the container and should re-direct you to the psql shell. Inside the shell, run queries to return all the rows from each table, something like:
```bash
  SELECT * FROM users;                                                                                                                                                                  
  SELECT * FROM patients;                                   
  SELECT * FROM providers;
  SELECT * FROM services;
  SELECT * FROM availability_slots;
```
There should be 5 users, 2 patients, 2 providers, 3 services, and 4 slots. To exit out of the psql shell, type `\q`. 

