FROM node:18 AS build
LABEL authors="jkom"
WORKDIR /app
COPY package.json package-lock.json ./
ARG REACT_APP_API_URL_SALES
ENV REACT_APP_API_URL_SALES=$REACT_APP_API_URL_SALES
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build && ls

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]