FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY web/package.json web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY web/ .
RUN pnpm build

FROM nginx:alpine
COPY --from=build /app/dist/web/browser /usr/share/nginx/html
# nginx:alpine processes *.template files in /etc/nginx/templates/ at startup via envsubst
COPY web/nginx.conf /etc/nginx/templates/default.conf.template
ENV PORT=80
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
