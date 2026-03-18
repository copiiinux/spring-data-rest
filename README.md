# spring-data-rest

### Run the app

````
./mvnw clean spring-boot:run
````

### Build the app

````
./mvnw clean package
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
docker compose build
````