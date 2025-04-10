
# Use Python Image for Backend
FROM python:3.10
LABEL authors="jkom"
WORKDIR /app
COPY backend/ .

# Execute API
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8096"]


# Use an official R base image
FROM rocker/verse:latest

# Set the working directory
WORKDIR /app

# Copy the R scripts and data into the container
COPY scripts-r/generate_data/generate_transaction_data.R /app/scripts-r/generate_data/
#COPY data/ /app/data/

# Install R packages
RUN R -e "install.packages(c('dplyr', 'ggplot2', 'lubridate', 'tidyr', 'stringi', 'mongolite'), repos='http://cran.rstudio.com/')"
# Install Python dependancy
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the entrypoint script
COPY entrypoint.R /app/entrypoint.R

# Set the entrypoint to run the R script
ENTRYPOINT ["Rscript", "entrypoint.R"]
# Execute API
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "9090"]
