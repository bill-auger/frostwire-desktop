example model setup and callchain for searches

this was compiled while implementing JSON API search queries
    based on the existing PagedWebSearchPerformer class

other search query types may be more or less similar

feel free to add to this and/or create separate documents
    for other search query types if they differ greatly as
    this documentation will be invaluable for adding new search engines and query types


NOTE:
  the 'single-quoted' instance names within are intended to be unique and consistent
      across the scope of this document and so do not match the variable names
      used in the actual code which are not as consistent or descriptive as they could be


============================================================
ON STARTUP:

------------------------------------------------------------
SearchEngine.java
  => creates public class constant list of SearchEngine 'SEARCH_ENGINES'
  for each supported search engine named 'SOME'
    => creates public class constant int 'SOME_ID'
    => creates public class constant subclass singleton of SearchEngine 'SOME'
       with a public method getPerformer()

  SOME::getPerformer()
    => returns a SomeSearchPerformer instance adding it to SEARCH_ENGINES

NOTE:
  in addition at least these following classes must be defined:
    * SomeSearchPerformer
    * SomeSearchResponse
    * SomeSearchResult
    * SomeSearchItem

------------------------------------------------------------
SearchEngineSettings.java
  for each supported search engine named "SOME"
    => creates public class constant instance of BooleanSetting 'SOME_SEARCH_ENABLED'
       associated with the options enable/disable checkboxes
       and stored as a private property of each *SearchEngine instance)
       accessible via engine.getEnabledSetting() (.setValue() & .getValue())

------------------------------------------------------------
SearchMediator.java
  => creates singleton of SearchMediator                  'searchMediator'
  => creates singleton of SearchManagerImpl               'searchManagerImpl'
  => creates singleton of SearchMediator::ManagerListener 'searchManagerListener'

NOTE:
  searchManagerImpl and searchManagerListener are not enforced singltons but they effectively are
    as the SearchMediator singleton holds the only instantiated SearchManagerImpl and
    that SearchManagerImpl instance holds the only instantiated ManagerListener

------------------------------------------------------------
SearchManagerImpl.java
  => creates empty list of PerformTask 'performTasks'
  => creates instance   of ThreadPool  'threadPool'

------------------------------------------------------------
SearchResultDisplayer.java
  => creates singleton of SearchResultDisplayer 'searchResultDisplayer'
  => creates empty list of SearchResultMediator 'searchResultMediators'

NOTE:
  searchResultDisplayer is not an enforced singleton but it effectively is
    as the SearchMediator singleton holds the only instantiated SearchResultDisplayer
    it is created lazily via SearchResultDisplayer::getSearchResultDisplayer()


============================================================
ON USER INITIATED SEARCH:

------------------------------------------------------------
ApplicationHeader.java
  for singlton? instance of ApplicationHeader 'applicationHeader' (TODO:)
  for applicationHeader instance of GoogleSearchField 'cloudSearchField'
  * SearchListener::actionPerformed()
    => gets instance of String 'query' (and indirectly 'queryTitle')
       via cloudSearchField.getText();
    => gets instance of MediaType 'mediaType'
       via MediaType.getTorrentMediaType() (TODO: unclear why this is so specific)
    => creates instance of SearchInformation 'info'
       via SearchInformation::createTitledKeywordSearch(query, mediaType, queryTitle);
    => gets singleton instance of SearchMediator 'searchMediator'
       via SearchMediator::instance()
    => calls searchMediator.triggerSearch()

NOTE:
  searchMediator.triggerSearch() is also called from: (TODO: under what circumstances?)
  * SearchAction.java            => SearchAction::actionPerformed()
  * MagnetClipboardListener.java => MagnetClipboardListener::handleMagnets()

