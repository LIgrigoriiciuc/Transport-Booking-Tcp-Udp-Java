# Transport-Booking-Tcp-Udp-Java

A robust desktop client-server reservation system for a transport company, developed in Java. This application facilitates seat reservations for trips, with real-time notifications to all connected clients upon reservation changes. It leverages TCP for reliable communication and UDP for efficient push notifications.

## Features

- **User Authentication**: Secure login system with password hashing using BCrypt.
- **Trip Search and Booking**: Search trips by destination and date range, view available seats, and make reservations.
- **Real-Time Notifications**: UDP-based push notifications inform all clients instantly about reservation updates.
- **Multi-Client Support**: Concurrent server handling multiple clients using virtual threads.
- **Database Integration**: SQLite database with connection pooling via HikariCP.
- **Transaction Management**: ACID-compliant transactions for reservation operations.
- **Modular Architecture**: Clean separation of concerns with Client, Server, and Shared modules.
- **Logging**: Comprehensive logging with Log4j2 for debugging and monitoring.

## Technologies Used

- **Language**: Java 21
- **Frameworks/Libraries**:
  - JavaFX for the desktop client UI
  - Gson for JSON serialization
  - HikariCP for database connection pooling
  - BCrypt for password hashing
  - Log4j2 for logging
- **Database**: SQLite
- **Networking**: TCP for client-server communication, UDP for notifications
- **Build Tool**: Gradle with multi-module setup

## Architecture

The project is structured into three main modules:

- **Client**: JavaFX-based GUI application handling user interactions, network communication with the server.
- **Server**: Multi-threaded server managing business logic, database operations, and client connections.
- **Shared**: Common utilities, DTOs, and network protocols shared between client and server.

### Key Components

- **Network Layer**: Custom TCP/UDP implementation for communication.
- **Service Layer**: Business logic services (Auth, Trip, Seat, Reservation).
- **Repository Layer**: Data access objects with generic CRUD operations.
- **Transaction Logic**: Unit of Work pattern for transaction management.

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 7.0+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/Transport-Booking-Tcp-Udp-Java.git
   cd Transport-Booking-Tcp-Udp-Java
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

### Running the Application

1. **Start the Server**:
   ```bash
   ./gradlew :Server:run
   ```

2. **Start the Client** (in a new terminal):
   ```bash
   ./gradlew :Client:run
   ```

The client will connect to the server on localhost:65535 by default. Configure connection settings in `client.properties` and `server.properties`.

### Database Setup

The application uses SQLite. On first run, the database is initialized with the schema from `DatabaseInit.sql`.

## Configuration

- **Client**: `Client/src/main/resources/client.properties`
- **Server**: `Server/src/main/resources/server.properties`
- **Logging**: `log4j2.xml` in respective resources directories

## Usage

1. Launch the client application.
2. Log in with valid credentials.
3. Search for trips by destination and date.
4. Select a trip and view available seats.
5. Make a reservation for selected seats.
6. Receive real-time notifications of reservation changes.

## Project Structure

```
Transport-Booking-Tcp-Udp-Java/
├── Client/
│   ├── src/main/java/GUI/
│   ├── src/main/java/Network/
│   └── src/main/resources/
├── Server/
│   ├── src/main/java/Domain/
│   ├── src/main/java/Network/
│   ├── src/main/java/Repository/
│   ├── src/main/java/Service/
│   └── src/main/java/Util/
├── Shared/
│   ├── src/main/java/Network/
│   └── src/main/java/Util/
└── build.gradle
```

## Design Patterns

- **Observer Pattern**: For push notifications.
- **Repository Pattern**: For data access abstraction.
- **Service Layer Pattern**: For business logic encapsulation.
- **Facade Pattern**: For simplified interface to complex subsystems.
- **Unit of Work**: For transaction management.

## Testing

Run tests with:
```bash
./gradlew test
```

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Push to the branch.
5. Create a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built as a demonstration of Java networking, concurrency, and GUI development skills.
- Inspired by real-world transport reservation systems.
