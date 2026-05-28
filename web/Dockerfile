FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=build /app/dist/web/browser /usr/share/nginx/html
# nginx:alpine processes *.template files in /etc/nginx/templates/ at startup via envsubst
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
