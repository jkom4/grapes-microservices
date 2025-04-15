# In your project directory (where docker-compose.yml is)
docker-compose up --build -d # Build (if needed) and run in detached mode

# To view logs
docker-compose logs -f shiny-app

# To stop
docker-compose down

# Option A: Run R script inside the shiny container (if script is copied in Dockerfile)
docker exec -it grapes-insights-app Rscript /srv/shiny-server/app/generate_and_save_data_refactored.R

# Option B: Run locally, connecting to mapped ports (e.g., 3307, 27018)
# Adjust your local .Renviron or Sys.setenv to point to localhost:3307 etc.
# Then source() the script locally.