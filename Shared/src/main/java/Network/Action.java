package Network;

public enum Action {
    CONNECT,       // ← new, sends udpPort right after TCP connect
    LOGIN,
    LOGOUT,
    SEARCH_TRIPS,
    GET_SEATS,
    GET_RESERVATIONS,
    MAKE_RESERVATION,
    CANCEL_RESERVATION,
    OK, //also login logout?
    ERROR
}