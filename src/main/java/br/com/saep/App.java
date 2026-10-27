package br.com.saep;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ponto de entrada do sistema SAEP. */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new LoginFrame().setVisible(true);
        });
    }
}
