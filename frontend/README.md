# NetHelt Frontend

Frontend application for the NetHelt platform built with Angular.

## About

This repository contains the web client responsible for:

- User authentication
- User profile management
- Notifications
- Networks and devices management
- Metrics visualization and statistics

The application communicates with the NetHelt Backend via REST API.

## Tech Stack

- Angular 21
- TypeScript
- Angular Router
- RxJS
- SCSS

## Getting Started

### Prerequisites

- Node.js 22 or newer
- npm
- Angular CLI 21

### Installation

Install project dependencies:

```bash
npm install
```

### Configuration

Configure the environment variables in:

```text
src/environments/
```

Update the appropriate environment file (e.g. `environment.ts`) to point to your NetHelt web-api instance.

### Running the application

Start the development server:

```bash
ng serve
```

The application will be available at:

```text
http://localhost:4200
```

### Build

Create a production build:

```bash
ng build
```

## Backend

This application requires the NetHelt web-api module to be running.
