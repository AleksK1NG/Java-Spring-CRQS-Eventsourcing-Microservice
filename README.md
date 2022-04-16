### Spring CQRS and Event Sourcing microservice example 👋‍💫✨

#### 👨‍💻 Full list what has been used:
* [Spring](https://spring.io/) - Java Spring
* [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - data access layer
* [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb) - Spring Data MongoDB
* [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth) - Spring Cloud Sleuth distributed tracing
* [Kafka](https://spring.io/projects/spring-kafka) - Spring for Apache Kafka
* [PostgreSQL](https://www.postgresql.org/) - PostgreSQL database.
* [Jaeger](https://www.jaegertracing.io/) - Jaeger is a distributed tracing system
* [Docker](https://www.docker.com/) - Docker
* [Prometheus](https://prometheus.io/) - Prometheus
* [Grafana](https://grafana.com/) - Grafana
* [Flyway](https://flywaydb.org/) - Database migrations
* [Resilience4j](https://resilience4j.readme.io/docs/getting-started-3) - Resilience4j is a lightweight, easy-to-use fault tolerance
* [Swagger OpenAPI 3](https://springdoc.org/) - java library helps to automate the generation of API documentation

### Swagger UI:

http://localhost:8006/swagger-ui/index.html

### Jaeger UI:

http://localhost:16686

### Prometheus UI:

http://localhost:9090

### Grafana UI:

http://localhost:3005


For local development:
```
make local // runs docker-compose.yaml with all required containers
run spring application
```