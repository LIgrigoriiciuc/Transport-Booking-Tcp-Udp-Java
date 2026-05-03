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
   git clone https://github.com/LIgrigoriiciuc/Transport-Booking-Tcp-Udp-Java.git
   cd Transport-Booking-Tcp-Udp-Java
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

### Running the Application

1. **Start the Server** (automatically initializes database on first run):
   ```bash
   ./gradlew :Server:run
   ```
   - The server will automatically execute `DatabaseInit.sql` on first startup
   - No manual database setup required!
   - Subsequent startups use the existing database

2. **Start the Client** (in a new terminal):
   ```bash
   ./gradlew :Client:run
   ```
   - Connects to the server on localhost:65535 by default
   - Shows login screen upon connection

Configure connection settings in `client.properties` (client host/port) and `server.properties` (server port).

### Database Setup

The application uses **SQLite with automatic initialization**:
- On **first server startup**, the `DatabaseInitializer` checks if tables exist
- If tables are missing, it automatically executes `DatabaseInit.sql`
- **No manual steps required!** The database is ready on first run
- Includes sample data: 4 offices, 4 test users, 8 trips, and pre-populated seats with sample reservations

**Sample Test Credentials:**
- Username: `admin` / Password: `admin123`
- Username: `john` / Password: `pass123`
- Username: `mary` / Password: `pass456`
- Username: `andrew` / Password: `pass789`

## Configuration

- **Client**: `Client/src/main/resources/client.properties`
- **Server**: `Server/src/main/resources/server.properties`
- **Logging**: `log4j2.xml` in respective resources directories

### Key Architectural Decisions

#### 1. **Virtual Threads (Java 21)**
- Each client gets a lightweight virtual thread (1KB vs 1MB for traditional threads)

```java
Thread.ofVirtual().start(new ClientHandler(service, client));
```

#### 2. **Facade Pattern for Services**
- `FacadeService` provides single entry point for all business logic
- Hides complexity of multiple service interactions
- Example: a reservation involves multiple services working together

```java
public void bookSeats(long userId, long tripId, List<Integer> seatNumbers) {
    // FacadeService coordinates all services and transaction
}
```

#### 3. **Unit of Work Pattern for Transactions**
- Manages explicit transaction boundaries
- Ensures ACID compliance for reservation operations
- Automatic rollback on errors

```java
transactionManager.execute(() -> {
    // All repository operations happen within transaction
    seat.reserve(reservation);
    reservation.addSeat(seat);
    // Commits on success, rolls back on exception
});
```

