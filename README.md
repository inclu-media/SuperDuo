# SuperDuo

## Football Scores

### API Key

Create a resource file api_key.xml in the res folder and fill it with the following lines:

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="api_key">[YOUR FOOTBALL-DATA.ORG API-KEY]</string>
</resources>
```

### Key Changes

#### myFetchService

* Leagues to be considered are read from leagues.xml
* API endpoints and constants put into resource file (api.xml)
* API version switched to v1
* Home and Away Team URLs read from API and stored into DB
* Team crests are fetched from wikimedia and cached in shared prefs (urls)
* API parameter for the next 2 days changed from n2 to n3 as n2 would be only be today and tmrw.

#### Widget

There is now a **collection widget** which shows today's games. Tapping on a games opens the MainActivity 
and scrolls on the Today page to the correct game. 

#### Database

Two now colums for team urls were added. These url are used for dynamically fetching a team's crest 
from wikimedia.

#### MainScreenFragment

 * Check for internet connection before starting the fetch service
 * Added text view for empty lists
 * Calculate date for loader here and not in PagerFragment 

#### PagerFragment

* update_scores was moved gere from the MainScreenFragment. The number of unnecessary API calls is highly reduced by that.

#### scoresAdapter

* Var renamed mXyz -> xyz as m usually indicated member variables which is not the case here.
* Changed share functionality to use chooser and got rid of "sunshine" code
* Made sharing hashtag a string resource
* Fixed League display (old league codes were used)
* Use wikimedia thumbnail generator for crests
* Added content description to list item -> talk back reads the score

#### Utilities

* Read league names and codes from resource file instead of constants
* "Matchday" added as a translatable string resource
* Added getDataForLeague to be used for API calls


