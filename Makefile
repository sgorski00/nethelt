up:
	@echo "Starting services..."
	@docker compose -f ./infrastructure/docker/compose.yml up -d

build-up:
	@echo "Rebuilding and starting services..."
	@docker compose -f ./infrastructure/docker/compose.yml up --build  -d

rebuild:
	@echo "Rebuilding without without cache and starting services..."
	@docker compose -f ./infrastructure/docker/compose.yml build --no-cache
	@docker compose -f ./infrastructure/docker/compose.yml up  -d

logs:
	@echo "Showing the logs..."
	@docker compose -f ./infrastructure/docker/compose.yml logs -f

down:
	@echo "Stopping services..."
	@docker compose -f ./infrastructure/docker/compose.yml down