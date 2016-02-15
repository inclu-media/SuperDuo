package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by Martin Melcher on 02/02/16.
 */
public class FootballAppWidgetProvider extends AppWidgetProvider {

    public static final String MATCH_INDEX = "match_index";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, FootballAppWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.lvMatches, intent);
            rv.setEmptyView(R.id.lvMatches, R.id.lvEmpty);

            // set up the pending intent template
            Intent appLaunchIntent = new Intent(context, MainActivity.class);
            appLaunchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            appLaunchIntent.setData(Uri.parse(appLaunchIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent appLaunchPendingIntent = PendingIntent.getActivity(context, 0,
                    appLaunchIntent, 0);
            rv.setPendingIntentTemplate(R.id.lvMatches, appLaunchPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
