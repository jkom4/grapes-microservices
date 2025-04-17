FROM rocker/shiny-verse:latest

# --- 1. Install additional system dependencies ---
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends \
    libmariadb-dev \
    libsasl2-dev \
    libssl-dev \
    libzip-dev \
    libcurl4-openssl-dev \
    libxml2-dev \
    libxt-dev \
    libx11-dev \
    libgl1 \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# --- 2. Copy the R script that installs all packages ---
COPY shiny-appv3/Scripts-r/install_packages.R /tmp/install_packages.R

# --- 3. Launch installation in a clean R session ---
RUN Rscript /tmp/install_packages.R
RUN R -e "install.packages(c('FactoMineR', 'factoextra','RMySQL'))"

# --- 4. Copy your Application ---
WORKDIR /srv/shiny-server/app
COPY shiny-appv3/Scripts-r/global.R .
COPY shiny-appv3/ui.R .
COPY shiny-appv3/server.R .
COPY shiny-appv3/Scripts-r/load_data.R .
COPY shiny-appv3/Scripts-r/generate_and_save_data_refactored.R .
COPY shiny-appv3/modules ./modules

# COPY Rprofile.site /usr/local/lib/R/etc/Rprofile.site


# ---  Properties and port---
RUN chown -R shiny:shiny .
EXPOSE 3838

CMD ["R", "-e", "shiny::runApp('/srv/shiny-server/app', host='0.0.0.0', port=3838)"]

