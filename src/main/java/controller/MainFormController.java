package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.text.NumberFormat;

public class MainFormController {


    public JFXButton btnSelectSource;
    public JFXTextField txtName;
    public JFXTextField txtSize;
    public JFXButton btnSelectDestination;
    public JFXTextField txtDestination;
    public ProgressBar pgrCopy;
    public JFXButton btnCopy;
    public ProgressIndicator pgrcrcl;
    public Label lblCopySize;
    public Label lblPre;
    public ProgressBar pgbTotal;
    public Label lblPreTot;
    public Label lblCopySizeTot;


    private File sourceDirectory;

    private double folderSize;


    private File destinationFolder;

    private int totalFileRead;

    public void btnSelectSourceOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");

        sourceDirectory = directoryChooser.showDialog(btnSelectSource.getScene().getWindow());

        folderSize =0;

        if (sourceDirectory != null) {
            txtName.setPromptText("Selected Directory :");
            txtName.setText(sourceDirectory.getAbsolutePath());

            File[] files = sourceDirectory.listFiles();
            for (File file : files) {
                folderSize+=file.length();
            }

            txtSize.setText(String.valueOf(formatNumber(folderSize/1024.00))+ " Kb");


            btnCopy.setDisable(false);


        } else {
            txtName.setPromptText(null);
            txtName.setText("Please Select a file");
            return;

        }




    }

    public void btnSelectDestinationOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select destination folder");

        destinationFolder = directoryChooser.showDialog(btnSelectSource.getScene().getWindow());



        txtDestination.setText(String.valueOf(destinationFolder));



    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {


        var task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                File copyFile = new File(destinationFolder, sourceDirectory.getName() + "-Copy");
                if (!copyFile.exists()) {
                    copyFile.mkdir();
                }


                File[] files = sourceDirectory.listFiles();
                totalFileRead =0;

                for (File file : files) {




                    File newFile = new File(copyFile, file.getName());
                    newFile.createNewFile();


                    FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    long filesize = file.length();
                    int totalRead = 0;





                    while (true) {
                        byte[] buffer = new byte[1024 * 10];
                        int read = bis.read(buffer);
                        totalRead += read;
                        if (read == -1) break;
                        bos.write(buffer,0,read);
                        updateProgress(totalRead,file.length());





                    }
                    totalFileRead+=totalRead;



                    updateProgress(totalRead,folderSize);



                    bos.close();
                    bis.close();


                }
                updateProgress(folderSize,folderSize);


                return null;
            }
        };

        task.workDoneProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number curwork) {

                System.out.println("curr work" + curwork);
                System.out.println("tot work" + task.getTotalWork());
                pgrCopy.setProgress(curwork.doubleValue() / task.getTotalWork());
                lblPre.setText(("Progress: " + formatNumber(task.getProgress() * 100) + "%"));
                lblCopySize.setText(formatNumber(task.getWorkDone() / 1024.0) + " / " + formatNumber(task.getTotalWork() / 1024.0) + " Kb");
                pgbTotal.setProgress(totalFileRead/folderSize);
                lblCopySizeTot.setText(formatNumber(totalFileRead/1024.00)+" / "+ formatNumber(folderSize/1024.00) +" Kb");
                lblPreTot.setText(("Total Progress: " + (formatNumber((totalFileRead/folderSize)* 100))+ "%"));

            }
        });


        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                pgrCopy.setProgress(0);
                lblCopySize.setText("0 / 0 Kb");
                lblPre.setText("Progress: 0%");
                new Alert(Alert.AlertType.INFORMATION, "Copied Successfully", ButtonType.OK).showAndWait();

                pgbTotal.setProgress(0);

                txtDestination.clear();
                txtSize.clear();
                txtName.clear();


                lblCopySizeTot.setText("0 / 0 Kb");
                lblPreTot.setText("Progress: 0%");
                sourceDirectory = null;
                destinationFolder = null;


            }
        });

        new Thread(task).start();


    }

    private String formatNumber(double input) {
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setGroupingUsed(true);
        numberInstance.setMaximumFractionDigits(2);
        numberInstance.setMinimumFractionDigits(2);
        return numberInstance.format(input);
    }
}
