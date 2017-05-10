package org.jdc.template.ux.individualedit;

import android.net.Uri;

import org.jdc.template.model.database.main.individual.Individual;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

public class IndividualEditContract {
    interface View {
        void showIndividual(Individual individual);
        boolean validateIndividualData();
        void getIndividualDataFromUi(Individual individual);
        void close();
        void showBirthDateSelector(LocalDate date);
        void showBirthDate(LocalDate date);
        void showForceSensitiveSelector();
        void showAffiliationSelector();
        void showProfilePicture(Uri uri);
    }

    public class Extras {
        public static final String EXTRA_ID = "INDIVIDUAL_ID";
    }
}
