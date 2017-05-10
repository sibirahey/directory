package org.jdc.template.ux.startup;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.analytics.HitBuilders;

import org.jdc.template.Analytics;
import org.jdc.template.BuildConfig;
import org.jdc.template.event.NewDataEvent;
import org.jdc.template.model.database.DatabaseManager;
import org.jdc.template.model.database.main.household.Household;
import org.jdc.template.model.database.main.household.HouseholdManager;
import org.jdc.template.model.database.main.individual.Individual;
import org.jdc.template.model.database.main.individual.IndividualManager;
import org.jdc.template.model.type.IndividualType;
import org.jdc.template.model.webservice.individuals.IndividualService;
import org.jdc.template.model.webservice.individuals.dto.DtoIndividual;
import org.jdc.template.model.webservice.individuals.dto.DtoIndividuals;
import org.jdc.template.ui.mvp.BasePresenter;
import org.jdc.template.util.AppConstants;
import org.jdc.template.util.RxUtil;
import org.threeten.bp.LocalDate;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pocketbus.Bus;
import pocketbus.Subscribe;
import timber.log.Timber;

public class StartupPresenter extends BasePresenter {
    private final Analytics analytics;
    private final DatabaseManager databaseManager;

    private StartupContract.View view;
    private long perfTime = 0;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private final Bus bus;
    private final IndividualService individualService;
    private final IndividualManager individualManager;
    private final HouseholdManager householdManager;

    @Inject
    public StartupPresenter(Application application, Analytics analytics, DatabaseManager databaseManager,
                            IndividualService individualService, IndividualManager individualManager,
                            HouseholdManager householdManager, Bus bus) {
        this.analytics = analytics;
        this.databaseManager = databaseManager;
        this.sharedPreferences = application.getSharedPreferences(AppConstants.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        this.sharedPreferencesEditor = application.getSharedPreferences(AppConstants.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        this.individualService = individualService;
        this.individualManager = individualManager;
        this.householdManager = householdManager;
        this.bus = bus;
    }

    public void init(StartupContract.View view) {
        this.view = view;
    }

    @Override
    public void load() {
        analytics.send(new HitBuilders.EventBuilder()
                .setCategory(Analytics.CATEGORY_APP)
                .setAction(Analytics.ACTION_APP_LAUNCH)
                .setLabel(BuildConfig.BUILD_TYPE)
                .build());

        if(!sharedPreferences.getBoolean("isWebServiceConsumed", false) ){
            RxUtil.toRetrofitObservable(individualService.individuals())
                    .subscribeOn(Schedulers.io())
                    .map(response -> RxUtil.verifyRetrofitResponse(response))
                    .filter(dtoSearchResponse -> dtoSearchResponse != null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(dtoIndividuals -> processIndividualsResponse(dtoIndividuals), throwable -> bus.post(new NewDataEvent(false, throwable)), () -> bus.post(new NewDataEvent(true)));
        }else{
            view.showMessage("REST Service ALREADY in Database");
            postStartup();
        }
    }

    private boolean startup() {
        perfTime = System.currentTimeMillis();
        databaseManager.initDatabaseConnection();
        return true;
    }

    private void postStartup(boolean success) {
        Timber.d("Startup Elapsed Time: %d ms", (System.currentTimeMillis() - perfTime));
        view.showStartActivity();
        view.close();
    }

    @Override
    public void register() {
        bus.register(this);
    }

    @Override
    public void unregister() {
        bus.unregister(this);
    }

    @Subscribe
    public void handle(NewDataEvent event) {
        Timber.i(event.getThrowable(), "Rest Service finished [%b]", event.isSuccess());
        if(!event.isSuccess()){
            view.showMessage("REST Service Error");
        }
    }

    public void processIndividualsResponse(DtoIndividuals dtoIndividuals) {

        postStartup();
    }

    private void postStartup() {
        Single.defer(() -> Single.just(startup()))
                .subscribeOn(Schedulers.io())
//                .filter(success -> success) // bail on fail?
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> postStartup(success));
    }
}
