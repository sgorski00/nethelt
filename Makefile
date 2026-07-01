.PHONY: up down build logs test coverage install-bg-client psql

up:
	docker compose -f infrastructure/docker-compose.yml up -d

down:
	docker compose -f infrastructure/docker-compose.yml down

build:
	docker compose -f infrastructure/docker-compose.yml build

logs:
	docker compose -f infrastructure/docker-compose.yml logs -f

test:
	mvn clean verify -Dspring.profiles.active=test

coverage:
	mvn clean verify -Pcoverage -Dspring.profiles.active=test

install-bg-client:
	mvn clean install -pl :bg-client -am -DskipTests

psql:
	docker exec -it nh-postgres psql -U postgres -d nh-db

lint:
	@echo "Running Java linter..."
	@mvn spotless:apply
	@echo "Running JS prettier..."
	@cd frontend && npx prettier . --write
	@echo "Running JS eslint..."
	@cd frontend && ng lint --fix
