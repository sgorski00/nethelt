.PHONY: up down build logs test coverage dist psql

up:
	docker compose -f infrastructure/docker-compose.yml up -d

down:
	docker compose -f infrastructure/docker-compose.yml down

build:
	docker compose -f infrastructure/docker-compose.yml build

logs:
	docker compose -f infrastructure/docker-compose.yml logs -f

logs-backend:
	docker compose -f infrastructure/docker-compose.yml logs nh-web-api -f

restart-backend:
	docker compose -f infrastructure/docker-compose.yml build nh-web-api
	docker compose -f infrastructure/docker-compose.yml up nh-web-api -d

test:
	@echo "Running backend tests..."
	@mvn clean verify -Dspring.profiles.active=test
	@echo "Running frontend tests..."
	@cd frontend && npm run test -- --watch=false

coverage:
	mvn clean verify -Pcoverage -Dspring.profiles.active=test

dist:
	mvn clean package -Pdist -pl desktop-client/distribution -am -DskipTests

psql:
	docker exec -it nh-postgres psql -U postgres -d nh-db

lint:
	@echo "Running Java linter..."
	@mvn spotless:apply
	@echo "Running JS prettier..."
	@cd frontend && npx prettier . --write
	@echo "Running JS eslint..."
	@cd frontend && ng lint --fix
