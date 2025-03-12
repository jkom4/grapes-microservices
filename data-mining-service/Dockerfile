FROM python:3.10
LABEL authors="jkom"
# Install R dependency
RUN apt-get update && apt-get install -y r-base

# Install R packages
RUN R -e "install.packages(c('dplyr', 'ggplot2'), repos='http://cran.rstudio.com/')"

# Copy source code
WORKDIR /app
COPY backend/ .
COPY scripts-r/ ./scripts-r
COPY data/ ./data

# Install Python dependancy
RUN pip install -r requirements.txt

# Execute API
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "5000"]
