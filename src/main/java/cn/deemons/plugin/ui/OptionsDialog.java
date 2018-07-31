package cn.deemons.plugin.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;

public class OptionsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox parserStringsXmlToCheckBox;
    private JCheckBox parserExcelToStringsCheckBox;
    private JRadioButton replaceRadioButton;
    private JRadioButton deleteRadioButton;
    private OptionsListener listener;

    public OptionsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        replaceRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (replaceRadioButton.isSelected() && deleteRadioButton.isSelected()) {
                    deleteRadioButton.setSelected(false);
                }
            }
        });

        deleteRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (deleteRadioButton.isSelected() && replaceRadioButton.isSelected()) {
                    replaceRadioButton.setSelected(false);
                }
            }
        });


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
        dispose();
        if (listener != null) {
            listener.onCheck(parserStringsXmlToCheckBox.isSelected(),
                    parserExcelToStringsCheckBox.isSelected(),
                    replaceRadioButton.isSelected(), deleteRadioButton.isSelected());
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        OptionsDialog dialog = new OptionsDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void setListener(OptionsListener listener) {
        this.listener = listener;
    }

    public interface OptionsListener {
        void onCheck(boolean toTable, boolean toXml, boolean replace, boolean delete);
    }


}
