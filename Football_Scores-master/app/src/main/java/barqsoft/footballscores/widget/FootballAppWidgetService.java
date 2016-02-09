package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.joda.time.LocalDate;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by Martin Melcher on 02/02/16.
 */
public class FootballAppWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FootballRemoteViewsFactory(getApplicationContext(), intent);
    }

    public class FootballRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private int mAppWidgetId;
        private Cursor mCursor;

        public FootballRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            LocalDate ld = new LocalDate();
            String[] date = new String[1];
            date[0] =ld.toString("yyyy-MM-dd");
            mCursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                    null,null,date,null);
        }

        @Override
        public void onDataSetChanged() {
            // do nothing
        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
            if (mCursor.moveToPosition(position)) {
                String homeName = mCursor.getString(scoresAdapter.COL_HOME);
                String awayName = mCursor.getString(scoresAdapter.COL_AWAY);
                String score = Utilies.getScores(
                        mCursor.getInt(scoresAdapter.COL_HOME_GOALS),
                        mCursor.getInt(scoresAdapter.COL_AWAY_GOALS)
                );
                String time = mCursor.getString(scoresAdapter.COL_MATCHTIME);

                rv.setTextViewText(R.id.tvHomeName, homeName);
                rv.setTextViewText(R.id.tvAwayName, awayName);
                rv.setTextViewText(R.id.tvScore, score);
                rv.setTextViewText(R.id.tvTime, time);

                // set content description for talk back
                rv.setContentDescription(rv.getLayoutId(),
                        Utilies.getContentDescription(mContext, homeName, awayName, score, time));
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
