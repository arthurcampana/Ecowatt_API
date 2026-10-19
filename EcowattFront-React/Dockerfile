# ---- Etapa 1: build dos estáticos com Vite ----
FROM node:20-alpine AS build
WORKDIR /app

# A URL da API é injetada no build (Vite embute VITE_* em tempo de build).
# O browser do usuário acessa o backend pela porta publicada no host.
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Etapa 2: servir os estáticos com Nginx ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
