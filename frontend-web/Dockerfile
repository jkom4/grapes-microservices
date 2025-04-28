FROM node:18 AS build
LABEL authors="jkom"
WORKDIR /app
COPY package.json package-lock.json ./
ARG REACT_APP_BREVO_API_KEY
ARG REACT_APP_BREVO_LIST_ID

RUN REACT_APP_BREVO_API_KEY=${REACT_APP_BREVO_API_KEY} \
    REACT_APP_BREVO_LIST_ID=${REACT_APP_BREVO_LIST_ID} \
    npm ci --legacy-peer-deps
COPY . .
RUN npm run build && ls

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY --from=build /app/public/chat_exe /usr/share/nginx/html/chat_exe
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]