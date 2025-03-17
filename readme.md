## Getting Started

This project is an orchestrator for your Minecraft servers that can be run in Docker.\
It is aimed to help reduce a server's power draw when nobody is connected, suspending the instance.\

### Prerequisites

Other than Docker, there is nothing else that you require.\
You can check the docker-compose.yaml and config.json files from this repository as an example to get yourself started.

### Running

This guide will use the default network name I specified in both docker-compose.yaml and config.json files.

1. Run `docker network create mcdockerseer`
2. Update `config.json` for your first server with the absolute path to it.
3. Update `privatePort` and `publicPort` to your liking (private: the actual server's port; public: the port you will be
   connecting to)
4. Set `max-tick-time=-1` inside all servers' `server.properties` files.
5. Run `docker compose up -d`

### Contributing

As this is an open source project, feel free to create a pull request with your own code or an issue requesting a new
functionality.