package sed.sample.sqlitepersontableviewexampleusingtasks;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.MaskerPane;

public class PrimaryController implements Initializable
{

    @FXML
    private Label lblLastAction;
    @FXML
    private TableColumn<Person, String> tcFirstName, tcLastName;
    @FXML
    private TableColumn<Person, Integer> tcId, tcAge;
    @FXML
    private TableView<Person> tvMain;
    @FXML
    private Spinner<Integer> spAge;
    @FXML
    private TextField tfFirstName, tfLastName;
    @FXML
    private MaskerPane mpTableView;
    @FXML
    private Button btnAddPerson, btnDeletePerson;
//    @FXML
//    private FontAwesomeIconView faivAddPerson, faivDeletePerson;

    DBHandler dBHandler;

    @FXML
    private void handleBtnAddPerson(ActionEvent actionEvent)
    {
        Person person = new Person(tfFirstName.getText().trim(), tfLastName.getText().trim(), spAge.getValue());
        System.out.println("adding person");
        Service<Boolean> addNewPersonService = dBHandler.addNewPerson(person);
        addNewPersonService.setOnSucceeded(onSucceededEvent -> {
            if (addNewPersonService.getValue()) {
                //Update TableView
                Service<Integer> getLastIdFromPersonTableService = dBHandler.getLastIDFromPersonTable();
                getLastIdFromPersonTableService.setOnSucceeded((t) -> {
                    person.setId(getLastIdFromPersonTableService.getValue());//Update the Person's ID to the AutoIncrement ID in the Database
                    tvMain.getItems().add(person);//Add new Person to the TableView
                    lblLastAction.setText(person.getFirstName() + " added!");
                });
                getLastIdFromPersonTableService.start();

            }
            else {
                lblLastAction.setText(person.getFirstName() + " failed to add!");
            }
        });
        addNewPersonService.start();
    }

    @FXML
    private void handleBtnDeletePerson(ActionEvent actionEvent)
    {
        Person person = tvMain.getSelectionModel().getSelectedItem();

        Service<Boolean> deletePersonService = dBHandler.deletePerson(person);
        deletePersonService.setOnSucceeded((onSucceededEvent) -> {
            if (deletePersonService.getValue()) {
                //Update TableView
                tvMain.getItems().remove(person);//Remove Person from the TableView
                lblLastAction.setText(person.getFirstName() + " deleted!");
            }
            else {
                lblLastAction.setText(person.getFirstName() + " failed to delete!");
            }
        });
        deletePersonService.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
        tcId.setCellValueFactory(new PropertyValueFactory("id"));
        tcFirstName.setCellValueFactory(new PropertyValueFactory("firstName"));
        tcLastName.setCellValueFactory(new PropertyValueFactory("lastName"));
        tcAge.setCellValueFactory(new PropertyValueFactory("age"));

        tfFirstName.setDisable(true);
        tfLastName.setDisable(true);
        spAge.setDisable(true);

        dBHandler = new DBHandler();

        Platform.runLater(() -> {
            mpTableView.setDisable(false);
            mpTableView.toFront();
            Service<List<Person>> getDBDataService = dBHandler.getDBData();
            getDBDataService.setOnSucceeded(onSucceededEvent -> {
                tvMain.getItems().setAll(getDBDataService.getValue());
                mpTableView.setDisable(true);
                mpTableView.toBack();
                tfFirstName.setDisable(false);
                tfLastName.setDisable(false);
                spAge.setDisable(false);
            });
            getDBDataService.start();
        });

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 125, 25);
        spAge.setValueFactory(valueFactory);

        btnAddPerson.disableProperty().bind(Bindings.isEmpty(tfFirstName.textProperty()).or(tfLastName.textProperty().isEmpty()));
        btnDeletePerson.disableProperty().bind(tvMain.getSelectionModel().selectedItemProperty().isNull());
    }

}
