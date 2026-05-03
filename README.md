# Transport-Booking-Tcp-Udp-Java

A Java desktop client-server application for managing transport seat reservations. The server handles concurrent clients using virtual threads and notifies connected clients of reservation changes via UDP. The client is a JavaFX application communicating over TCP.

**Stack:** Java 21, JavaFX, TCP + UDP sockets, Gson, SQLite, HikariCP, BCrypt, Log4j2.

---

## Architecture

**Client** — `NetworkProxy` owns the TCP connection. A `TcpReaderThread` runs in the background and enqueues every server response into a `BlockingQueue<Packet>`. All user-triggered requests call `sendAndReceive()` on the JavaFX Application Thread, which sends a packet and blocks on `responses.take()`. A separate `UdpListenerThread` receives push pings from the server and calls the registered `onPush` callback, which queues a refresh onto the JavaFX thread via `Platform.runLater`.

**Server** — `ConcurrentServer` accepts connections and spawns a virtual thread per client (`Thread.ofVirtual()`). Each client is handled by `ClientHandler`, which processes requests sequentially and delegates to `NetworkServiceImpl`. After a reservation change, `NetworkServiceImpl` sends a UDP datagram to every connected client's registered UDP port via `UdpPusher`.

**Shared** — DTOs, `Packet`, `Action` enum, and `PacketFactory` for building typed request/response packets serialized with Gson.

---

## Key Technical Decisions

**TCP + UDP channel separation** — TCP handles all request/response communication. UDP carries server-initiated push notifications. This separation keeps the `BlockingQueue` assumption safe: since the JavaFX thread serializes all user-triggered TCP requests, responses always arrive in order and `responses.take()` always gets the right packet. A UDP ping never lands in the TCP response queue and can never corrupt an in-flight exchange.

**Single BlockingQueue without correlation IDs** — this works precisely because JavaFX is single-threaded for UI interactions. A push-triggered refresh is queued via `Platform.runLater` and only runs after the current user action completes. Two TCP requests cannot overlap, so the queue ordering assumption always holds.

**Dumb UDP ping** — the push payload carries no data. The client re-fetches only what it needs on the JavaFX thread after the ping arrives. Adding data to the ping would require synchronizing it with the TCP response queue; keeping it empty avoids the problem entirely.

**Double-booking prevention** — `reservationLock` in `NetworkServiceImpl` serializes reservation and cancellation operations. Seat availability is checked and updated within the same synchronized block, preventing races between concurrent virtual threads handling different clients.

**Unit of Work transactions** — `TransactionManager.execute()` opens a connection, begins a transaction, and binds both to a `ThreadLocal`. All repository operations on that thread transparently join the transaction via `DatabaseConnection.getConnection()` without being passed a connection explicitly. On success it commits; any exception triggers an automatic rollback. This means `makeReservationForSeats` — which touches both the reservation table and multiple seat rows — either fully succeeds or leaves the database unchanged.

**Virtual threads** — each client connection runs on a Java 21 virtual thread. Blocking on I/O or database operations parks the virtual thread rather than blocking an OS thread, making the server practical for many concurrent clients with minimal memory overhead.

**HikariCP connection pool** — HikariCP pre-warms connections and handles lifecycle. The pool integrates with the thread-local transaction pattern: within a transaction the bound connection is reused directly; outside a transaction repositories borrow a pooled connection and return it on `close()`.

**BCrypt for passwords** — passwords are stored and verified with BCrypt. The cost factor makes brute-force attacks expensive without noticeable login latency.

---

## Project Structure

```
Server/
  Network/    — ConcurrentServer, ClientHandler, NetworkServiceImpl, UdpPusher
  Service/    — FacadeService, AuthService, TripService, SeatService, ReservationService
  Repository/ — GenericRepository<T>, concrete repos
  Util/       — DatabaseConnection, TransactionManager, DatabaseInitializer

Client/
  Network/    — NetworkProxy, TcpConnection, TcpReaderThread, UdpListenerThread
  GUI/        — SceneManager, LoginController, MainWindowController

Shared/
  Dto/        — Request and response DTOs
  Network/    — Packet, Action, PacketFactory
```

---

## Setup

Prerequisites: Java 21. Gradle wrapper included — no local Gradle install needed.

```bash
git clone https://github.com/LIgrigoriiciuc/Transport-Booking-Tcp-Udp-Java.git
cd Transport-Booking-Tcp-Udp-Java
./gradlew build
```

Run the server (initializes SQLite on first start):
```bash
./gradlew :Server:run
```

Run the client:
```bash
./gradlew :Client:run
```

Connection config in `client.properties` and `server.properties`. Logging config in `log4j2.xml` in each module's resources.

---

## Sample Credentials

| Username | Password  |
|----------|-----------|
| admin    | admin123  |
| john     | pass123   |
| mary     | pass456   |
| andrew   | pass789   |

## Known Limitations
- No SSL/TLS — traffic is unencrypted over the wire
- Single server instance — no failover or horizontal scaling
- SQLite chosen for simplicity — would need Postgres for production load