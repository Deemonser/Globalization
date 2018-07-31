package cn.deemons.plugin.ui;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.logging.Logger;
import javax.swing.*;
import java.awt.event.*;

public class ProcessDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea1;

    public ProcessDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.setEnabled(false);
        textArea1.setEditable(false);
        textArea1.setText("waiting.....");
        setTitle("ProcessDialog");
        textArea1.setVisible(true);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    public void setFinishEnable() {
        buttonOK.setEnabled(true);
    }

    public void addString(String string) {
        textArea1.append("\n" + string);
    }


    static ProcessDialog dialog = new ProcessDialog();

    public static void main(String[] args) {

        dialog.addString("456");
        dialog.pack();
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                dialog.addString("8979");
//            }
//        });

        dialog.setVisible(true);

        System.exit(0);
    }
}