#### 4. **TCP + UDP Hybrid Communication**
- **TCP**: Guaranteed delivery for commands (login, search, book)
- **UDP**: Fire-and-forget for notifications (low latency, doesn't need ACK)
- Client listens on both ports simultaneously

#### 5. **Connection Pooling with HikariCP**
- Pre-creates 2-10 connections for efficient database access
- Prevents connection leak and improves throughput
- Thread-local transaction connections for explicit transaction management

#### 6. **No ORM - Direct SQL Queries**
- Full control over query performance
- Simpler transaction management
- Lightweight codebase

## Configuration

- **Client**: `Client/src/main/resources/client.properties`
  - `server.host`: Server hostname (default: localhost)
  - `server.port`: Server port (default: 65535)
  
- **Server**: `Server/src/main/resources/server.properties`
  - `server.port`: Port to listen on (default: 65535)
  
- **Database**: `Server/src/main/resources/app.properties`
  - `db.url`: SQLite database URL (default: jdbc:sqlite:transport.db)

- **Logging**: `log4j2.xml` in both client and server resources directories
  - Configure log levels, output formats, and file paths

## Usage

### Step-by-Step

1. **Start the server** (in terminal 1):
   ```bash
   cd Transport-Booking-Tcp-Udp-Java
   ./gradlew :Server:run
   ```
   - Automatically initializes SQLite database on first run
   - Listens on port 65535

2. **Start one or more clients** (in terminal 2, 3, etc.):
   ```bash
   ./gradlew :Client:run
   ```

3. **Log in** with test credentials:
   - Use any of the sample users (admin, john, mary, andrew)
   - Each has password like `admin123`, `pass123`, etc.

4. **Search for trips**:
   - Filter by destination and date range
   - View all available trips

5. **View and book seats**:
   - Click on a trip to see seat availability
   - Select desired seats (green = available, red = reserved)
   - Confirm booking

6. **Real-time updates**:
   - When any user books seats, all connected clients see the update immediately
   - No need to refresh - notifications arrive via UDP
   - Seat status updates within milliseconds

### Multi-Client Scenario

```
Terminal 1: Server running
Terminal 2: Client A logs in as 'john', searches London trips
Terminal 3: Client B logs in as 'mary', searches London trips

When Client A books seats 1,2,3 on a trip:
  → Server processes booking in transaction
  → Database updated
  → UDP broadcast sent to all clients
  → Client B sees seats 1,2,3 become red (reserved) instantly
  → No polling - events are pushed in real-time!
```

## Project Structure

```
Transport-Booking-Tcp-Udp-Java/
├── Client/
│   ├── src/main/java/GUI/
│   │   ├── SceneManager.java: Manages UI scene transitions
│   │   ├── INavigationListener.java: Interface for scene changes
│   │   └── Controller/: FXML controller classes
│   ├── src/main/java/Network/
│   │   ├── NetworkProxy.java: Main client-server interface
│   │   ├── TcpConnection.java: TCP socket management
│   │   ├── TcpReaderThread.java: Listens for TCP responses
│   │   ├── UdpListenerThread.java: Listens for UDP notifications
│   │   ├── IResponseReceiver.java: Callback for TCP responses
│   │   └── IPushReceiver.java: Callback for UDP push events
│   └── src/main/resources/
│       ├── client.properties: Connection config
│       ├── fxml/: JavaFX layout files
│       └── log4j2.xml: Logging configuration
│
├── Server/
│   ├── src/main/java/
│   │   ├── StartServer.java: Main entry point
│   │   ├── Domain/: Entity classes
│   │   │   ├── User.java, Trip.java, Seat.java, Reservation.java, Office.java
│   │   ├── Network/
│   │   │   ├── ConcurrentServer.java: Accepts connections on port 65535
│   │   │   ├── ClientHandler.java: Processes individual client (virtual thread)
│   │   │   ├── NetworkServiceImpl.java: Routes requests to facade
│   │   │   ├── UdpPusher.java: Broadcasts events to all clients
│   │   │   ├── ConnectionSession.java: Per-client session data
│   │   │   └── DtoUtils.java: JSON serialization helpers
│   │   ├── Service/: Business logic layer
│   │   │   ├── FacadeService.java: Coordinates all services
│   │   │   ├── AuthService.java: Login, password hashing
│   │   │   ├── TripService.java: Trip search & queries
│   │   │   ├── SeatService.java: Seat queries & locking
│   │   │   ├── ReservationService.java: Booking workflow
│   │   │   ├── OfficeService.java: Office management
│   │   │   └── TransactionsLogic/: Transaction management
│   │   ├── Repository/: Data access layer
│   │   │   ├── GenericRepository.java: Base CRUD operations
│   │   │   ├── SeatRepository.java, TripRepository.java, etc.
│   │   └── Util/
│   │       ├── DatabaseConnection.java: HikariCP connection pool
│   │       ├── DatabaseInitializer.java: Auto-executes DatabaseInit.sql
│   │       └── ConnectionHolder.java: Resource wrapper for connections
│   └── src/main/resources/
│       ├── app.properties: Database connection config
│       ├── DatabaseInit.sql: Schema & sample data (auto-executed)
│       ├── server.properties: Server config
│       └── log4j2.xml: Logging configuration
│
├── Shared/
│   ├── src/main/java/Network/
│   │   ├── Dto/: Shared data transfer objects
│   │   │   ├── RequestDto/: Login, Search, Reservation requests
│   │   │   └── ResponseDto/: User, Trip, Seat, Reservation responses
│   │   ├── Packet.java: Protocol wrapper (action + payload)
│   │   └── Constants.java: Shared constants
│   └── src/main/java/Util/
│       └── Validators.java: Input validation
│
└── Gradle Build Files
    ├── build.gradle: Root configuration
    ├── settings.gradle: Multi-module setup
    └── gradle/wrapper/: Gradle wrapper for CI/CD
```

## Database Initialization (Automatic)

**Fully Automatic!**

### How It Works

1. **On Application Start**:
   ```java
   // StartServer.java - before creating repositories
   DatabaseInitializer.initializeDatabase();
   ```

2. **Check Phase**:
   - `DatabaseInitializer` checks if `offices` table exists
   - If tables found → database already initialized, skip setup
   - If tables not found → proceed to initialization

3. **Initialize Phase**:
   - Read `DatabaseInit.sql` from classpath resources
   - Parse SQL statements (split by semicolon)
   - Execute each statement in order:
     - DROP existing tables (safe reset)
     - CREATE new tables with constraints
     - INSERT sample data (offices, users, trips, seats, reservations)
   - Auto-commit transaction

4. **Result**:
   - 4 offices (London, Manchester, Birmingham, Liverpool)
   - 4 test users with BCrypt-hashed passwords
   - 8 trips across different dates
   - 144 seats total (18 per trip)
   - Sample reservations showing booking examples

## Design Patterns

### 1. **Observer Pattern** (Push Notifications)
- **Problem**: Multiple clients need to know immediately when seat status changes
- **Solution**: UdpPusher acts as subject, all connected clients are observers
- **Implementation**: When reservation commits, UdpPusher broadcasts UDP packet to all clients listening on port 65534

### 2. **Repository Pattern** (Data Access Abstraction)
- **Problem**: Business logic shouldn't depend on database implementation
- **Solution**: Repositories abstract all database queries
- **Implementation**: 
  - `GenericRepository<T>`: Base class with generic CRUD operations
  - Specific repos: `SeatRepository`, `TripRepository`, `UserRepository`, etc.
  - Services talk to repos, not directly to database

```java
// Service uses repository, not SQL directly
public List<Trip> searchByDestination(String destination) {
    return tripRepository.findByDestination(destination);
}
```

### 3. **Service Layer Pattern** (Business Logic Organization)
- **Problem**: Business rules scattered across controllers
- **Solution**: Dedicated service classes for each domain
- **Implementation**:
  - `AuthService`: Login, password validation (BCrypt)
  - `TripService`: Trip queries and searching
  - `SeatService`: Seat availability and locking
  - `ReservationService`: Booking workflow
  - `OfficeService`: Office management

### 4. **Facade Pattern** (Simplified Interface)
- **Problem**: Clients need to coordinate multiple services for complex operations
- **Solution**: FacadeService provides single entry point
- **Implementation**: 
  - NetworkServiceImpl calls FacadeService for all requests
  - FacadeService internally coordinates multiple services and transactions
  - Example: `bookSeats()` involves SeatService, ReservationService, and TransactionManager

```java
// Facade hides complexity
facadeService.bookSeats(userId, tripId, seatNumbers);

// Instead of client doing:
transactionManager.execute(() -> {
    seat.lock(seatNumbers);
    reservation.create(userId);
    seats.reserve(seatNumbers, reservation);
    // ... more manual steps
});
```

### 5. **Unit of Work Pattern** (Transaction Management)
- **Problem**: Multiple repository operations must succeed or all fail
- **Solution**: TransactionManager coordinates all changes within a transaction
- **Implementation**: 
  - `TransactionManager.execute()` starts transaction
  - Thread-local connection binding via `DatabaseConnection.bindConnection()`
  - Automatic rollback on exception
  - Automatic commit on success

```java
transactionManager.execute(() -> {
    // All operations here use same connection
    // All succeed or all fail together
    seat.update(seatId, "reserved");
    reservation.insert(reservationData);
    // One exception → full rollback
});
```

### 6. **Builder/Fluent Pattern** (DTOs)
- **Problem**: Complex objects with many fields
- **Solution**: DTOs use builder pattern for smooth serialization
- **Implementation**: Gson handles complex DTO hierarchies
  - RequestDto: LoginDTO, TripSearchDTO, ReservationDTO
  - ResponseDto: UserDTO, TripDTO, SeatDTO, ReservationDTO

### 7. **Singleton Pattern with Thread-Local** (Connection Management)
- **Problem**: Global database connection management with transaction isolation
- **Solution**: HikariDataSource (singleton) + ThreadLocal for transaction connections
- **Implementation**:
  - `DatabaseConnection.dataSource`: Singleton pool
  - `DatabaseConnection.transactionConnection`: Thread-local for explicit transactions
  - Virtual threads each get their own connection context

## Performance Architecture

### Virtual Threads (Java 21)
```
Traditional Approach:
├─ Thread 1 (1MB memory) → Client A
├─ Thread 2 (1MB memory) → Client B
├─ Thread 3 (1MB memory) → Client C
└─ ...10,000 threads × 1MB = 10GB RAM!

Virtual Threads Approach:
├─ Virtual Thread 1 (few KB) → Client A
├─ Virtual Thread 2 (few KB) → Client B
├─ Virtual Thread 3 (few KB) → Client C
└─ ...10,000 threads × 5KB = 50MB RAM!

Result: 1000x better memory efficiency!
```

*Code*:
```java
// ConcurrentServer.java
while (true) {
    Socket client = serverSocket.accept();
    // ~1KB per thread, NOT 1MB!
    Thread.ofVirtual().start(new ClientHandler(service, client));
}
```

### Connection Pooling
```
Without HikariCP:
├─ Client 1 creates connection → wait
├─ Client 2 creates connection → wait
├─ Client 3 creates connection → wait
└─ Connection overhead dominates!

With HikariCP (2-10 connections pre-created):
├─ Client 1 → Get from pool (instant)
├─ Client 2 → Get from pool (instant)
├─ Client 3 → Get from pool (instant)
└─ Reuse eliminates creation overhead
```

*Config*:
```java
config.setMaximumPoolSize(10);      // Max 10 connections
config.setMinimumIdle(2);            // Keep 2 always ready
config.setIdleTimeout(600_000);      // Clean up after 10 minutes
```

### Transaction Isolation
```
Scenario: Two clients book same seats simultaneously

Traditional Approach (no locking):
├─ Client A reads seats 1,2,3 → available
├─ Client B reads seats 1,2,3 → available
├─ Client A books seats 1,2,3
├─ Client B books seats 1,2,3       ← DOUBLE BOOKING!

Our Approach (transactional with row locks):
├─ Client A: BEGIN TRANSACTION
│  ├─ Reads and locks seats 1,2,3
│  ├─ Books them
│  ├─ COMMIT
├─ Client B: BEGIN TRANSACTION
│  ├─ Tries to read seats 1,2,3 → LOCKED (waits)
│  ├─ After A commits, reads available seats
│  ├─ Books different seats
│  ├─ COMMIT
└─ No double booking!
```

### Notification Speed
```
UDP Push Notifications (vs. REST polling):

Polling Approach:
├─ Client polls every 5 seconds: "Any new reservations?"
├─ Seat changes instantly, but client doesn't know for 5 seconds
├─ Network overhead: N requests/minute

Push Approach (UDP):
├─ Reservation happens
├─ Server broadcasts immediately via UDP
├─ All clients notified within milliseconds
├─ Network overhead: only on events (not periodic)
└─ Real-time user experience!
```

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'Add amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Create a Pull Request with description of changes.

### Areas for Enhancement
- Add unit tests (JUnit 5)
- Implement SSL/TLS for TCP connections
- Add database replication support
- Implement authentication tokens (JWT)
- Add REST API alongside native protocol
- Improve error handling and validation
- Add performance metrics collection
- Create web-based client (Spring Boot + React)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built as a comprehensive demonstration of:
  - Java 21 Virtual Threads for efficient concurrency
  - TCP/UDP networking protocols
  - Transaction management and ACID compliance
  - Design patterns in practice (Observer, Repository, Facade, Unit of Work)
  - Connection pooling and resource management
  - JavaFX desktop GUI development
  - Multi-threaded server architecture
  
- Inspired by real-world transport reservation systems like FlixBus, Greyhound, and train booking platforms.
