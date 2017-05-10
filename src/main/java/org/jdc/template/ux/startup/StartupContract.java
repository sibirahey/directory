package org.jdc.template.ux.startup;

public class StartupContract {
    interface View {
        void showStartActivity();
        void close();
        void showMessage(String message);
    }

    class Extras {
    }
}
