# Base image
FROM node:16.3.0-alpine

# Install JSON Server
RUN \
    echo "==> Install app..."                && \
    npm install -g --production json-server  && \
    echo "==> Remove unused temp..."         && \
    rm -rf /root/.npm                           \
           /usr/lib/node_modules/npm

# Copy fakeAPI.json into the container
COPY fakeAPI.json /fakeAPI.json

# Copy routes file into the container
COPY routes.json /routes.json

# Container listen on port 3000
EXPOSE 3000/tcp

# run the app
ENTRYPOINT ["json-server", "-H", "0.0.0.0", "-w", "fakeAPI.json", "--routes", "routes.json"]