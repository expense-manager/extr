package com.expensemanager.app.help;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.service.font.Font;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AhoyOnboarderActivity {

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, HelpActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AhoyOnboarderCard groupIntroduction = new AhoyOnboarderCard(getString(R.string.make_groups), getString(R.string.group_introduction), R.drawable.help_icon_group);
        AhoyOnboarderCard categoryIntroduction = new AhoyOnboarderCard(getString(R.string.define_categories), getString(R.string.category_introduction), R.drawable.help_icon_category);
        AhoyOnboarderCard expenseIntroduction = new AhoyOnboarderCard(getString(R.string.add_expenses), getString(R.string.expense_introduction), R.drawable.help_icon_expense);
        AhoyOnboarderCard reportIntroduction = new AhoyOnboarderCard(getString(R.string.view_reports), getString(R.string.report_introduction), R.drawable.help_icon_report);
        AhoyOnboarderCard helpIntroduction = new AhoyOnboarderCard(getString(R.string.get_help), getString(R.string.help_introduction), R.drawable.help_icon_help);

        groupIntroduction.setBackgroundColor(R.color.black_transparent);
        categoryIntroduction.setBackgroundColor(R.color.black_transparent);
        expenseIntroduction.setBackgroundColor(R.color.black_transparent);
        reportIntroduction.setBackgroundColor(R.color.black_transparent);
        helpIntroduction.setBackgroundColor(R.color.black_transparent);

        List<AhoyOnboarderCard> pages = new ArrayList<>();

        pages.add(groupIntroduction);
        pages.add(categoryIntroduction);
        pages.add(expenseIntroduction);
        pages.add(reportIntroduction);
        pages.add(helpIntroduction);

        for (AhoyOnboarderCard page : pages) {
            page.setTitleColor(R.color.white);
            page.setDescriptionColor(R.color.grey_200);
        }

        setFinishButtonTitle("Get Started");
        showNavigationControls(true);
        //setGradientBackground();
        setImageBackground(R.drawable.intro_background);

        setFont(EApplication.getInstance().getTypeface(Font.BOLD));

        setInactiveIndicatorColor(R.color.grey_600);
        setActiveIndicatorColor(R.color.white);

        setOnboardPages(pages);

    }

    @Override
    public void onFinishButtonPressed() {
        finish();
    }
}
