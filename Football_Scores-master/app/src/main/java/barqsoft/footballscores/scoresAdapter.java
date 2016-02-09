package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by yehya khaled on 2/26/2015.
 * Updated by Martin Melcher 02/01/2016:
 * - Var renamed mXyz -> xyz as m usually indicated member variables which is not the case here.
 * - Changed share functionality to use chooser and got rid of "sunshine" code
 * - Made sharing hashtag a string resource
 * - Fixed League display (old league codes were used)
 * - Use wikimedia thumbnail generator for crests
 * - Added content description to list item -> talk back reads the score
 */
public class scoresAdapter extends CursorAdapter
{
    private static final String LOG_TAG = scoresAdapter.class.getSimpleName();
    private Context mContext;

    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_HOME_URL = 4;
    public static final int COL_AWAY = 5;
    public static final int COL_AWAY_URL = 6;
    public static final int COL_LEAGUE = 7;
    public static final int COL_HOME_GOALS = 8;
    public static final int COL_AWAY_GOALS = 9;
    public static final int COL_ID = 10;
    public static final int COL_MATCHDAY = 11;

    public double detail_match_id = 0;

    static class ViewHolder
    {
        TextView home_name;
        TextView away_name;
        TextView score;
        TextView date;
        ImageView home_crest;
        ImageView away_crest;
        double match_id;
        View the_view;
    }

    private void loadCrest(String crestUrl, ImageView view) {

        if (crestUrl.isEmpty()) {
            view.setImageResource(R.drawable.crest_48);
        }
        else {
            int dimen = Math.round(Utilies.dipToPixels(mContext, 48));
            Picasso.with(mContext).load(crestUrl).resize(dimen, dimen).centerInside()
                    .placeholder(R.drawable.crest_48).error(R.drawable.crest_48).into(view);
        }
    }

    public scoresAdapter(Context context,Cursor cursor,int flags)
    {
        super(context,cursor,flags);

        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View item = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder holder = new ViewHolder();
        item.setTag(holder);
        return item;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.home_name = (TextView) view.findViewById(R.id.home_name);
        holder.away_name = (TextView) view.findViewById(R.id.away_name);
        holder.score     = (TextView) view.findViewById(R.id.score_textview);
        holder.date      = (TextView) view.findViewById(R.id.data_textview);
        holder.home_crest = (ImageView) view.findViewById(R.id.home_crest);
        holder.away_crest = (ImageView) view.findViewById(R.id.away_crest);
        holder.the_view = view;

        final String homeName = cursor.getString(COL_HOME);
        final String score = Utilies.getScores(cursor.getInt(COL_HOME_GOALS),
                cursor.getInt(COL_AWAY_GOALS));
        final String awayName = cursor.getString(COL_AWAY);
        final String time = cursor.getString(COL_MATCHTIME);

        // set content description
        String conDesc = Utilies.getContentDescription(context, homeName, awayName, score, time);
        holder.the_view.setContentDescription(conDesc);

        holder.home_name.setText(homeName);
        holder.away_name.setText(awayName);
        holder.date.setText(time);
        holder.score.setText(score);
        holder.match_id = cursor.getDouble(COL_ID);

        String homeCrestUrl = cursor.getString(COL_HOME_URL);
        loadCrest(homeCrestUrl, holder.home_crest);
        String awayCrestUrl = cursor.getString(COL_AWAY_URL);
        loadCrest(awayCrestUrl, holder.away_crest);

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if(holder.match_id == detail_match_id)
        {

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(context, cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilies.getLeague(context, cursor.getInt(COL_LEAGUE)));

            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String shareText = homeName + " " + score +
                            " " + awayName + " " + context.getString(R.string.share_hashtag);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent,
                            context.getString(R.string.share_text_chooser)));
                }
            });

            // add content description to share button
            share_button.setContentDescription(mContext.getString(R.string.conDescShareButton));
        }
        else
        {
            container.removeAllViews();
        }
    }
}
