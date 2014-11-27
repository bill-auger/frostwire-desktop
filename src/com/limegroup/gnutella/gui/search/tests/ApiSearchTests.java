package com.limegroup.gnutella.gui.search;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.search.SearchEngine;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.search.SearchResultMediator;
//import com.limegroup.gnutella.gui.GUIMediator;

import javax.swing.SwingUtilities;


@SuppressWarnings("unused")
public class ApiSearchTests {

    /* constants */

    private static final String BTDIGG_TEST_QUERY_VALID = "test BtDigg API search with valid query";
    private static final String BTDIGG_QUERY_VALID = "mit ocw";
    private static final String BTDIGG_TEST_QUERY_INVALID = "test BtDigg API search with invalid query";
    private static final String BTDIGG_QUERY_INVALID = ""; // must be 1 or more chars
    private static final String BTDIGG_TEST_QUERY_NONEXIST = "test BtDigg API search for non-existing torrent";
    private static final String BTDIGG_QUERY_NONEXIST = "XnoX-XresultsX-XnoX-XsuchX-XtorrentX" ;

    static long SearchGuid;
    static SearchResultMediator ResultsPanel;
    static int NExpectedResults = 0;
    static boolean DidTestPass;


    /* test steps */

    private static void runSearchQuery(String engineName, String query) {

        SearchEngine searchEngine = enableEngine(engineName);
        if (searchEngine == null) return;

        SearchGuid = startSearch(query);
        if (SearchGuid == 0) return;

// System.out.println("ApiSearchTests::runSearchQuery() query=" + query + " SearchGuid=" + SearchGuid);

        ResultsPanel = SearchMediator.getResultPanelForGUID(SearchGuid);

// if (ResultsPanel == null) System.out.println("ApiSearchTests::runSearchQuery() (resultsPanel == null)");
    }

    private static void getTestResult(int nExpectedResults, boolean shouldError) {

        if (ResultsPanel == null) {
            DidTestPass = shouldError;
        } else {

            int nActualResults = ResultsPanel.getSize(); // totalResults();
            DidTestPass = (nExpectedResults == nActualResults);

// System.out.println("ApiSearchTests::getNSearchResults() nResults=" + nActualResults + " size=" + nActualResul);
        }
    }


    /* helpers */

    private static void beforeEach() {
        SearchGuid = 0; ResultsPanel = null; DidTestPass = false;
    }

    private static long startSearch(String query) {

        MediaType mediaType = MediaType.getTorrentMediaType();
        SearchInformation info = SearchInformation.createTitledKeywordSearch(query, null, mediaType, query);
        long SearchGuid = SearchMediator.instance().triggerSearch(info);

        return SearchGuid;
    }

    private static void sleep(int t) {
        try { java.lang.Thread.sleep(t); } catch ( java.lang.InterruptedException ie) {}
    }

    private static SearchEngine enableEngine(String engineName) {

        SearchEngine testEngine = null;

        for (SearchEngine engine : SearchEngine.getEngines()) {
            boolean isTestEngine = (engine.getName() == engineName);
            engine.getEnabledSetting().setValue(isTestEngine);
            if (isTestEngine) testEngine = engine;

// System.out.println("ApiSearchTests::enableEngine() engine=" + engineName + " isEnabled=" + engine.isEnabled());
        }

        return testEngine;
    }

    private static String result(boolean didTestPass) {
        return ((didTestPass)? "passed" : "failed");
    }


    /* main */

    private static void runTest(String engineName, String testName, String query,
                                int nExpectedResults, boolean shouldError) {

        final String ENGINE_NAME = engineName;
        final String TEST_NAME = testName;
        final String QUERY = query;
        final int N_EXPECTED_RESULTS = nExpectedResults;
        final boolean SHOULD_ERROR = shouldError;

        final Runnable RUN_SEARCH_QUERY = new Runnable() {
            public void run() { runSearchQuery(ENGINE_NAME, QUERY); }
        };

        final Runnable GET_SEARCH_RESULTS = new Runnable() {
            public void run() { getTestResult(N_EXPECTED_RESULTS, SHOULD_ERROR); }
        };


        beforeEach();

        System.out.println("\n" + TEST_NAME);

        sleep(1000);
        try { SwingUtilities.invokeAndWait(RUN_SEARCH_QUERY); } catch (Exception ex) {}
        sleep(5000);
        try { SwingUtilities.invokeAndWait(GET_SEARCH_RESULTS); } catch (Exception ex) {}

        System.out.println(TEST_NAME + " => " + result(DidTestPass));
    }

    public static void main(String[] args) {

        System.out.println("ApiSearchTests"); sleep(1000);
        runTest("BtDigg", BTDIGG_TEST_QUERY_VALID,    BTDIGG_QUERY_VALID,   10, false);
        runTest("BtDigg", BTDIGG_TEST_QUERY_INVALID,  BTDIGG_QUERY_INVALID,  0, true);
        runTest("BtDigg", BTDIGG_TEST_QUERY_NONEXIST, BTDIGG_QUERY_NONEXIST, 0, false);
    }
}
