FROM rocker/shiny-verse:latest

# --- 1. Installer les dépendances système supplémentaires ---
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

# --- 2. Copier le script R qui installe tous les packages ---
COPY install_packages.R /tmp/install_packages.R

# --- 3. Lancer l'installation dans une R session propre ---
RUN Rscript /tmp/install_packages.R

# --- 4. Copier ton application ---
WORKDIR /srv/shiny-server/app
COPY global.R .
COPY ui.R .
COPY server.R .
COPY load_data.R .
COPY generate_and_save_data_refactored.R .
COPY modules ./modules

# COPY Rprofile.site /usr/local/lib/R/etc/Rprofile.site


# --- 5. Propriétés et port ---
RUN chown -R shiny:shiny .
EXPOSE 3838

CMD ["R", "-e", "shiny::runApp('/srv/shiny-server/app', host='0.0.0.0', port=3838)"]
