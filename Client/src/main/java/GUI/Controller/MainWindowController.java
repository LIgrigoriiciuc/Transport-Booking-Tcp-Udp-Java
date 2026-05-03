package GUI.Controller;
import GUI.INavigationListener;
import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import Network.INetworkService;
import Network.NetworkProxy;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainWindowController implements BaseController {
    @FXML private TableView<TripDTO> tripTable;
    @FXML private TableColumn<TripDTO, String> colDest;
    @FXML private TableColumn<TripDTO, String> colTime;
    @FXML private TableColumn<TripDTO, String> colBus;
    @FXML private TableColumn<TripDTO, String> colFreeSeats;
    @FXML private GridPane seatGrid;
    @FXML private TextField destFilter, startTime, endTime, clientNameField;
    @FXML private ListView<ReservationDTO> resList;

    private INetworkService service;
    private INavigationListener navigation;
    private UserDTO currentUser;

    private final List<SeatDTO> selectedSeats = new ArrayList<>();
    private TripDTO selectedTrip;
    private static final double SEAT_SIZE = 52;

    private static final String STYLE_FREE = "-fx-background-radius: 4;";
    private static final String STYLE_SELECTED = "-fx-background-color: #607d8b; -fx-text-fill: white; -fx-background-radius: 4;";
    private static final String STYLE_RESERVED = "-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 4;";

    @FXML
    public void initialize() {
        colDest.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDestination()));
        colTime.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTime()));
        colBus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBusNumber()));
        colFreeSeats.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFreeSeats() + " free"));

        tripTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (oldVal == null || !newVal.getId().equals(oldVal.getId()))) {
                selectedTrip = newVal; // only fetch seats when the trip actually changed
                drawSeats(newVal.getId(), Set.of());
            }
        });

        resList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ReservationDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getClientName()
                            + " | " + item.getReservationTime()
                            + " | seats: " + item.getSeatNumbers()
                            + " | by: " + item.getUserUsername());
                }
            }
        });
    }

    @Override
    public void setService(INetworkService service) {
        this.service = service;
        if (service instanceof NetworkProxy proxy) {
            proxy.setOnPush(() -> javafx.application.Platform.runLater(() -> {
                refreshSeats();
                refreshReservations();
            }));
        }
        refreshTrips();
        refreshReservations();
    }

    @Override
    public void setNavigationListener(INavigationListener listener) {
        this.navigation = listener;
    }

    @Override
    public void cleanup() {
        if (service instanceof NetworkProxy proxy) {
            proxy.setOnPush(null);
        }
    }

    public void setCurrentUser(UserDTO user) {
        this.currentUser = user;
    }

    private void drawSeats(Long tripId, Set<Long> keepSelectedIds) {
        seatGrid.getChildren().clear();
        selectedSeats.clear();

        List<SeatDTO> seats = service.getSeatsForTrip(new GetSeatsDTO(tripId));
        seats.sort((a, b) -> Integer.compare(a.getNumber(), b.getNumber()));

        int cols = 3;
        int freeCount = 0;

        for (int i = 0; i < seats.size(); i++) {
            SeatDTO seat = seats.get(i);
            Button btn = new Button(String.valueOf(seat.getNumber()));
            btn.setMinSize(SEAT_SIZE, SEAT_SIZE);
            btn.setMaxSize(SEAT_SIZE, SEAT_SIZE);

            if (seat.isReserved()) {
                btn.setDisable(true);
                btn.setStyle(STYLE_RESERVED);
                btn.setTooltip(new Tooltip("Reservation #" + seat.getReservationId()));
            } else {
                freeCount++;
                if (keepSelectedIds.contains(seat.getId())) {
                    selectedSeats.add(seat); // restore previous selection
                    btn.setStyle(STYLE_SELECTED);
                } else {
                    btn.setStyle(STYLE_FREE);
                }
                btn.setOnAction(e -> toggleSeat(btn, seat));
            }

            seatGrid.add(btn, i % cols, i / cols);
        }
        if (selectedTrip != null) {
            selectedTrip.setFreeSeats(freeCount);
            tripTable.refresh();
        }
    }

    private void toggleSeat(Button btn, SeatDTO seat) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            btn.setStyle(STYLE_FREE);
        } else {
            selectedSeats.add(seat);
            btn.setStyle(STYLE_SELECTED);
        }
    }

    private void refreshSeats() {
        if (selectedTrip == null) return;
        Set<Long> keep = selectedSeats.stream()
                .map(SeatDTO::getId)
                .collect(Collectors.toSet());
        drawSeats(selectedTrip.getId(), keep);
    }


    @FXML
    private void handleReserve() {
        if (selectedTrip == null)    {
            showAlert("No trip selected", "Select a trip first.");
            return; }
        if (selectedSeats.isEmpty()) {
            showAlert("No seats selected","Select at least one seat.");
            return; }
        String name = clientNameField.getText().trim();
        if (name.isBlank())          {
            showAlert("Missing name","Enter client name.");
            return; }

        List<Long> seatIds = selectedSeats.stream().map(SeatDTO::getId).toList();
        try {
            service.makeReservation(new MakeReservationDTO(name, seatIds, currentUser.getId()));
            clientNameField.clear();
            drawSeats(selectedTrip.getId(), Set.of()); // gui update, push triggers it again
        } catch (Exception e) {
            Set<Long> keep = selectedSeats.stream()
                    .map(SeatDTO::getId)
                    .collect(Collectors.toSet());
            drawSeats(selectedTrip.getId(), keep);
            showAlert("Reservation Failed", e.getMessage());
        }
    }

    @FXML
    private void handleCancelReservation() {
        ReservationDTO selected = resList.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Nothing selected", "Select a reservation to cancel."); return; }
        try {
            service.cancelReservation(new CancelReservationDTO(selected.getId()));
        } catch (Exception e) {
            showAlert("Cancel Failed", e.getMessage());
        }
    }

    @FXML
    private void handleFilter() {
        String dest = destFilter.getText();
        String from = startTime.getText();
        String to   = endTime.getText();
        try {
            List<TripDTO> filtered = service.searchTrips(new SearchTripsDTO(dest, from, to));
            tripTable.getItems().setAll(filtered);
        } catch (Exception e) {
            showAlert("Filter Error", "Dates must be: yyyy-MM-dd HH:mm");
        }
    }

    @FXML
    private void handleReset() {
        destFilter.clear();
        startTime.clear();
        endTime.clear();
        refreshTrips();
    }

    @FXML
    private void handleLogout() {
        cleanup();
        service.logout(new LogoutDTO(currentUser.getId()));
        navigation.onLogout();
    }

    private void refreshTrips() {
        tripTable.getItems().setAll(service.searchTrips(new SearchTripsDTO("", null, null)));
    }

    private void refreshReservations() {
        resList.getItems().setAll(service.getAllReservations());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}