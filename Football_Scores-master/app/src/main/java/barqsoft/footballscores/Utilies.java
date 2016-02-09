package barqsoft.footballscores;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by yehya khaled on 3/3/2015.
 * Updated by Martin Melcher 01/02/2016:
 * - Read league names and codes from resource file instead of constants
 * - "Matchday" added as a translatable string resource
 * - Added getDataForLeague to be used for API calls
 */
public class Utilies
{

    private static final String LEAGUE_PREFIX = "league";

    public static String getLeague(Context context, int league_num)
    {
        int resId = context.getResources().getIdentifier(LEAGUE_PREFIX + String.valueOf(league_num),
                "string", context.getPackageName());
        if (resId > 0) {
            return context.getString(resId);
        }
        else {
            return context.getString(R.string.unknown_league);
        }
    }

    /**
     * Do we need data for this league?
     * Leagues are configured in leagues.xml
     * @param context
     * @param sLeagueNum String representation of the league number
     * @return boolean
     */
    public static boolean getDataForLeague(Context context, String sLeagueNum) {
        int resId = context.getResources().getIdentifier(LEAGUE_PREFIX + sLeagueNum, "string",
                context.getPackageName());
        return (resId > 0);
    }

    public static String getMatchDay(Context context, int match_day,int league_num)
    {
        // champions league is not configured in the league codes -> ignore is here
        return context.getString(R.string.matchday) + " : " + String.valueOf(match_day);
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static String getContentDescription(Context context, String homeName,
                                               String awayName, String score, String time) {
        return homeName + " " + context.getString(R.string.versus) + " " +
                awayName +". "+ score + ". " + context.getString(R.string.startsAt) + " " + time;
    }
}
