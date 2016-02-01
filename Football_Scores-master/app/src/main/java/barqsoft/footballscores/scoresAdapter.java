package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yehya khaled on 2/26/2015.
 * Updated by Martin Melcher 02/01/2016:
 * - Var renamed mXyz -> xyz as m usually indicated member variables which is not the case here.
 * - Changed share functionality to use chooser and got rid of "sunshine" code
 * - Made sharing hashtag a string resource
 * - Fixed League display (old league codes were used)
 * - Use glide for displaying crests-svg's (url was previously loaded into db during fetch)
 */
public class scoresAdapter extends CursorAdapter
{
    private static final String LOG_TAG = scoresAdapter.class.getSimpleName();
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> mSVGRequestBuilder;
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

    private class SvgDecoder implements ResourceDecoder<InputStream, SVG> {
        public Resource<SVG> decode(InputStream source, int width, int height) throws IOException {
            try {
                SVG svg = SVG.getFromInputStream(source);
                return new SimpleResource<SVG>(svg);
            } catch (SVGParseException ex) {
                throw new IOException("Cannot load SVG from stream", ex);
            }
        }

        @Override
        public String getId() {
            return "SvgDecoder.com.bumptech.svgsample.app";
        }
    }

    private class SvgDrawableTranscoder implements ResourceTranscoder<SVG, PictureDrawable> {
        @Override
        public Resource<PictureDrawable> transcode(Resource<SVG> toTranscode) {
            SVG svg = toTranscode.get();

            int dimen = Math.round(Utilies.dipToPixels(mContext, 48));
            svg.setDocumentHeight(dimen);
            svg.setDocumentWidth(dimen);

            Picture picture = svg.renderToPicture(dimen, dimen);

            PictureDrawable drawable = new PictureDrawable(picture);
            return new SimpleResource<PictureDrawable>(drawable);
        }

        @Override
        public String getId() {
            return "";
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SvgSoftwareLayerSetter<T> implements RequestListener<T, PictureDrawable> {

        @Override
        public boolean onException(Exception e, T model, Target<PictureDrawable> target, boolean isFirstResource) {
            ImageView view = ((ImageViewTarget<?>) target).getView();
            if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
                view.setLayerType(ImageView.LAYER_TYPE_NONE, null);
            }
            return false;
        }

        @Override
        public boolean onResourceReady(PictureDrawable resource, T model, Target<PictureDrawable> target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {
            ImageView view = ((ImageViewTarget<?>) target).getView();
            if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
                view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
            }

            return false;
        }
    }


    private void loadCrest(String crestUrl, ImageView view, boolean showCrest) {

        // don't get the crest from SCG for past matches
        // saves memory
        if (!showCrest) {
            view.setImageResource(R.drawable.crest_48);
            return;
        }

        if (crestUrl.endsWith(".png")) {
            int dimen = Math.round(Utilies.dipToPixels(mContext, 48));
            Glide.with(mContext).load(Uri.parse(crestUrl)).override(dimen, dimen)
                    .centerCrop().into(view);
        }
        else {
            mSVGRequestBuilder.diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .load(Uri.parse(crestUrl)).into(view);
        }
    }

    public scoresAdapter(Context context,Cursor cursor,int flags)
    {
        super(context,cursor,flags);

        mContext = context;
        mSVGRequestBuilder = Glide.with(context)
                .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(R.drawable.crest_48)
                .error(R.drawable.crest_48)
                .listener(new SvgSoftwareLayerSetter<Uri>());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View item = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder holder = new ViewHolder(item);
        item.setTag(holder);
        return item;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        ViewHolder holder = (ViewHolder) view.getTag();
        final String homeName = cursor.getString(COL_HOME);
        final String score = Utilies.getScores(cursor.getInt(COL_HOME_GOALS),
                cursor.getInt(COL_AWAY_GOALS));
        final String awayName = cursor.getString(COL_AWAY);

        holder.home_name.setText(homeName);
        holder.away_name.setText(awayName);
        holder.date.setText(cursor.getString(COL_MATCHTIME));
        holder.score.setText(score);
        holder.match_id = cursor.getDouble(COL_ID);

        // set crests using Glide
        // only render SVG crests for today's matches (high memory consumption)
        DateTime dt = new DateTime(cursor.getString(COL_DATE));
        boolean useCrests = ((dt.toLocalDate()).equals(new LocalDate()));

        String homeCrestUrl = cursor.getString(COL_HOME_URL);
        loadCrest(homeCrestUrl, holder.home_crest, useCrests);
        String awayCrestUrl = cursor.getString(COL_AWAY_URL);
        loadCrest(awayCrestUrl, holder.away_crest, useCrests);

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
        }
        else
        {
            container.removeAllViews();
        }
    }
}
