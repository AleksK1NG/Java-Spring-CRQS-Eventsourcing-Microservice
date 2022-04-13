.PHONY:


#Quarkus

## Running the application in dev mode
#You can run your application in dev mode that enables live coding using:
dev:
	./mvnw compile quarkus:dev

# The application can be packaged using:
package:
	./mvnw package


package-uber-jar:
	./mvnw package -Dquarkus.package.type=uber-jar

package-native:
	./mvnw package -Pnative

package-native-GraalVM:
	./mvnw package -Pnative -Dquarkus.native.container-build=true

# ==============================================================================
# Docker

local:
	@echo Clearing kafka data
	rm -rf ./kafka_data
	@echo Clearing zookeeper data
	rm -rf ./zookeeper
	@echo Clearing prometheus data
	rm -rf ./prometheus
	@echo Starting local docker compose
	docker-compose -f docker-compose.yaml up -d --build


clean_docker_data:
	@echo Clearing kafka data
	rm -rf ./kafka_data
	@echo Clearing zookeeper data
	rm -rf ./zookeeper
	@echo Clearing prometheus data
	rm -rf ./prometheus
	@echo Clearing pg data
	rm -rf ./pgdata
	@echo Clearing mongo data
	rm -rf ./mongodb_data_container

# ==============================================================================
# Docker support

FILES := $(shell docker ps -aq)

down-local:
	docker stop $(FILES)
	docker rm $(FILES)

clean:
	docker system prune -f

logs-local:
	docker logs -f $(FILES)