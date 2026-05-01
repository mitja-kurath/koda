FROM node:22-alpine AS build
WORKDIR /app
RUN npm install -g pnpm
COPY package.json ./
RUN pnpm install --no-frozen-lockfile
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=build /app/dist/web/browser /usr/share/nginx/html
EXPOSE 80