------------------------------------------------------------
SearchMediator.java
  for SearchMediator singleton of SearchMediator    'searchMediator'
  for searchMediator singleton of SearchManagerImpl 'searchManagerImpl'

  * searchMediator.triggerSearch()
    for parameter of SearchInformation 'info'
    => creates long 'searchGuid' via newSearchToken()
    => calls SearchResultMediator::addResultTab(searchGuid, info)
    => gets String 'query' via info.getQuery()
    => calls searchMediator.performSearch(searchGuid, query)

  * searchMediator.repeatSearch()
    for parameter of SearchResultMediator 'searchResultMediator'
    for parameter of SearchInformation    'info'
    => gets long 'prevToken' via searchResultMediator.getToken()
    => calls searchResultMediator.stopSearch(prevToken)
    => creates long 'searchGuid' via searchMediator.newSearchToken()
    => gets String 'query' via info.getQuery()
    => calls searchMediator.performSearch(searchGuid, query)

  * searchMediator.performSearch()
    => gets list of SearchEngine 'searchEngines' via SearchEngine::getEngines()
    for each instance of SearchEngine 'searchEngine' in searchEngines
      => gets an instance of SearchPerformer 'searchPerformer'
         via searchEngines.getPerformer()
      => calls searchManagerImpl.perform(sp) if searchEngines.isEnabled()

  * SearchMediator::addResultTab()
    for long parameter                      'searchGuid'
    for      parameter of SearchInformation 'info'
    => get singleton instance of SearchMediator 'searchMediator'
       via SearchMediator::instance()
    => gets String 'query'
       via info.getQuery()
    => creates list of String 'queryTokens'
       via searchMediator.tokenize(query)
    => gets singleton of SearchResultDisplayer 'searchResultDisplayer'
       via SearchMediator::getSearchResultDisplayer()
    => gets instance of SearchResultMediator 'searchResultMediator'
       via searchResultDisplayer.addResultTab(searchGuid, queryTokens, info)
    => returns searchResultMediator

------------------------------------------------------------
SearchResultDisplayer.java
  for singleton                  of SearchResultDisplayer 'searchResultDisplayer'
  for searchResultDisplayer list of SearchResultMediator  'searchResultMediators'

  * searchResultDisplayer.addResultTab()
    for long parameter                      'searchGuid'
    for list parameter of String            'queryTokens'
    for      parameter of SearchInformation 'info'
    => creates instance of SearchResultMediator 'searchResultMediator'
       via SearchResultMediator(searchGuid, queryTokens, info)
    => gets String 'title' via info.getTitle()
    => calls searchResultDisplayer.addResultPanelInternal(searchResultMediator, title)
    => returns 'searchResultMediator'

  * searchResultDisplayer.addResultPanelInternal()
    for parameter of SearchResultMediator 'searchResultMediator'
    for parameter of String               'title'
    => adds searchResultMediator to searchResultMediators
    => returns searchResultMediator

------------------------------------------------------------
SearchManagerImpl.java
  for SearchMediator    singleton of SearchMediator    'searchMediator' (outside class)
  for searchMediator    singleton of SearchManagerImpl 'searchManagerImpl'
  for searchManagerImpl instance  of ThreadPool        'threadPool'

  * searchManagerImpl.perform()
    for parameter of SearchPerformer 'searchPerformer'
    => creates an instance of PerformerResultListener 'performerResultListener'
       via PerformerResultListener(searchManagerImpl)
    => creates an instance of PerformTask             'performTask'
       via PerformTask(searchPerformer)
    => calls sp.registerListener(performerResultListener)
    => calls searchManagerImpl.submitSearchTask(performTask)

  * searchManagerImpl.submitSearchTask()
    for parameter of PerformTask 'performTask'
    => adds performTask to performTasks
    => calls tp.executeTask(performTask)

  * (some asynchonous magic happens)
    => then performerResultListener::onResults() fires

------------------------------------------------------------
PerformerResultListener.java
  * PerformerResultListener::PerformerResultListener()
    for parameter of SearchManagerImpl 'searchManagerImpl'
    => stores reference to searchManagerImpl on new instance


============================================================
ON SEARCH RESULTS:

------------------------------------------------------------
PerformerResultListener.java
  for performerResultListener singleton of SearchManagerImpl 'searchManagerImpl'
  * performerResultListener.onResults()
    for      parameter of SearchPerformer 'searchPerformer'
    for list parameter of SearchResult    'searchResults'
    => creates List<SearchResult> 'completedSearchResults' and populates like:
      for each instance of SearchResult 'searchResult' in searchResults
        if searchResult is instance of CrawlableSearchResult
             but not searchResult.isComplete()
          => defer adding but call searchManagerImpl.crawl(searchPerformer, searchResult)
             whereby some more asynchonous magic happens?
        else
          => completedSearchResults.add()
    => calls searchManagerImpl.onResults(searchPerformer, completedSearchResults)

