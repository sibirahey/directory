package org.jdc.template.ux.individualedit;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.analytics.HitBuilders;

import org.jdc.template.Analytics;
import org.jdc.template.model.database.main.individual.Individual;
import org.jdc.template.model.database.main.individual.IndividualManager;
import org.jdc.template.ui.mvp.BasePresenter;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IndividualEditPresenter extends BasePresenter {
    private final IndividualManager individualManager;
    private final Analytics analytics;

    private IndividualEditContract.View view;
    private long individualId;
    private Individual individual;

    @Inject
    public IndividualEditPresenter(IndividualManager individualManager, Analytics analytics) {
        this.individualManager = individualManager;
        this.analytics = analytics;
    }

    public void init(IndividualEditContract.View view, long individualId) {
        this.view = view;
        this.individualId = individualId;
    }

    @Override
    public void load() {
        loadIndividual();
    }

    private void loadIndividual() {
        individual = individualManager.findByRowId(individualId);
        if (individual != null) {
            analytics.send(new HitBuilders.EventBuilder()
                    .setCategory(Analytics.CATEGORY_INDIVIDUAL)
                    .setAction(Analytics.ACTION_EDIT)
                    .build());

            view.showIndividual(individual);
        }else {
            individual = new Individual();
        }
    }

    public void birthdayClicked() {
        view.showBirthDateSelector(individual.getBirthDate());
    }

    public void affiliationClicked(){
        view.showAffiliationSelector();
    }

    public void forceSensitiveClick(){
        view.showForceSensitiveSelector();
    }

    public void pictureTaken(Uri uri){
        view.showProfilePicture(uri);
    }

    public void birthDateSelected(LocalDate date) {
            individual.setBirthDate(date);
            view.showBirthDate(date);
    }

    public void saveIndividual() {
        if (view.validateIndividualData()) {
            view.getIndividualDataFromUi(individual);

            individualManager.save(individual);
            analytics.send(new HitBuilders.EventBuilder().setCategory(Analytics.CATEGORY_INDIVIDUAL).setAction(Analytics.ACTION_EDIT_SAVE).build());

            view.close();
        }
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
