FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY web/package.json web/
COPY api/package.json api/
RUN pnpm install --frozen-lockfile
COPY web/ web/
RUN pnpm --filter web build

FROM nginx:alpine
COPY --from=build /app/web/dist/web/browser /usr/share/nginx/html
# nginx:alpine processes *.template files in /etc/nginx/templates/ at startup via envsubst
COPY web/nginx.conf /etc/nginx/templates/default.conf.template
ENV PORT=80
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