------------------------------------------------------------
SearchManagerImpl.java
  for SearchMediator    singleton of SearchMediator    'searchMediator' (outside class)
  for searchMediator    singleton of SearchManagerImpl 'searchManagerImpl'
  for searchManagerImpl singleton of ManagerListener   'searchManagerListener'

  * smi.onResults()
    for      parameter of SearchPerformer 'searchPerformer'
    for list parameter of SearchResult    'searchResults'
    => calls searchManagerListener.onResults(searchPerformer, searchResults)

------------------------------------------------------------
SearchMediator.java
  for SearchMediator    singleton of SearchMediator    'searchMediator'
  for searchMediator    singleton of SearchManagerImpl 'searchManagerImpl'
  for searchManagerImpl singleton of ManagerListener   'searchManagerListener'

  * searchManagerListener.onResults()
    for      parameter of SearchPerformer 'searchPerformer'
    for list parameter of SearchResult    'searchResults'
    => gets             long                 'searchGuid'
       via searchPerformer.getToken()
    => gets instance of SearchResultMediator 'searchResultMediator'
       via SearchMediator::getResultPanelForGUID(searchGuid)
    => gets list     of String               'queryTokens'
       via searchResultMediator.getSearchTokens()
    => creates list  of SearchResult         'filteredSearchResults'
       via sm.filter(searchPerformer, searchResults, queryTokens)
    => gets instance of SearchResult         'searchResult'
       from head of filteredSearchResults
    => gets instance of SearchEngine         'searchEngine'
       via SearchEngine::getSearchEngineByName(searchResult)
    => gets instance of string               'query'
       via searchResultMediator.getQuery()
    => gets list     of UISearchResult 'uISearchResults'
       via SearchMediator::convertResults(filteredSearchResults, searchEngine, query)
    for each instance of UISearchResult 'uISearchResult' in uISearchResults
      => gets singleton of SearchResultDisplayer 'searchResultDisplayer'
         via SearchMediator::getSearchResultDisplayer()
      => calls searchResultDisplayer.addQueryResult(searchGuid, uISearchResult, searchResultMediator)

  * SearchMediator::getResultPanelForGUID()
    for long parameter of searchGuid
    => gets singleton of SearchResultDisplayer 'searchResultDisplayer'
        via SearchMediator::getSearchResultDisplayer()
    => gets instance of SearchResultMediator 'searchResultMediator'
        via searchResultDisplayer.getResultPanelForGUID(searchGuid)
    => returns SearchResultMediator

  * SearchMediator::convertResults()
    for list parameter of SearchResult 'searchResults'
    for      parameter of SearchEngine 'searchEngine'
    for      parameter of String       'query'
    => creates list of UISearchResult 'uISearchResults' and populates like:
      map each instance of SearchResult 'searchResult' in searchResults
        case searchResult instance of
          * YouTubeCrawledSearchResult
            => YouTubeUISearchResult(searchResult, searchEngine, query)
          * SoundcloudSearchResult
            => SoundcloudUISearchResultsearchResult, searchEngine, query)
          * TorrentSearchResult
            => TorrentUISearchResult(searchResult, searchEngine, query)
          * ArchiveorgCrawledSearchResult
            => ArchiveorgUISearchResult(searchResult, searchEngine, query)
    => returns uISearchResults

------------------------------------------------------------
SearchResultDisplayer.java
  for SearchResultDisplayer singleton of SearchResultDisplayer 'searchResultDisplayer'
  for searchResultDisplayer list      of SearchResultMediator  'searchResultMediators'

  * searchResultDisplayer.getResultPanelForGUID()
    for long parameter 'searchGuid'
    => finds matching instance of SearchResultMediator 'searchResultMediator'
       in 'searchResultMediators'
    => returns searchResultMediator


============================================================
TODO:
* which events lead to *SearchPerformer::searchPage()
* when are GeneralResultFilter, MediaTypeFilter, SearchEngineFilter created?
  and what is the fourth one? especially for MediaTypeFilter
  (seems to be created in SearchResultDataLine::initialize()
  per NamedMediaType.getFromExtension using extension from *SearchResult)
