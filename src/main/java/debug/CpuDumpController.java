package main.java.debug;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import main.java.CPU;
import main.java.Launch;

import java.util.List;

import static main.java.CPU.hex;

public class CpuDumpController {
    // Register labels
    @FXML private Label pcLabel, aLabel, xLabel, yLabel, spLabel, pLabel;

    // Flags
    @FXML private CheckBox nFlag, vFlag, bFlag, dFlag, iFlag, zFlag, cFlag, pausedFlag;

    // Disassembly table
    @FXML private TableView<DisassemblyRow> disasmTable;
    @FXML private TableColumn<DisassemblyRow,String> addrCol, bytesCol, instrCol;

    @FXML
    public void initialize() {
        addrCol.setCellValueFactory(new PropertyValueFactory<>("addr"));
        bytesCol.setCellValueFactory(new PropertyValueFactory<>("bytes"));
        instrCol.setCellValueFactory(new PropertyValueFactory<>("instr"));
    }

    private CPU cpu;
    private Launch launcher;
    private boolean isClosed;

    public void showCpuDump(){
        if(isClosed)
            return;
        pcLabel.setText(hex(cpu.getPC(),4));
        aLabel .setText(hex(cpu.getAccumulator(),2));
        xLabel .setText(hex(cpu.getX(),2));
        yLabel .setText(hex(cpu.getY(),2));
        spLabel.setText(hex(cpu.getSP(),2));
        pLabel .setText(hex(cpu.getStatus(),2));

        nFlag.setSelected(cpu.getFlag('N'));
        vFlag.setSelected(cpu.getFlag('V'));
        bFlag.setSelected(cpu.getFlag('B'));
        dFlag.setSelected(cpu.getFlag('D'));
        iFlag.setSelected(cpu.getFlag('I'));
        zFlag.setSelected(cpu.getFlag('Z'));
        cFlag.setSelected(cpu.getFlag('C'));

        List<DisassemblyRow> rows = launcher.t.getDecoder().disassemble(25);// disassemble 10 upcoming instructions
        Platform.runLater(() -> {
            disasmTable.getItems().setAll(rows);
            if (disasmTable.getSelectionModel().isEmpty()) {
                disasmTable.getSelectionModel().select(0);
                disasmTable.scrollTo(0); // optional but good UX
            }
        });

    }

    public void setLauncher(Launch launch){
        this.launcher = launch;
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    public void markClosed() {
        isClosed = true;
    }

    public void singleStep() {

    }

    public void setPausedFlag(){
        pausedFlag.setSelected(launcher.t.isPaused());
    }

    public void pauseCheckBoxSelected(){
        if (pausedFlag.isSelected()) {
            launcher.t.pause();
        } else {
            launcher.t.unpause();
        }
    }
}
