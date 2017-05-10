package org.jdc.template.ux.individual;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.dbtools.android.domain.date.DBToolsThreeTenFormatter;
import org.jdc.template.InternalIntents;
import org.jdc.template.inject.Injector;
import org.jdc.template.model.database.main.individual.Individual;
import org.jdc.template.ui.activity.BaseActivity;
import org.jdc.template.util.AppConstants;
import org.jdc.template.util.FloatingActionImageView;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.jdc.template.R;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IndividualActivity extends BaseActivity implements IndividualContract.View {

    private SharedPreferences sharedPreferences;

    @Nullable
    @BindView(R.id.mainToolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.nameTextView)
    TextView nameTextView;
    @BindView(R.id.birthDateTextView)
    TextView birthDateTextView;
    @BindView(R.id.affiliationTextView)
    TextView affiliationTextView;
    @BindView(R.id.forceSensitiveTextView)
    TextView forceSensitiveTextView;
    @BindView(R.id.activityIndividualImage)
    ImageView activityIndividualImage;

    @Inject
    InternalIntents internalIntents;

    @Inject
    IndividualPresenter presenter;

    public IndividualActivity() {
        Injector.get().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getApplication().getSharedPreferences(AppConstants.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_individual);
        ButterKnife.bind(this);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        setupActionBar();

        long individualId = getIntent().getLongExtra(IndividualContract.Extras.EXTRA_ID, 0L);
        presenter.init(this, individualId);
        presenter.load();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        enableActionBarBackArrow();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.individual);
        }

        if(sharedPreferences.getInt(AppConstants.DEVICE_SCREEN_SIZE, 0) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) activityIndividualImage.getLayoutParams();
            lp.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT;
            lp.rightMargin = 20;
            activityIndividualImage.setLayoutParams(lp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.individual_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit:
                presenter.editIndividualClicked();
                return true;
            case R.id.menu_item_delete:
                presenter.deleteIndividualClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.reload(true);
    }

    public void promptDeleteIndividual() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_individual_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    presenter.deleteIndividual();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void showIndividual(@Nonnull Individual individual) {
        nameTextView.setText(individual.getFullName());
        showBirthDate(individual);
        affiliationTextView.setText(individual.getAffiliation().toString().replace("_", " "));
        showForceSensitive(individual);
        if(!individual.getProfilePicture().isEmpty()){
            Picasso.with(this).load(individual.getProfilePicture()).placeholder(R.drawable.default_image).into(activityIndividualImage);
        }else{
            Picasso.with(this).load(R.drawable.default_image).placeholder(R.drawable.default_image).into(activityIndividualImage);
        }
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void showEditIndividual(long individualId) {
        internalIntents.editIndividual(this, individualId);
    }

    private void showBirthDate(Individual individual) {
        if (individual.getBirthDate() == null) {
            return;
        }

        LocalDate date = individual.getBirthDate();
        long millis = DBToolsThreeTenFormatter.localDateTimeToLong(date.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());
        birthDateTextView.setText(DateUtils.formatDateTime(this, millis, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    private void showForceSensitive(Individual individual){
        if(individual.isForceSensitive()){
            forceSensitiveTextView.setText("YES");
        }else{
            forceSensitiveTextView.setText("NO");
        }
    }
}