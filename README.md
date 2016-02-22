# SuperDuo

## Alexandria

### General Changes

* DownloadImage service was replaced with Picasso (for image caching)
* Gradle was set to version 1.5.0 for the project so that vector image resources can be automatically converted to PNGs vor API level < 21.

### UI and UX Changes

* New material icons for OK, Cancel and Delete.
* Changed Button styles (back to default) as the buttons were not recognisable as such.
* Repaced v4 with  support lib v7 ActionBarDrawerToggle.
* Replaced custom back navigation on BookDetails with proper toolbar home/back navigation.

### Other Fixes

#### BookService

* check for empty response from server (empty JSON)

#### AddBook

* Check for an Internet connection before feching data from the API.
* Check if an author exists (empty author crashes the app).
* Use Zebra Crossing barcode scanning lib.

#### BookDetail

* Don't use a service for deleting books. Do it directly via the content resolver othervise the book list gets out of sync.
* Fixed device-rotation crash.
* Cater for empty authors.

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
* API parameter for the next 2 days changed from n2 to n3 as n2 would only be today and tmrw.

#### Widget

There is now a **collection widget** which shows today's games. Tapping on a games opens the MainActivity 
and scrolls on the Today page to the correct game. 

#### Database

Two new colums for team urls were added. These url are used for dynamically fetching a team's crest 
from wikimedia.

#### MainScreenFragment

 * Check for internet connection before starting the fetch service
 * Added text view for empty lists
 * Calculate date for loader here and not in PagerFragment 

#### PagerFragment

* update_scores was moved here from the MainScreenFragment. The number of unnecessary API calls is highly reduced by that.

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


